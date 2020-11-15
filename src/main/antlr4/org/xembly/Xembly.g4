/*
 * Copyright (c) 2013-2020, xembly.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the xembly.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
grammar Xembly;

@lexer::header {
    import java.util.Collection;
    import java.util.LinkedList;
}

@parser::header {
    import java.util.Collection;
    import java.util.LinkedList;
}

directives returns [Collection<Directive> ret]
    @init { $ret = new LinkedList<Directive>(); }
    :
    (
        (label COLON)?
        directive
        SEMICOLON
        { $ret.add($directive.ret); }
    )*
    EOF
    ;

directive returns [Directive ret]
    :
    'XPATH' argument
    {
        try {
            $ret = new XpathDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'SET' argument
    {
        try {
            $ret = new SetDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'XSET' argument
    {
        try {
            $ret = new XsetDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'ATTR' name=argument COMMA value=argument
    {
        try {
            $ret = new AttrDirective($name.ret.toString(), $value.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'ADD' argument
    {
        try {
            $ret = new AddDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'ADDIF' argument
    {
        try {
            $ret = new AddIfDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'REMOVE'
    {
        $ret = new RemoveDirective();
    }
    |
    'STRICT' argument
    {
        $ret = new StrictDirective(Integer.parseInt($argument.ret.toString()));
    }
    |
    'UP'
    {
        $ret = new UpDirective();
    }
    |
    'PI' target=argument data=argument
    {
        try {
            $ret = new PiDirective($target.ret.toString(), $data.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'PUSH'
    {
        $ret = new PushDirective();
    }
    |
    'POP'
    {
        $ret = new PopDirective();
    }
    |
    'CDATA' argument
    {
        try {
            $ret = new CdataDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    'COMMENT' argument
    {
        try {
            $ret = new CommentDirective($argument.ret.toString());
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    ;

argument returns [Object ret]
    :
    TEXT
    { $ret = $TEXT.text; }
    ;

label
    :
    DIGIT+
    ;

DIGIT: '0'..'9';
COMMA: ',';
COLON: ':';
SEMICOLON: ';';
TEXT
    :
    '"' (~'"')* '"'
    {
        try {
            this.setText(Arg.unescape(this.getText()));
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    |
    '\'' (~'\'')* '\''
    {
        try {
            this.setText(Arg.unescape(this.getText()));
        } catch (final XmlContentException ex) {
            throw new ParsingException(ex);
        }
    }
    ;
SPACE
    :
    ( ' ' | '\t' | '\n' | '\r' )+
    { skip(); }
    ;
