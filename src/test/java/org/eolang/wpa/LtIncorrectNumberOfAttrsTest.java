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
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LtIncorrectNumberOfAttrs}.
 * @since 0.0.43
 */
final class LtIncorrectNumberOfAttrsTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void findsNoDefectsInLargeConsistentPackage() throws IOException {
        final int count = 500;
        final Map<String, XML> pkg = new HashMap<>(count);
        pkg.put(
            "helper",
            new XMLDocument(
                String.join(
                    System.lineSeparator(),
                    "<object>",
                    "  <o name='helper'>",
                    "    <o base='∅' name='a'/>",
                    "  </o>",
                    "</object>"
                )
            )
        );
        for (int idx = 0; idx < count - 1; ++idx) {
            pkg.put(
                String.format("user%d", idx),
                new XMLDocument(
                    String.join(
                        System.lineSeparator(),
                        "<object>",
                        String.format("  <o name='user%d'>", idx),
                        "    <o base='Φ.helper' line='1'>",
                        "      <o base='int'/>",
                        "    </o>",
                        "  </o>",
                        "</object>"
                    )
                )
            );
        }
        MatcherAssert.assertThat(
            "Large consistent package must find no defects within timeout",
            new LtIncorrectNumberOfAttrs().defects(pkg),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesIncorrectNumberOfAttributes() throws IOException {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo with one attribute.",
                                "[a] > foo"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App uses foo with two attributes instead.",
                                "[a b] > app",
                                "  foo a b > @"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(Matchers.greaterThan(0))
        );
    }

    @Test
    void allowsCorrectNumberOfAttributes() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "a",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# A with one attribute.",
                                "[pos] > a"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App uses a with correct number of arguments.",
                                "[] > app",
                                "  a 0"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsCorrectAttributesCountInSameFile() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "single",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# A with one attribute.",
                                "[pos] > a",
                                "",
                                "# B uses A with one attribute",
                                "[] > b",
                                "  a 52"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void ignoresUndefinedObjectInPackage() throws IOException {
        MatcherAssert.assertThat(
            "Defects should empty, since object is not defined in provided package",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "hello",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Say hello.",
                                "[content] > hello"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App uses undeclared object.",
                                "[] > app",
                                "  hello \"f\"",
                                "  bye 0x1"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void understandsPackages() throws IOException {
        MatcherAssert.assertThat(
            "Defects should be empty, since object is expected to be packaged",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "foo-unpackaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# An unpackaged foo.",
                                "[] > foo"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "foo-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+package f",
                                "",
                                "# Packaged foo in f.",
                                "[bar] > foo"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "res",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+alias f.foo",
                                "",
                                "# Resolver application that uses f.foo.",
                                "[args] > app",
                                "  foo args > @"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsCorrectAttributesInVerticalApplication() throws IOException {
        MatcherAssert.assertThat(
            "Defects should be empty, since attributes are correct in vertical application",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "a",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# A with one attribute.",
                                "[pos] > a",
                                ""
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "b",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# B with two attributes.",
                                "[left right] > b"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "usage",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Usage of A and B objects with vertical application.",
                                "[] > app",
                                "  b",
                                "    0",
                                "    a 0"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesInCorrectAttributesInVerticalApplication() throws IOException {
        MatcherAssert.assertThat(
            "Defects should not be empty, since attributes are passed incorrectly",
            new LtIncorrectNumberOfAttrs().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "x",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# X with one attribute.",
                                "[pos sigma] > x",
                                ""
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "y",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Y with two attributes.",
                                "[left right] > y"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "xy-app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Usage of X and Y objects with vertical application.",
                                "[] > app",
                                "  y > @",
                                "    1",
                                "    x 0"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(Matchers.greaterThan(0))
        );
    }
}
