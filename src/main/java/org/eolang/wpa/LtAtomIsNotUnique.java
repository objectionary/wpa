/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.UncheckedInput;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * All atom FQNs in the entire scope of EO program must be unique.
 * This lint firstly transforms the original XMIR into XMIR that contains `@fqn`
 * attributes for each atom `o`, and then lints it.
 * @since 0.0.31
 */
final class LtAtomIsNotUnique implements Lint {

    /**
     * Stylesheet for adding `@fqn` attribute for atoms.
     */
    private final XSL pre;

    /**
     * Ctor.
     * @checkstyle ConstructorsCodeFreeCheck (10 lines)
     */
    LtAtomIsNotUnique() {
        this(
            new XSLDocument(
                new UncheckedInput(
                    new ResourceOf("org/eolang/funcs/atom-fqns.xsl")
                ).stream()
            ).with(new ClasspathSources())
        );
    }

    /**
     * Ctor.
     * @param sheet Sheet
     */
    LtAtomIsNotUnique(final XSL sheet) {
        this.pre = sheet;
    }

    @Override
    public String name() {
        return "atom-is-not-unique";
    }

    @Override
    public Collection<Defect> defects(final Map<String, XML> pkg) {
        return pkg.values().stream()
            .map(this.pre::transform)
            .map(xmir -> new Xnav(xmir.inner()))
            .flatMap(LtAtomIsNotUnique::occurrences)
            .collect(Collectors.groupingBy(LtAtomIsNotUnique.AtomOccurrence::fqn))
            .values().stream()
            .filter(group -> group.size() > 1)
            .flatMap(group -> LtAtomIsNotUnique.groupDefects(group).stream())
            .collect(Collectors.toList());
    }

    @Override
    public String motive() {
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
     * Create defects for a group of occurrences that share the same atom FQN.
     * @param group Occurrences with the same FQN
     * @return Defects
     */
    private static Collection<Defect> groupDefects(
        final List<LtAtomIsNotUnique.AtomOccurrence> group
    ) {
        return IntStream.range(0, group.size()).boxed().flatMap(
            idx -> IntStream.range(0, group.size())
                .filter(other -> other != idx)
                .mapToObj(other -> group.get(idx).defect(group.get(other)))
        ).collect(Collectors.toList());
    }

    /**
     * Extract all atom occurrences from a transformed XMIR source.
     * @param xmir Transformed XMIR (with @fqn attributes added by XSL)
     * @return Stream of atom occurrences with FQN and line already captured
     */
    private static Stream<LtAtomIsNotUnique.AtomOccurrence> occurrences(final Xnav xmir) {
        final String pack;
        if (xmir.path("/object/metas/meta[head='package']").count() == 1L) {
            pack = xmir.one("/object/metas/meta[head='package']/tail").text().get();
        } else {
            pack = "";
        }
        return xmir.path("//o[@fqn]").map(
            atom -> {
                final String local = atom.attribute("fqn").text().get();
                final String full;
                if (pack.isEmpty()) {
                    full = String.format("Ф.%s", local);
                } else {
                    full = String.format("Ф.%s.%s", pack, local);
                }
                return new LtAtomIsNotUnique.AtomOccurrence(
                    xmir,
                    full,
                    Integer.parseInt(atom.attribute("line").text().orElse("0"))
                );
            }
        );
    }

    /**
     * Single atom occurrence extracted from a transformed XMIR source.
     * @since 0.0.31
     */
    private static final class AtomOccurrence {

        /**
         * Transformed XMIR source containing this atom.
         */
        private final Xnav source;

        /**
         * Fully qualified name of the atom.
         */
        private final String atomFqn;

        /**
         * Line number of the atom in the source.
         */
        private final int line;

        /**
         * Ctor.
         * @param src Transformed XMIR source
         * @param fqn Fully qualified atom name
         * @param ln Line number
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        AtomOccurrence(final Xnav src, final String fqn, final int ln) {
            this.source = src;
            this.atomFqn = fqn;
            this.line = ln;
        }

        /**
         * Fully qualified name of the atom.
         * @return FQN string
         */
        String fqn() {
            return this.atomFqn;
        }

        /**
         * Create a defect reporting this atom as a duplicate of another.
         * @param original The occurrence where the atom was originally defined
         * @return Defect
         */
        Defect defect(final LtAtomIsNotUnique.AtomOccurrence original) {
            return new Defect.Default(
                "atom-is-not-unique",
                Severity.ERROR,
                new ProgramName(new XMLDocument(this.source.node())).get(),
                this.line,
                String.format(
                    "Atom with FQN \"%s\" is duplicated, original was found in \"%s\"",
                    this.atomFqn,
                    new ProgramName(new XMLDocument(original.source.node())).get()
                )
            );
        }
    }
}
