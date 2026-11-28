/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LtUnlintNonExistingDefectWpa}.
 *
 * @since 0.0.42
 */
final class LtUnlintNonExistingDefectWpaTest {

    @Test
    void allowsUnlintingExistingWpaDefects() throws IOException {
        MatcherAssert.assertThat(
            "Lint should not complain, since program has WPA defects",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtObjectIsNotUnique()),
                new ListOf<>()
            ).defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo.",
                                "",
                                "+unlint object-is-not-unique",
                                "",
                                "[] > foo"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Bar.",
                                "",
                                "+unlint object-is-not-unique",
                                "",
                                "[] > foo"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesUnlintOfNonExistingWpaDefects() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should be, since +unlint unlints non-existing defect",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtObjectIsNotUnique()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    "bar",
                    new EoSyntax(
                        String.join(
                            System.lineSeparator(),
                            "# Bar",
                            "",
                            "+unlint object-is-not-unique",
                            "",
                            "[] > bar"
                        )
                    ).parsed()
                )
            ),
            Matchers.hasSize(Matchers.greaterThan(0))
        );
    }

    @Test
    void allowsNoUnlints() {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtObjectIsNotUnique()),
                new ListOf<>()
            ).defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "f",
                        new XMLDocument(
                            String.join(
                                System.lineSeparator(),
                                "<object>",
                                "  <o name='f'/>",
                                "</object>"
                            )
                        )
                    ),
                    new MapEntry<>(
                        "fa",
                        new XMLDocument(
                            String.join(
                                System.lineSeparator(),
                                "<object>",
                                "  <o name='fa'/>",
                                "</object>"
                            )
                        )
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void doesNotReportNonWpaUnlintRulesWithWpaSupplied() throws IOException {
        MatcherAssert.assertThat(
            "Non-WPA unlint rule should not be flagged even when WPA lints are supplied",
            new LtUnlintNonExistingDefectWpa(
                new PkWpa(),
                new ListOf<>()
            ).defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "f",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# E tests.",
                                "",
                                "+unlint unit-test-without-live-file",
                                "",
                                "[] > f"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>("e", new XMLDocument("<object><o name='e'/></object>"))
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void doesNotReportNonWpaUnlintRules() throws IOException {
        MatcherAssert.assertThat(
            "Non-WPA unlint rules should not be flagged as unlint-non-existing-defect",
            new LtUnlintNonExistingDefectWpa(
                new WpaLints(),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    "foo",
                    new EoSyntax(
                        String.join(
                            System.lineSeparator(),
                            "# Foo.",
                            "",
                            "+unlint redundant-object",
                            "",
                            "[] > foo"
                        )
                    ).parsed()
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void allowsExistingUnlintWithLineNumber() throws IOException {
        MatcherAssert.assertThat(
            "An existing defect should be able to be unlinted with line number",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtUnlintNonExistingDefectWpaTest.LtEveryLine()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo.",
                                "",
                                "+unlint every-line:7",
                                "",
                                "[] > foo",
                                "  bar 42 > x",
                                "  bar 42 52 > y"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesNonExistingUnlintWithLineNumber() throws IOException {
        MatcherAssert.assertThat(
            "Non existing defect with line number should be reported",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtUnlintNonExistingDefectWpaTest.LtEveryLine()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    new MapEntry<>(
                        "app",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# App.",
                                "",
                                "+unlint every-line:26",
                                "",
                                "[] > app",
                                "  tee 42 > x",
                                "  tee 42 52 > y"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.hasSize(Matchers.greaterThan(0))
        );
    }

    @Test
    void allowsUnlintForDefectsInTheLineRange() throws IOException {
        MatcherAssert.assertThat(
            "Defects are not empty, but they should",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtUnlintNonExistingDefectWpaTest.LtEveryLine()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    "main",
                    new EoSyntax(
                        String.join(
                            System.lineSeparator(),
                            "# Main.",
                            "",
                            "+unlint every-line:6-8",
                            "",
                            "[] > main",
                            "  fork > parent",
                            "  fork parent > child",
                            "  fork child parent > subchild"
                        )
                    ).parsed()
                )
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void catchesUnlintWithOutOfRangeLines() throws IOException {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtUnlintNonExistingDefectWpaTest.LtEveryLine()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    "main",
                    new EoSyntax(
                        String.join(
                            System.lineSeparator(),
                            "# Semaphore.",
                            "",
                            "+unlint every-line:13-45",
                            "",
                            "[] > sem",
                            "  p 1 > lock-one",
                            "  p 1 1 > lock-more"
                        )
                    ).parsed()
                )
            ),
            Matchers.iterableWithSize(1)
        );
    }

    @Test
    void catchesUnlintWithSomeLineOutOfRange() throws IOException {
        MatcherAssert.assertThat(
            "Defects are empty, but they should not",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtUnlintNonExistingDefectWpaTest.LtEveryLine()),
                new ListOf<>()
            ).defects(
                new MapOf<>(
                    "main",
                    new EoSyntax(
                        String.join(
                            System.lineSeparator(),
                            "# Object that represents Jeffrey from a real world.",
                            "",
                            "+unlint every-line:2-6",
                            "",
                            "[] > jeff",
                            "  send 0 > content",
                            "  send content \"boss@google.com\" > emailed",
                            "  send emailed > @"
                        )
                    ).parsed()
                )
            ),
            Matchers.iterableWithSize(1)
        );
    }

    @Test
    void allowsValidUnlintRangeWithSameRuleDefectInAnotherSource() throws IOException {
        MatcherAssert.assertThat(
            "It must have no defects",
            new LtUnlintNonExistingDefectWpa(
                new ListOf<>(new LtIncorrectAlias())
            ).defects(
                new MapOf<String, XML>(
                    new MapEntry<>(
                        "foo",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Foo.",
                                "",
                                "+unlint incorrect-alias:4-4",
                                "+alias Q.missing",
                                "",
                                "[] > foo"
                            )
                        ).parsed()
                    ),
                    new MapEntry<>(
                        "bar",
                        new EoSyntax(
                            String.join(
                                System.lineSeparator(),
                                "# Bar.",
                                "",
                                "+alias Q.missing",
                                "[] > bar"
                            )
                        ).parsed()
                    )
                )
            ),
            Matchers.emptyIterable()
        );
    }

    /**
     * Lint that complains on every line with an object that has a base.
     *
     * @since 0.1.0
     */
    private static final class LtEveryLine implements Lint {

        @Override
        public String name() {
            return "every-line";
        }

        @Override
        public Collection<Defect> defects(final Map<String, XML> pkg) {
            return pkg.values().stream().flatMap(
                xmir -> new Xnav(xmir.inner()).path("//o[@base and @line]").map(
                    obj -> new Defect.Default(
                        this.name(),
                        Severity.WARNING,
                        new ProgramName(xmir).get(),
                        Integer.parseInt(obj.attribute("line").text().get()),
                        "Every line is a defect"
                    )
                )
            ).collect(Collectors.toList());
        }

        @Override
        public String motive() {
            return "nothing";
        }
    }
}
