/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.jcabi.xml.XML;
import java.util.Map;
import org.cactoos.func.Chained;
import org.cactoos.iterable.IterableEnvelope;
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
 */
final class PkWpa extends IterableEnvelope<Lint> {

    /**
     * WPA lints.
     */
    private static final Iterable<Lint> WPA = new WpaLints();

    /**
     * Default ctor.
     */
    PkWpa() {
        this(PkWpa.WPA);
    }

    /**
     * Ctor.
     * @param lints Lints
     */
    PkWpa(final Iterable<Lint> lints) {
        super(
            new Shuffled<>(
                new Mapped<Lint>(
                    new Chained<>(
                        LtWpaUnlint::new,
                        LtDfSticky::new
                    ),
                    new Joined<Lint>(
                        lints,
                        new ListOf<>(
                            new LtUnlintNonExistingDefectWpa(lints)
                        )
                    )
                )
            )
        );
    }
}
