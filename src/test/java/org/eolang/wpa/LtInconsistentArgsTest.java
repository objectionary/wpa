/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LtInconsistentArgs}.
 * @since 0.0.41
 */
final class LtInconsistentArgsTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void findsInconsistenciesInLargePackage() throws IOException {
        final int count = 500;
        final Map<String, XML> pkg = new HashMap<>(count);
        for (int idx = 0; idx < count; ++idx) {
            final String args = IntStream.rangeClosed(1, idx + 1)
                .mapToObj(i -> "      <o base='int'/>")
                .collect(Collectors.joining("\n"));
            pkg.put(
                String.format("obj%d", idx),
                new XMLDocument(
                    String.join(
                        "\n",
                        "<object>",
                        String.format("  <o name='obj%d'>", idx),
                        "    <o base='helper' name='x'>",
                        args,
                        "    </o>",
                        "  </o>",
                        "</object>"
                    )
                )
            );
        }
        MatcherAssert.assertThat(
            "Large package with inconsistent args must detect defects within timeout",
            new LtInconsistentArgs().defects(pkg),
            Matchers.iterableWithSize(Matchers.greaterThan(0))
        );
    }

    @Test
    void catchesArgumentsInconsistency() throws IOException {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtInconsistentArgs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo",
                                "[] > foo",
                                "  bar 42 > x",
                                "  bar 1 2 3 > y"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void allowsConsistentArgumentsPassing() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtInconsistentArgs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# This is app",
                                "[] > app",
                                "  foo 42 > x",
                                "  foo 52 > spb"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesInconsistencyAcrossSources() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not caught across multiple sources, but they should",
            new LtInconsistentArgs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App",
                                "[] > app",
                                "  f 42 > x"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "main",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Main",
                                "[] > main",
                                "  f 1 2 3 > y"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void allowsConsistentArgumentsAcrossSources() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtInconsistentArgs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "fizz",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Fizz",
                                "[] > fizz",
                                "  f 42 > x"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "buzz",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Buzz",
                                "[a] > main",
                                "  f a > x"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesInconsistencyAcrossSourcesWithAlias() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not caught across multiple sources with alias, but they should",
            new LtInconsistentArgs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "text-fqn",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App",
                                "[] > app",
                                "  Q.org.eolang.txt.text \"f\" \"y\" > x"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "text-alias",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+alias org.eolang.txt.text",
                                "# Main",
                                "[] > main",
                                "  text \"f\" > y"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }
}
