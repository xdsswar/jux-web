/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.html;

/**
 * Runtime exception thrown when an HTML template cannot be loaded, parsed,
 * or processed.
 *
 * <p>Includes the template path and line number where the error occurred,
 * making it easy to locate and fix template issues. The {@link #getMessage()}
 * method formats this information as {@code "path:line - message"}.</p>
 *
 * <p><b>Example output:</b></p>
 * <pre>
 * pages/home.html:42 - Unexpected closing tag &lt;/span&gt;, expected &lt;/div&gt;
 * </pre>
 */
public class TemplateException extends RuntimeException {

    private final String templatePath;
    private final int lineNumber;

    /**
     * Create a new template exception.
     *
     * @param message      description of the error
     * @param templatePath path to the template file where the error occurred
     * @param lineNumber   line number in the template (1-based), or -1 if unknown
     */
    public TemplateException(String message, String templatePath, int lineNumber) {
        super(message);
        this.templatePath = templatePath;
        this.lineNumber = lineNumber;
    }

    /**
     * Create a new template exception with a cause.
     *
     * @param message      description of the error
     * @param templatePath path to the template file where the error occurred
     * @param lineNumber   line number in the template (1-based), or -1 if unknown
     * @param cause        the underlying cause
     */
    public TemplateException(String message, String templatePath, int lineNumber, Throwable cause) {
        super(message, cause);
        this.templatePath = templatePath;
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the path to the template file where the error occurred.
     *
     * @return the template path, may be null if unknown
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * Returns the line number in the template where the error occurred.
     *
     * @return the 1-based line number, or -1 if unknown
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns a formatted error message including the template path and line number.
     *
     * <p>Format: {@code "templatePath:lineNumber - originalMessage"}</p>
     *
     * @return the formatted error message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (templatePath != null) {
            sb.append(templatePath);
        } else {
            sb.append("<unknown>");
        }
        if (lineNumber >= 0) {
            sb.append(':').append(lineNumber);
        }
        sb.append(" - ").append(super.getMessage());
        return sb.toString();
    }
}
