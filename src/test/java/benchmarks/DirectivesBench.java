/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package benchmarks;

import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.xembly.Directives;

/**
 * Benchmark for {@link Directives}.
 *
 * @since 0.0.34
 * @checkstyle DesignForExtensionCheck (10 lines)
 * @checkstyle NonStaticMethodCheck (100 lines)
 */
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DirectivesBench {

    /**
     * Benchmark for {@link Directives#Directives(String)}.
     */
    @Benchmark
    public final void buildsLargeXml() {
        final Directives dirs = new Directives().add("root");
        for (int idx = 0; idx < 100_000; ++idx) {
            dirs.add("item").attr("idx", idx).up();
        }
    }

    /**
     * Benchmark for {@link Directives#Directives(String)}.
     */
    @Benchmark
    public final void parsesLongProgram() {
        final StringBuilder program = new StringBuilder(1000).append("ADD 'root';");
        for (int idx = 0; idx < 10_000; ++idx) {
            program.append("XPATH '/root'; ADDIF 'node';SET '")
                .append(idx).append("'; ADD 'x'; REMOVE;");
        }
        final Directives dirs = new Directives(program.toString());
        MatcherAssert.assertThat(dirs, Matchers.notNullValue());
    }
}
