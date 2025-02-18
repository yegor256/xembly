/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Directives in plain text.
 * @since 0.24
 */
@SuppressWarnings({
    "PMD.GodClass",
    "PMD.InefficientEmptyStringCheck",
    "PMD.DoubleBraceInitialization"
})
final class Verbs {
    /**
     * Commands without arguments.
     */
    @SuppressWarnings("PMD.NonStaticInitializer")
    private static final Map<String, Callback<Directive>> ARGUMENTLESS =
        new HashMap<String, Callback<Directive>>() {
            {
                this.put("REMOVE", RemoveDirective::new);
                this.put("UP", UpDirective::new);
                this.put("PUSH", PushDirective::new);
                this.put("POP", PopDirective::new);
            }
        };

    /**
     * Commands with one argument.
     */
    @SuppressWarnings("PMD.NonStaticInitializer")
    private static final Map<String, Function<String, Callback<Directive>>> SIMPLE =
        new HashMap<String, Function<String, Callback<Directive>>>() {
            {
                this.put("XPATH", value -> () -> new XpathDirective(value));
                this.put("SET", value -> () -> new SetDirective(value));
                this.put("XSET", value -> () -> new XsetDirective(value));
                this.put("ADD", value -> () -> new AddDirective(value));
                this.put("ADDIF", value -> () -> new AddIfDirective(value));
                this.put("STRICT", value -> () -> new StrictDirective(Integer.parseInt(value)));
                this.put("CDATA", value -> () -> new CdataDirective(value));
                this.put("COMMENT", value -> () -> new CommentDirective(value));
            }
        };

    /**
     * Commands with two arguments.
     */
    @SuppressWarnings("PMD.NonStaticInitializer")
    private static final Map<String, BiFunction<String, String, Callback<Directive>>> COMPLEX =
        new HashMap<String, BiFunction<String, String, Callback<Directive>>>() {
            {
                this.put("ATTR", (attr, value) -> () -> new AttrDirective(attr, value));
                this.put("XATTR", (attr, value) -> () -> new XattrDirective(attr, value));
                this.put("PI", (target, data) -> () -> new PiDirective(target, data));
            }
        };

    /**
     * Text.
     */
    private final String text;

    /**
     * Directives.
     */
    private final Collection<Directive> dirs;

    /**
     * Ctor.
     * @param txt Text to parse
     */
    Verbs(final String txt) {
        this.text = txt;
        this.dirs = new LinkedList<>();
    }

    /**
     * Parse directives from text.
     * @return Directives from text
     */
    public Iterable<Directive> directives() {
        final String trimmed = this.text.trim();
        if (!trimmed.isEmpty()) {
            try {
                final String semicolon = ";";
                final StringBuilder builder = new StringBuilder();
                for (final String part : trimmed.split(semicolon)) {
                    if (builder.length() == 0) {
                        builder.append(Verbs.ltrim(part));
                    } else {
                        builder.append(part);
                    }
                    final Optional<Callback<Directive>> command =
                        Verbs.parsedCommand(builder.toString());
                    if (command.isPresent()) {
                        this.dirs.add(command.get().call());
                        builder.setLength(0);
                    } else {
                        builder.append(semicolon);
                    }
                }
            } catch (final XmlContentException | ParsingException ex) {
                throw new SyntaxException(
                    String.format(
                        "Parsing failed as %s: \"%s\"",
                        ex.getClass().getCanonicalName(),
                        ex.getLocalizedMessage()
                    ),
                    ex
                );
            }
        }
        return Collections.unmodifiableCollection(this.dirs);
    }

    /**
     * Try to parse Xembly command.
     * @param part Part of the command
     * @return Command or empty
     * @throws XmlContentException If fails to un-escape all XML symbols
     * @throws ParsingException If fails to parse given command
     */
    private static Optional<Callback<Directive>> parsedCommand(final String part)
        throws XmlContentException, ParsingException {
        final Optional<Callback<Directive>> cmd;
        final String trimmed = Verbs.withoutNumber(part.trim());
        if (Verbs.ARGUMENTLESS.containsKey(trimmed)) {
            cmd = Optional.of(Verbs.ARGUMENTLESS.get(trimmed));
        } else {
            final int index = Verbs.nearQuoteIndex(part);
            final String command = Verbs.withoutNumber(part.substring(0, index).trim());
            if (Verbs.SIMPLE.containsKey(command)) {
                cmd = Verbs.simpleCommand(part, command, index);
            } else if (Verbs.COMPLEX.containsKey(command)) {
                cmd = Verbs.complexCommand(part, command, index);
            } else {
                throw new ParsingException(
                    String.format("Unknown command near [%s;]", part)
                );
            }
        }
        return cmd;
    }

    /**
     * Get command without number in front.
     * @param command Command to process
     * @return Command without number in front
     */
    private static String withoutNumber(final String command) {
        final int index = command.indexOf(':');
        final String cmd;
        if (index == -1) {
            cmd = command;
        } else {
            cmd = Verbs.ltrim(command.substring(index + 1));
        }
        return cmd;
    }

