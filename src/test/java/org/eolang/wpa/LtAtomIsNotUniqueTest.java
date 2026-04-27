/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.jcabi.xml.XML;
import matchers.DefectMatcher;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LtAtomIsNotUnique}.
 * @since 0.0.31
 */
final class LtAtomIsNotUniqueTest {

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
                                "[] > foo",
                                "  [] > at ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Bar, but its foo.",
                                "[] > foo",
                                "  [] > at ?"
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
                                "[] > fuz",
                                "  [] > test ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Its bar.",
                                "[] > bar",
                                "  [] > test ?"
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
                                "+package xyz",
                                "",
                                "# App, XYZ packaged.",
                                "[] > app",
                                "  [] > foo ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+package xyz",
                                "",
                                "# App duplicate.",
                                "[] > app",
                                "  [] > foo ?"
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
                                "[] > top",
                                "  [] > test ?",
                                "  [] > f",
                                "    [] > a",
                                "      [] > bar",
                                "        [] > abr ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "nested-dup",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Top object with nested atoms inside, but without test atom.",
                                "[] > top",
                                "  [] > f",
                                "    [] > a",
                                "      [] > bar",
                                "        [] > abr ?"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(2)
        );
    }

    @Test
    void catchesDuplicatesInSingleFile() throws Exception {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtAtomIsNotUnique().defects(
                new MapOf<>(
                    new MapEntry<>(
                        "dup",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Dups.",
                                "[attr] > dups",
                                "  [] > foo ?",
                                "  [] > @",
                                "  [] > foo ?"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.allOf(
                Matchers.<Defect>iterableWithSize(Matchers.greaterThan(0)),
                Matchers.<Defect>everyItem(new DefectMatcher())
            )
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
                                "[] > app",
                                "  [] > x ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "x-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+package xyz",
                                "",
                                "# X, but its packaged.",
                                "[] > app",
                                "  [] > x ?"
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
                                "[attr] > a",
                                "  [] > x ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "b",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# B.",
                                "[attr] > b",
                                "  [] > y ?"
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
                                "[attr] > spb",
                                "  [] > mow ?",
                                "  [] > sfo ?"
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
                                "+package a.b.c",
                                "",
                                "# ABC.",
                                "[] > abc ?"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "abc-packaged",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "+package a.b.z",
                                "",
                                "# ABC.",
                                "[] > abc ?"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }
}
