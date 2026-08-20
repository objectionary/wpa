/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2026 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.wpa;

/**
 * One recorded usage of an object in a source file.
 * @since 0.0.41
 */
final class Usage {

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
     * Program name.
     * @return Name of the program
     */
    String program() {
        return this.program;
    }

    /**
     * Line number.
     * @return Number of the line
     */
    int line() {
        return this.line;
    }

    /**
     * Argument count.
     * @return Count of arguments
     */
    int args() {
        return this.args;
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
        return this.program.equals(other.program())
            && this.line == other.line();
    }
}
