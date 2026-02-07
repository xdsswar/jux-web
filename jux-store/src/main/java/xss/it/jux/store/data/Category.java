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

package xss.it.jux.store.data;

/**
 * A product category in the store.
 *
 * <p>Categories group products into browsable sections. Each category
 * has a unique slug for URL routing, a human-readable name, a description,
 * a product count, and an emoji icon for visual identification.</p>
 *
 * @param slug         URL-friendly identifier (e.g. "electronics")
 * @param name         display name (e.g. "Electronics")
 * @param description  short description of the category
 * @param productCount number of products in this category
 * @param icon         emoji icon for visual display (e.g. "\uD83D\uDCBB")
 */
public record Category(
        String slug,
        String name,
        String description,
        int productCount,
        String icon
) {}
