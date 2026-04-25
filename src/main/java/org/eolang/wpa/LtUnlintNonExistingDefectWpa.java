/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XML;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.cactoos.io.ResourceOf;
import org.cactoos.list.ListOf;
import org.cactoos.text.IoCheckedText;
import org.cactoos.text.TextOf;

/**
 * Lint for checking `+unlint` meta to suppress non-existing defects in WPA scope.
 *
 * @since 0.0.42
 */
final class LtUnlintNonExistingDefectWpa implements Lint {

    /**
     * Lints.
     */
    private final Iterable<Lint> lints;

    /**
     * Lint names for exclusion.
     */
    private final Collection<String> excluded;

    /**
     * Ctor.
     *
     * @param lnts Lints
     */
    LtUnlintNonExistingDefectWpa(final Iterable<Lint> lnts) {
        this(lnts, new ListOf<>());
    }

    /**
     * Ctor.
     *
     * @param lnts Lints
     * @param exld Lint names to exclude
     */
    LtUnlintNonExistingDefectWpa(
        final Iterable<Lint> lnts, final Collection<String> exld
    ) {
        this.lints = lnts;
        this.excluded = exld;
    }

    @Override
    public String name() {
        return "unlint-non-existing-defect";
    }

    @Override
    public Collection<Defect> defects(final Map<String, XML> pkg) {
        return pkg.values().stream()
            .flatMap(xmir -> this.sourceDefects(xmir, this.existingDefects(pkg)).stream())
            .collect(Collectors.toList());
    }

    @Override
    public String motive() throws IOException {
        return new IoCheckedText(
            new TextOf(
                new ResourceOf(
                    String.format(
                        "org/eolang/motives/misc/%s.md", this.name()
                    )
                )
            )
        ).asString();
    }

    /**
     * Find defects for single source.
     * @param xmir Source XML
     * @param existing Existing defects map
     * @return Defects found
     */
    private Collection<Defect> sourceDefects(
        final XML xmir, final Map<XML, Map<String, List<Integer>>> existing
    ) {
        return new Xnav(xmir.inner()).path("/object/metas/meta[head='unlint']/tail")
            .map(xnav -> xnav.text().get())
            .distinct()
            .filter(new DefectMissing(existing.get(xmir), this.excluded)::apply)
            .flatMap(
                unlint -> new Xnav(xmir.inner()).path(
                    String.format(
                        "object/metas/meta[head='unlint' and tail='%s']/@line", unlint
                    )
                ).map(
                    xnav -> new Defect.Default(
                        this.name(),
                        Severity.WARNING,
                        new ProgramName(xmir).get(),
                        Integer.parseInt(xnav.text().get()),
                        String.format(
                            "Unlinting rule '%s' doesn't make sense, since there are no defects with it",
                            unlint
                        )
                    )
                )
            )
            .collect(Collectors.toList());
    }

    /**
     * Find existing defects.
     *
     * @param pkg Program package to scan
     * @return Map of existing defects
     */
    private Map<XML, Map<String, List<Integer>>> existingDefects(final Map<String, XML> pkg) {
        return pkg.values().stream().collect(
            Collectors.toMap(
                xml -> xml,
                xml -> StreamSupport.stream(this.lints.spliterator(), false)
                    .flatMap(wpl -> LtUnlintNonExistingDefectWpa.defectStream(wpl, pkg))
                    .collect(
                        Collectors.groupingBy(
                            Defect::rule,
                            Collectors.mapping(Defect::line, Collectors.toList())
                        )
                    )
            )
        );
    }

    /**
     * Produce a stream of defects from a lint, wrapping IO errors.
     * @param lint The lint
     * @param pkg The package
     * @return Stream of defects
     */
    private static Stream<Defect> defectStream(final Lint lint, final Map<String, XML> pkg) {
        try {
            return lint.defects(pkg).stream();
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format(
                    "IO operation failed while linting program with %s",
                    lint.name()
                ),
                exception
            );
        }
    }
}
