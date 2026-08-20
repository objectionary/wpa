/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XMLDocument;

/**
 * Single atom occurrence extracted from a transformed XMIR source.
 * @since 0.0.31
 */
final class AtomOccurrence {

    /**
     * Transformed XMIR source containing this atom.
     */
    private final Xnav source;

    /**
     * Fully qualified name of the atom.
     */
    private final String atomfqn;

    /**
     * Line number of the atom in the source.
     */
    private final int line;

    /**
     * Ctor.
     * @param src Transformed XMIR source
     * @param fqn Fully qualified atom name
     * @param lno Line number
     */
    AtomOccurrence(final Xnav src, final String fqn, final int lno) {
        this.source = src;
        this.atomfqn = fqn;
        this.line = lno;
    }

    /**
     * Fully qualified name of the atom.
     * @return FQN string
     */
    String fqn() {
        return this.atomfqn;
    }

    /**
     * Create a defect reporting this atom as a duplicate of another.
     * @param original The occurrence where the atom was originally defined
     * @return Defect
     */
    Defect defect(final AtomOccurrence original) {
        return new Defect.Default(
            "atom-is-not-unique",
            Severity.ERROR,
            new ProgramName(new XMLDocument(this.source.node())).get(),
            this.line,
            String.format(
                "Atom with FQN \"%s\" is duplicated, original was found in \"%s\"",
                this.atomfqn,
                new ProgramName(new XMLDocument(original.source.node())).get()
            )
        );
    }
}
