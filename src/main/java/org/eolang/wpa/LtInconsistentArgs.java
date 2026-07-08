/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.github.lombrozo.xnav.Xnav;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cactoos.io.ResourceOf;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.w3c.dom.Node;

/**
 * Lint for checking arguments' inconsistency provided to the objects.
 * @since 0.0.41
 */
@SuppressWarnings("PMD.TooManyMethods")
final class LtInconsistentArgs implements Lint {

    @Override
    public String name() {
        return "inconsistent-args";
    }

    @Override
    public Collection<Defect> defects(
        final Map<String, XML> pkg) throws IOException {
        return LtInconsistentArgs.usagesByBase(pkg).entrySet().stream().filter(
            entry -> entry.getValue().stream()
                .map(usage -> usage.args).distinct().count() != 1L
        ).flatMap(
            entry -> entry.getValue().stream().map(
                usage -> LtInconsistentArgs.toDefect(
                    this.name(), entry, usage
                )
            )
        ).collect(Collectors.toList());
    }

    @Override
    public String motive() throws IOException {
        return new UncheckedText(
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
     * Scan all files once and collect all object usages grouped by base name.
     * @param pkg Package with sources
     * @return Usages grouped by base name
     */
    private static Map<String, List<Usage>> usagesByBase(
        final Map<String, XML> pkg) {
        final Map<String, List<Usage>> result = new HashMap<>();
        for (final XML xml : pkg.values()) {
            final Xnav source = new Xnav(xml.inner());
            final String program = new ProgramName(xml).get();
            source.path("//o[@base]").filter(
                obj -> !LtInconsistentArgs.objectReference(obj)
            ).forEach(
                obj -> {
                    final String base =
                        LtInconsistentArgs.objectRef(obj, source);
                    result.computeIfAbsent(base, k -> new ArrayList<>(1)).add(
                        new LtInconsistentArgs.Usage(
                            program,
                            LtInconsistentArgs.lineOf(obj),
                            obj.node().getChildNodes().getLength()
                        )
                    );
                }
            );
        }
        return result;
    }

    /**
     * Build a defect for one usage of an inconsistently-called base.
     * @param lint Lint name
     * @param entry Base name mapped to all its usages across the package
     * @param current The specific usage to report
     * @return Defect
     */
    private static Defect toDefect(
        final String lint,
        final Map.Entry<String, List<Usage>> entry,
        final Usage current
    ) {
        return new Defect.Default(
            lint,
            Severity.WARNING,
            current.program,
            current.line,
            String.format(
                "Object '%s' has arguments inconsistency (clashes with [%s])",
                entry.getKey(),
                entry.getValue().stream()
                    .filter(other -> !current.sameLocation(other))
                    .map(Usage::clashRef)
                    .collect(Collectors.joining(", "))
            )
        );
    }

    /**
     * Get object reference for base attribute.
     * @param obj Object navigator
     * @param source Source navigator
     * @return Reference string
     */
    private static String objectRef(final Xnav obj, final Xnav source) {
        final String base = obj.attribute("base").text().get();
        final String result;
        if (base.startsWith("ξ.")
            && LtInconsistentArgs.voidAttribute(base, obj)) {
            result = LtInconsistentArgs.voidFqn(base, obj);
        } else if (base.startsWith("ξ.")) {
            result = String.format(
                "%s.%s",
                new ProgramName(new XMLDocument(source.node())).get(),
                base
            );
        } else {
            result = base;
        }
        return result;
    }

    /**
     * Extract line number from an XML object.
     * @param obj Object navigator
     * @return Line number or 0 if not found
     */
    private static int lineOf(final Xnav obj) {
        return Integer.parseInt(obj.attribute("line").text().orElse("0"));
    }

    /**
     * Object is a reference to itself?
     * @param object Object
     * @return True or False
     */
    private static boolean objectReference(final Xnav object) {
        final Optional<String> base = object.attribute("base").text();
        return object.attribute("name").text().isEmpty() && base.isPresent()
            && base.get().startsWith("ξ.");
    }

    /**
     * Base refers to void attribute?
     * @param base Object base
     * @param object Object
     * @return True or False
     */
    private static boolean voidAttribute(final String base, final Xnav object) {
        return LtInconsistentArgs.parentObject(object).path(
            String.format("o[@name='%s']", base.replace("ξ.", ""))
        ).anyMatch(
            attr -> attr.attribute("base")
                .text().filter("∅"::equals).isPresent()
        );
    }

    /**
     * Void FQN for given base in object scope.
     * @param base Base
     * @param object Object
     * @return Void FQN
     */
    private static String voidFqn(final String base, final Xnav object) {
        final Xnav method = LtInconsistentArgs.parentObject(object);
        return String.format(
            "%s%s.%s.∅",
            LtInconsistentArgs.parentTree(method),
            LtInconsistentArgs.coordinates(method),
            base
        );
    }

    /**
     * Parent tree up to the given object, in string.
     * @param object Object up to build the tree
     * @return Parent tree
     */
    private static String parentTree(final Xnav object) {
        final List<String> tree = new ListOf<>();
        Xnav current = LtInconsistentArgs.parentObject(object);
        while (!"object".equals(current.node().getNodeName())) {
            tree.add(LtInconsistentArgs.coordinates(current));
            current = LtInconsistentArgs.parentObject(current);
        }
        final String result;
        if (tree.isEmpty()) {
            result = "";
        } else {
            result = tree.stream().collect(Collectors.joining(".", "", "."));
        }
        return result;
    }

    /**
     * Object coordinates.
     * @param object Object
     * @return Object coordinates
     */
    private static String coordinates(final Xnav object) {
        final String result;
        if (object.attribute("name").text().isPresent()) {
            result = object.attribute("name").text().get();
        } else {
            result = ":anonymous";
        }
        return result;
    }

    /**
     * Parent of the given object.
     * @param object Current object
     * @return Parent object
     */
    private static Xnav parentObject(final Xnav object) {
        final Xnav result;
        final Node prev = object.node().getParentNode();
        if (prev != null && prev.getNodeType() == Node.ELEMENT_NODE) {
            result = new Xnav(prev);
        } else {
            result = new Xnav("<o/>");
        }
        return result;
    }

    /**
     * One recorded usage of an object in a source file.
     * @since 0.0.41
     */
    private static final class Usage {

        /**
         * Program name.
         */
        private final String program;

        /**
         * Line number.
         */
        private final int line;

        /**
         * Argument count.
         */
        private final int args;

        /**
         * Ctor.
         * @param pname Program name
         * @param lno Line number
         * @param argc Argument count
         */
        Usage(final String pname, final int lno, final int argc) {
            this.program = pname;
            this.line = lno;
            this.args = argc;
        }

        /**
         * Short reference for use in clash messages.
         * @return Program name and line, colon-separated
         */
        String clashRef() {
            return String.format("%s:%d", this.program, this.line);
        }

        /**
         * True if this usage is at the same source location as another.
         * @param other Other usage
         * @return True or False
         */
        boolean sameLocation(final Usage other) {
            return this.program.equals(other.program)
                && this.line == other.line;
        }
    }
}
