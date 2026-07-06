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
import java.util.stream.IntStream;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * Object is not unique.
 * @since 0.0.30
 */
final class LtObjectIsNotUnique implements Lint {

    /**
     * Lint name.
     */
    private static final String NAME = "object-is-not-unique";

    @Override
    public String name() {
        return LtObjectIsNotUnique.NAME;
    }

    @Override
    public Collection<Defect> defects(final Map<String, XML> pkg) {
        return pkg.values().stream()
            .map(LtObjectIsNotUnique.SourceObject::from)
            .collect(Collectors.groupingBy(LtObjectIsNotUnique.SourceObject::key))
            .values().stream()
            .filter(group -> group.size() > 1)
            .flatMap(group -> LtObjectIsNotUnique.defects(group).stream())
            .collect(Collectors.toList());
    }

    @Override
    public String motive() throws IOException {
        return new UncheckedText(
            new TextOf(
                new ResourceOf(
                    String.format(
                        "org/eolang/motives/errors/%s.md", this.name()
                    )
                )
            )
        ).asString();
    }

    /**
     * Create defects for a group of sources with the same object name in the same package.
     * @param group Sources with duplicate object names
     * @return Defects found
     */
    private static Collection<Defect> defects(
        final List<LtObjectIsNotUnique.SourceObject> group
    ) {
        return IntStream.range(0, group.size()).boxed().flatMap(
            idx -> IntStream.range(0, group.size())
                .filter(other -> other != idx)
                .mapToObj(other -> group.get(idx).defect(group.get(other)))
        ).collect(Collectors.toList());
    }

    /**
     * Top-level object extracted from a single XMIR source file.
     * @since 0.0.30
     */
    private static final class SourceObject {

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

        // @checkstyle ParameterNumberCheck (5 lines)
        private SourceObject(
            final String pkg, final String name, final int line, final XML source
        ) {
            this.pkg = pkg;
            this.name = name;
            this.line = line;
            this.source = source;
        }

        /**
         * Grouping key combining package and object name.
         * @return Key as a list of two strings
         */
        private List<String> key() {
            return List.of(this.pkg, this.name);
        }

        /**
         * Program name for use in defect messages.
         * @return Program name
         */
        private String programName() {
            return new ProgramName(this.source).get();
        }

        /**
         * Create a defect reporting this object as a duplicate of the original.
         * @param original The source where the object was originally defined
         * @return Defect
         */
        private Defect defect(final LtObjectIsNotUnique.SourceObject original) {
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

        /**
         * Build a SourceObject by extracting fields from an XMIR file.
         * @param xmir Source XMIR
         * @return SourceObject
         */
        private static LtObjectIsNotUnique.SourceObject from(final XML xmir) {
            final Xnav xml = new Xnav(xmir.inner());
            return new LtObjectIsNotUnique.SourceObject(
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
    }
}
