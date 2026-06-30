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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for {@link Program} with realistic multi-package directory structures.
 * @since 0.1.0
 * @todo #27:60min Re-enable the incorrect-alias lint in integration tests once its
 *  performance is fixed. Remove it from the .without() calls in this test class.
 *  The lint is currently too slow because it is called once per source file.
 * @todo #27:60min Re-enable the object-is-not-unique lint in integration tests once
 *  its performance is fixed. Remove it from the .without() calls in this test class.
 *  The lint is currently too slow because it is called once per source file.
 * @todo #27:60min Re-enable the atom-is-not-unique lint in integration tests once
 *  its performance is fixed. Remove it from the .without() calls in this test class.
 *  The lint is currently too slow because it is called once per source file.
 * @todo #27:60min Re-enable the inconsistent-args lint in integration tests once its
 *  performance is fixed. Remove it from the .without() calls and re-enable the
 *  detectsInconsistentArgsAcrossFolders test. The lint has O(n^2) performance issue.
 * @todo #27:60min Re-enable the incorrect-number-of-attributes lint in integration
 *  tests once its performance is fixed. Remove it from the .without() calls in
 *  this test class. The lint recomputes object definitions once per source file.
 */
@ExtendWith(MktmpResolver.class)
@SuppressWarnings("JTCOP.RuleEveryTestHasProductionClass")
final class WpaIntegrationTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void analyzesMultipleFoldersAndFiles(@Mktmp final Path tmp) throws IOException {
        final Path iod = tmp.resolve("io");
        final Path txt = tmp.resolve("txt");
        final Path math = tmp.resolve("math");
        this.file(iod, "input.xmir", "# Input.", "[size] > input");
        this.file(iod, "output.xmir", "# Output.", "[data] > output");
        this.file(iod, "reader.xmir", "# Reader.", "[source] > reader");
        this.file(txt, "text.xmir", "# Text.", "[raw] > text");
        this.file(txt, "lines.xmir", "# Lines.", "[content] > lines");
        this.file(txt, "chars.xmir", "# Chars.", "[str] > chars");
        this.file(math, "num.xmir", "# Num.", "[value] > num");
        this.file(math, "add.xmir", "# Add.", "[left right] > add");
        this.file(math, "max.xmir", "# Max.", "[first second] > max");
        MatcherAssert.assertThat(
            "WPA analysis over multiple folders must produce valid defects only",
            new Program(iod, txt, math).without(
                "incorrect-alias",
                "object-is-not-unique",
                "atom-is-not-unique",
                "inconsistent-args",
                "incorrect-number-of-attributes"
            ).defects(),
            Matchers.everyItem(new DefectMatcher())
        );
    }

    @Test
    @Disabled("inconsistent-args is too slow, see #27")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void detectsInconsistentArgsAcrossFolders(@Mktmp final Path tmp) throws IOException {
        final Path lib = tmp.resolve("lib");
        final Path app = tmp.resolve("app");
        this.file(
            lib, "util.xmir",
            "# Util.", "[] > util", "  helper 1 2 > result"
        );
        this.file(
            lib, "core.xmir",
            "# Core.", "[] > core", "  helper 1 2 3 > result"
        );
        this.file(
            app, "main.xmir",
            "# Main.", "[] > main", "  helper 42 > x"
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
        this.file(pkg, "foo.xmir", "# Foo.", "[a] > foo");
        this.file(pkg, "bar.xmir", "# Bar.", "[] > bar", "  foo 1 > x");
        this.file(pkg, "baz.xmir", "# Baz.", "[] > baz", "  foo 2 > y");
        MatcherAssert.assertThat(
            "Consistent package must produce no defects with fast lints",
            new Program(pkg).without(
                "incorrect-alias",
                "object-is-not-unique",
                "atom-is-not-unique",
                "inconsistent-args",
                "incorrect-number-of-attributes"
            ).defects(),
            Matchers.emptyIterable()
        );
    }

    private void file(
        final Path dir, final String name, final String... lines
    ) throws IOException {
        final Path path = dir.resolve(name);
        path.toFile().getParentFile().mkdirs();
        Files.write(
            path,
            new EoSyntax(
                String.join(System.lineSeparator(), lines)
            ).parsed().toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
