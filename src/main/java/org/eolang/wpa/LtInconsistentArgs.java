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
                .map(Usage::args).distinct().count() != 1L
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
                        new Usage(
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

    private static Defect toDefect(
        final String lint,
        final Map.Entry<String, List<Usage>> entry,
        final Usage current
    ) {
        return new Defect.Default(
            lint,
            Severity.WARNING,
            current.program(),
            current.line(),
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

    private static int lineOf(final Xnav obj) {
        return Integer.parseInt(obj.attribute("line").text().orElse("0"));
    }

    private static boolean objectReference(final Xnav object) {
        final Optional<String> base = object.attribute("base").text();
        return object.attribute("name").text().isEmpty() && base.isPresent()
            && base.get().startsWith("ξ.");
    }

    private static boolean voidAttribute(final String base, final Xnav object) {
        return LtInconsistentArgs.parentObject(object).path(
            String.format("o[@name='%s']", base.replace("ξ.", ""))
        ).anyMatch(
            attr -> attr.attribute("base")
                .text().filter("∅"::equals).isPresent()
        );
    }

    private static String voidFqn(final String base, final Xnav object) {
        final Xnav method = LtInconsistentArgs.parentObject(object);
        return String.format(
            "%s%s.%s.∅",
            LtInconsistentArgs.parentTree(method),
            LtInconsistentArgs.coordinates(method),
            base
        );
    }

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

    private static String coordinates(final Xnav object) {
        final String result;
        if (object.attribute("name").text().isPresent()) {
            result = object.attribute("name").text().get();
        } else {
            result = ":anonymous";
        }
        return result;
    }

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
}
