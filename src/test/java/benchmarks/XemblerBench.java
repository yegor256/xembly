/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package benchmarks;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.Xembler;

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
public class XemblerBench {

    /**
     * Benchmark for {@link Directives#Directives(String)}.
     * @throws Exception If fails
     */
    @Benchmark
    public final void modifiesDom() throws Exception {
        final StringBuilder program = new StringBuilder(1000)
            .append("ADD 'root';");
        for (int idx = 0; idx < 50_000; ++idx) {
            program.append(
                "XPATH '/root'; ADDIF 'node';ADD 'temp'; REMOVE;SET '"
            ).append(idx).append("';");
        }
        final Directives dirs = new Directives(program.toString());
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/root/node[.='49999']")
        );
    }
}
