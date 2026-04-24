/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XML;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.cactoos.list.ListOf;

/**
 * Internal helper that applies {@code +unlint} meta filtering to a single
 * defect discovered in the given XMIR.
 *
 * @since 0.0.1
 */
final class LtUnlint {

    /**
     * Line number to unlint.
     */
    private static final Pattern LINE_NUMBER = Pattern.compile(".*:\\d+$");

    /**
     * The defect to filter.
     */
    private final Defect defect;

    /**
     * Ctor.
     * @param dft The defect to filter
     */
    LtUnlint(final Defect dft) {
        this.defect = dft;
    }

    /**
     * Return the defect unless it is suppressed by {@code +unlint} meta in the
     * given XMIR.
     * @param xmir The XMIR that owns the defect
     * @return Defects after filtering (zero or one)
     */
    Collection<Defect> defects(final XML xmir) {
        final Collection<Defect> defects = new ArrayList<>(0);
        final String lname = this.defect.rule();
        final List<Integer> problematic = new ListOf<>(this.defect.line());
        final List<String> granular = new Xnav(xmir.inner()).path(
            String.format(
                "/object/metas/meta[head='unlint' and (tail='%s' or starts-with(tail, '%s:'))]/tail",
                lname, lname
            )
        ).map(xnav -> xnav.text().get()).collect(java.util.stream.Collectors.toList());
        final boolean global = !granular.isEmpty();
        final AtomicBoolean added = new AtomicBoolean(false);
        granular.forEach(
            unlint -> {
                if (unlint.matches(String.format("%s:\\d+-\\d+", lname))) {
                    problematic.removeIf(new UnlintInRange(unlint));
                } else if (LtUnlint.LINE_NUMBER.matcher(unlint).matches()) {
                    final List<String> split = new ListOf<>(unlint.split(":"));
                    final int lineno = Integer.parseInt(split.get(1));
                    problematic.removeIf(line -> line == lineno);
                } else {
                    problematic.clear();
                }
            }
        );
        problematic.forEach(
            line -> {
                if (line != 0 && this.defect.line() == line) {
                    defects.add(this.defect);
                    added.set(true);
                }
            }
        );
        if (!added.get() && !global) {
            defects.add(this.defect);
        }
        return defects;
    }
}
