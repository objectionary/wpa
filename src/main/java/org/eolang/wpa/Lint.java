/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

import com.jcabi.xml.XML;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A single whole-program checker for a set of {@code .xmir} sources.
 *
 * @since 0.0.1
 */
public interface Lint {

    /**
     * Name of the lint.
     * @return Lint name
     */
    String name();

    /**
     * Find and return defects.
     * @param pkg The XMIR sources to analyze, keyed by program name
     * @return Defects
     */
    Collection<Defect> defects(Map<String, XML> pkg) throws IOException;

    /**
     * Returns motive for a lint, explaining why this lint exists.
     * @return Motive text about lint
     * @throws IOException if something went wrong
     */
    String motive() throws IOException;

}
