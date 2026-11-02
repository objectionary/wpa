/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link LtAtomIsNotUnique}.
 *
 * @since 0.0.31
 */
final class LtAtomIsNotUniqueTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void findsNoDuplicatesInLargePackage() {
        final int count = 30;
        final Map<String, XML> pkg = new HashMap<>(count);
        IntStream.range(0, count).forEach(
            idx -> pkg.put(
                String.format("obj%d", idx),
                new XMLDocument(
                    String.format(
                        "<object><o line='1' name='obj%d'><o name='λ'/></o></object>",
                        idx
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            "Large package with unique atoms must produce no defects within timeout",
            new LtAtomIsNotUnique().defects(pkg),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesAtomDuplicatesWithoutPackages() throws Exception {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo.",
                                "",
                                "[] > foo",
                                "  [] > at /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Bar, but its foo.",
                                "",
                                "[] > foo",
                                "  [] > at /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void allowsSameAtomsWithDifferentFqns() throws Exception {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Fuz.",
                                "",
                                "[] > fuz",
                                "  [] > test /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Its bar.",
                                "",
                                "[] > bar",
                                "  [] > test /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesAtomDuplicatesWithinSamePackage() throws Exception {
        MatcherAssert.assertThat(
            "Defects should be reported",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App, XYZ packaged.",
                                "",
                                "+package xyz",
                                "",
                                "[] > app",
                                "  [] > foo /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App duplicate.",
                                "",
                                "+package xyz",
                                "",
                                "[] > app",
                                "  [] > foo /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void catchesNestedAtomDuplicates() throws Exception {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "nested",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Top object with nested atoms inside.",
                                "",
                                "[] > top",
                                "  [] > test /A",
                                "    ? > ^",
                                "  [] > f",
                                "    [] > a",
                                "      [] > bar",
                                "        [] > abr /A",
                                "          ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "nested-dup",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Top object with nested atoms inside, but without test atom.",
                                "",
                                "[] > top",
                                "  [] > f",
                                "    [] > a",
                                "      [] > bar",
                                "        [] > abr /A",
                                "          ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void allowsSameNameInDifferentPackages() throws Exception {
        MatcherAssert.assertThat(
            "Defects aren't empty, but they should",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "x",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# X.",
                                "",
                                "[] > app",
                                "  [] > x /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "x-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# X, but its packaged.",
                                "",
                                "+package xyz",
                                "",
                                "[] > app",
                                "  [] > x /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsUniqueAtoms() throws Exception {
        MatcherAssert.assertThat(
            "Defects aren't empty, but they should",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "a",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# A.",
                                "",
                                "[attr] > a",
                                "  [] > x /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "b",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# B.",
                                "",
                                "[attr] > b",
                                "  [] > y /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsAtomsWithUniqueFqnsInSingleFile() throws Exception {
        MatcherAssert.assertThat(
            "Defects aren't empty, but they should",
            new LtAtomIsNotUnique().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "spb",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Spb.",
                                "",
                                "[attr] > spb",
                                "  [] > mow /A",
                                "    ? > ^",
                                "  [] > sfo /A",
                                "    ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsSameNameWithPackageDifference() throws Exception {
        MatcherAssert.assertThat(
            "Defects aren't empty, but they should",
            new LtAtomIsNotUnique().defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "abc",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# ABC.",
                                "",
                                "+package a.b.c",
                                "",
                                "[] > abc /A",
                                "  ? > ^"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "abc-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# ABC.",
                                "",
                                "+package a.b.z",
                                "",
                                "[] > abc /A",
                                "  ? > ^"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }
}
