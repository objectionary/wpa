/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import matchers.DefectMatcher;
import org.eolang.parser.EoSyntax;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for {@link Program} with realistic multi-package directory structures.
 * @since 0.1.0
 */
@ExtendWith(MktmpResolver.class)
final class WpaIntegrationTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void analyzesMultipleFoldersAndFiles(@Mktmp final Path tmp) throws IOException {
        final Path io = tmp.resolve("io");
        final Path txt = tmp.resolve("txt");
        final Path math = tmp.resolve("math");
        this.file(io, "input.xmir", "# Input.\n[size] > input");
        this.file(io, "output.xmir", "# Output.\n[data] > output");
        this.file(io, "reader.xmir", "# Reader.\n[source] > reader");
        this.file(txt, "text.xmir", "# Text.\n[raw] > text");
        this.file(txt, "lines.xmir", "# Lines.\n[content] > lines");
        this.file(txt, "chars.xmir", "# Chars.\n[str] > chars");
        this.file(math, "num.xmir", "# Num.\n[value] > num");
        this.file(math, "add.xmir", "# Add.\n[left right] > add");
        this.file(math, "max.xmir", "# Max.\n[first second] > max");
        MatcherAssert.assertThat(
            "WPA analysis over multiple folders must produce valid defects only",
            new Program(io, txt, math).defects(),
            Matchers.everyItem(new DefectMatcher())
        );
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void detectsInconsistentArgsAcrossFolders(@Mktmp final Path tmp) throws IOException {
        final Path lib = tmp.resolve("lib");
        final Path app = tmp.resolve("app");
        this.file(
            lib,
            "util.xmir",
            String.join(
                System.lineSeparator(),
                "# Util.",
                "[] > util",
                "  helper 1 2 > result"
            )
        );
        this.file(
            lib,
            "core.xmir",
            String.join(
                System.lineSeparator(),
                "# Core.",
                "[] > core",
                "  helper 1 2 3 > result"
            )
        );
        this.file(
            app,
            "main.xmir",
            String.join(
                System.lineSeparator(),
                "# Main.",
                "[] > main",
                "  helper 42 > x"
            )
        );
        MatcherAssert.assertThat(
            "Inconsistent args across folders must be detected",
            new Program(lib, app).defects(),
            Matchers.hasItem(
                Matchers.hasToString(
                    Matchers.containsString("inconsistent-args")
                )
            )
        );
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void findsNoInconsistencyInConsistentPackage(@Mktmp final Path tmp) throws IOException {
        final Path pkg = tmp.resolve("consistent");
        this.file(
            pkg,
            "foo.xmir",
            String.join(
                System.lineSeparator(),
                "# Foo.",
                "[a] > foo"
            )
        );
        this.file(
            pkg,
            "bar.xmir",
            String.join(
                System.lineSeparator(),
                "# Bar.",
                "[] > bar",
                "  foo 1 > x"
            )
        );
        this.file(
            pkg,
            "baz.xmir",
            String.join(
                System.lineSeparator(),
                "# Baz.",
                "[] > baz",
                "  foo 2 > y"
            )
        );
        MatcherAssert.assertThat(
            "Consistent args usage must produce no inconsistency defects",
            new Program(pkg).defects().stream()
                .filter(d -> "inconsistent-args".equals(d.rule()))
                .count(),
            Matchers.equalTo(0L)
        );
    }

    private void file(final Path dir, final String name, final String eo) throws IOException {
        final Path path = dir.resolve(name);
        path.toFile().getParentFile().mkdirs();
        Files.write(
            path,
            new EoSyntax(eo).parsed().toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
