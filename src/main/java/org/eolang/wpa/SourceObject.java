/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XML;
import java.util.List;

/**
 * Top-level object extracted from a single XMIR source file.
 * @since 0.0.30
 */
final class SourceObject {

    /**
     * Package name, empty string if no package is defined.
     */
    private final String pkg;

    /**
     * Top-level object name.
     */
    private final String name;

    /**
     * Line number of the object definition.
     */
    private final int line;

    /**
     * Source XML for program name resolution via {@link ProgramName}.
     */
    private final XML source;

    SourceObject(
        final String pkg, final String name, final int line, final XML source
    ) {
        this.pkg = pkg;
        this.name = name;
        this.line = line;
        this.source = source;
    }

    Defect defect(final SourceObject original) {
        return new Defect.Default(
            LtObjectIsNotUnique.NAME,
            Severity.ERROR,
            this.programName(),
            this.line,
            String.format(
                "The object name \"%s\" is not unique, original object was found in \"%s\"",
                this.name,
                original.programName()
            )
        );
    }

    List<String> key() {
        return List.of(this.pkg, this.name);
    }

    static SourceObject from(final XML xmir) {
        final Xnav xml = new Xnav(xmir.inner());
        return new SourceObject(
            xml.path("/object/metas/meta[head='package']/tail")
                .findFirst()
                .flatMap(Xnav::text)
                .orElse(""),
            xml.path("/object/o/@name")
                .findFirst()
                .flatMap(Xnav::text)
                .orElse(""),
            Integer.parseInt(
                xml.path("/object/o/@line")
                    .findFirst()
                    .flatMap(Xnav::text)
                    .orElse("0")
            ),
            xmir
        );
    }

    private String programName() {
        return new ProgramName(this.source).get();
    }
}
