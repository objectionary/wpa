/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

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
    static final String NAME = "object-is-not-unique";

    @Override
    public String name() {
        return LtObjectIsNotUnique.NAME;
    }

    @Override
    public Collection<Defect> defects(final Map<String, XML> pkg) {
        return pkg.values().stream()
            .map(SourceObject::from)
            .collect(Collectors.groupingBy(SourceObject::key))
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

    private static Collection<Defect> defects(
        final List<SourceObject> group
    ) {
        return IntStream.range(0, group.size()).boxed().flatMap(
            idx -> IntStream.range(0, group.size())
                .filter(other -> other != idx)
                .mapToObj(other -> group.get(idx).defect(group.get(other)))
        ).collect(Collectors.toList());
    }
}