    /**
     * Get index of the nearest left single or double quote in the given command.
     * @param part Part of the command to process
     * @return Index of nearest quote
     * @throws ParsingException If fails to find quotes in the part of the command
     */
    private static int nearQuoteIndex(final String part) throws ParsingException {
        final int single = part.indexOf('\'');
        final int dual = part.indexOf('"');
        if (single == -1 && dual == -1) {
            throw new ParsingException(
                String.format("Couldn't find quote in the part of the command near [%s;]", part)
            );
        }
        final int index;
        if (single != -1 && (dual == -1 || single < dual)) {
            index = single;
        } else if (single == -1 || dual < single) {
            index = dual;
        } else {
            throw new ParsingException(
                String.format("Unexpected behaviour when searching for quotes near [%s;]", part)
            );
        }
        return index;
    }

    /**
     * Try to parse command with one argument.
     * @param part Full command part
     * @param command Command itself
     * @param index Index of nearest quote
     * @return Simple command or empty
     * @throws XmlContentException If fails to un-escape all XML symbols in command argument
     * @throws ParsingException If fails to parse simple command
     */
    private static Optional<Callback<Directive>> simpleCommand(
        final String part,
        final String command,
        final int index
    ) throws XmlContentException, ParsingException {
        final char quote = part.charAt(index);
        final String[] args = part.split(String.valueOf(quote));
        final Optional<Callback<Directive>> cmd;
        if (args.length == 1) {
            cmd = Optional.empty();
        } else if (args.length == 2) {
            if (part.charAt(part.length() - 1) == quote) {
                cmd = Optional.of(
                    Verbs.SIMPLE.get(command).apply(
                        Arg.unescape(String.format("'%s'", args[1]))
                    )
                );
            } else {
                cmd = Optional.empty();
            }
        } else if (args.length == 3) {
            if (args[2].trim().isEmpty()) {
                cmd = Optional.of(
                    Verbs.SIMPLE.get(command).apply(
                        Arg.unescape(String.format("'%s'", args[1]))
                    )
                );
            } else {
                throw new ParsingException(
                    String.format("Unexpected symbols after command argument near [%s;]", part)
                );
            }
        } else {
            throw new ParsingException(
                String.format(
                    "Unexpected behaviour when searching for command arguments [%s;]", part
                )
            );
        }
        return cmd;
    }

    /**
     * Try to parse command with two arguments.
     * @param part Full command part
     * @param command Command itself
     * @param index Index of nearest quote
     * @return Complex command or empty
     * @throws XmlContentException If fails to un-escape all XML symbols in command arguments
     * @throws ParsingException If fails to parse complex command
     * @checkstyle CyclomaticComplexityCheck (100 lines)
     * @checkstyle NestedIfDepthCheck (100 lines)
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    private static Optional<Callback<Directive>> complexCommand(
        final String part,
        final String command,
        final int index
    ) throws XmlContentException, ParsingException {
        final char quote = part.charAt(index);
        final String[] args = part.split(String.valueOf(quote));
        final Optional<Callback<Directive>> cmd;
        if (args.length == 1) {
            cmd = Optional.empty();
        } else if (args.length == 2) {
            if (part.charAt(part.length() - 1) == quote) {
                throw new ParsingException(
                    String.format("Unexpected last quote near [%s;]", part)
                );
            } else {
                cmd = Optional.empty();
            }
        } else if (args.length > 2) {
            String tail = Verbs.ltrim(
                part.substring(index + part.substring(index + 1).indexOf(quote) + 2)
            );
            if (tail.isEmpty()) {
                cmd = Optional.empty();
            } else {
                if (tail.charAt(0) != ',') {
                    throw new ParsingException(
                        String.format("Comma after first argument is expected near [%s;]", part)
                    );
                }
                tail = Verbs.ltrim(tail.substring(1));
                if (tail.isEmpty()) {
                    throw new ParsingException(
                        String.format("Unexpected last semicolon near [%s;]", part)
                    );
                }
                final char first = tail.charAt(0);
                if (first != '\'' && first != '"') {
                    throw new ParsingException(
                        String.format(
                            "Single or double quote is expected after comma near [%s;]", part
                        )
                    );
                }
                tail = tail.substring(1);
                final int next = tail.indexOf(first);
                if (next == -1) {
                    cmd = Optional.empty();
                } else {
                    if (!tail.substring(next + 1).trim().isEmpty()) {
                        throw new ParsingException(
                            String.format(
                                "Unexpected symbols after second argument near [%s;]", part
                            )
                        );
                    }
                    cmd = Optional.of(
                        Verbs.COMPLEX.get(command).apply(
                            Arg.unescape(String.format("'%s'", args[1])),
                            Arg.unescape(String.format("'%s'", tail.substring(0, next)))
                        )
                    );
                }
            }
        } else {
            throw new ParsingException(
                String.format(
                    "Unexpected behaviour when searching for command arguments [%s;]", part
                )
            );
        }
        return cmd;
    }

    /**
     * Trim from left.
     * @param str String to trim
     * @return String trimmed from left
     */
    private static String ltrim(final String str) {
        int idx = 0;
        while (idx < str.length() && Character.isWhitespace(str.charAt(idx))) {
            ++idx;
        }
        return str.substring(idx);
    }
}
