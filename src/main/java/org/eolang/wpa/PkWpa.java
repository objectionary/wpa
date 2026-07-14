/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import java.util.Collection;
import java.util.Iterator;
import org.cactoos.func.Chained;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Shuffled;
import org.cactoos.list.ListOf;

/**
 * A collection of lints for Whole Program Analysis (WPA),
 * provided by the {@link Program} class.
 *
 * <p>This class is thread-safe.</p>
 *
 * @since 0.1.0
 * @todo #62:90min Reconsider the architectural placement of LtUnlintNonExistingDefectWpa.
 *  This lint is a second-order lint (lint of lints): it validates +unlint annotations
 *  by running other active lints and inspecting their results. This makes it
 *  fundamentally different from peer lints in WpaLints and forces PkWpa to treat
 *  it as a special case. Consider whether it belongs in the Lint hierarchy at all,
 *  or should be extracted into a separate post-processing abstraction that operates
 *  on already-computed defect results rather than embedding itself among peer lints.
 */
final class PkWpa implements Iterable<Lint> {

    /**
     * WPA lints.
     */
    private static final Iterable<Lint> WPA = new WpaLints();

    /**
     * Lints.
     */
    private final Iterable<Lint> lints;

    /**
     * Excluded lint names.
     */
    private final Collection<String> excluded;

    /**
     * Default ctor.
     */
    PkWpa() {
        this(PkWpa.WPA, new ListOf<>());
    }

    /**
     * Ctor.
     * @param lints Lints
     * @param exld Excluded lint names
     */
    PkWpa(final Iterable<Lint> lints, final Collection<String> exld) {
        this.lints = lints;
        this.excluded = exld;
    }

    @Override
    public Iterator<Lint> iterator() {
        return new Shuffled<>(
            new Mapped<Lint>(
                new Chained<>(
                    LtWpaUnlint::new,
                    LtDfSticky::new
                ),
                new WithoutLints(
                    new Joined<Lint>(
                        this.lints,
                        new ListOf<>(
                            new LtUnlintNonExistingDefectWpa(this.lints, PkWpa.WPA, new ListOf<>())
                        )
                    ),
                    this.excluded
                )
            )
        ).iterator();
    }
}
