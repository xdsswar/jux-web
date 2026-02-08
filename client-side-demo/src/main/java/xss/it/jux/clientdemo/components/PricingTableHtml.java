/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.html.HtmlLoader;
import xss.it.jux.html.annotation.Html;
import xss.it.jux.html.annotation.HtmlId;
import xss.it.jux.html.annotation.Slot;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Pricing comparison table loaded from an HTML template via the jux-html module.
 *
 * <p>This component demonstrates the combined use of {@link Html @Html},
 * {@link HtmlId @HtmlId}, and {@link Slot @Slot} annotations. The overall
 * card layout (three pricing tiers with headers, prices, and CTA buttons) is
 * defined in the HTML template, while the feature lists are dynamically
 * generated in Java and injected into the template's slot elements.</p>
 *
 * <h2>JUX-HTML Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @HtmlId} for text mutation</b> — plan names, prices,
 *       descriptions, and button labels are populated dynamically.</li>
 *   <li><b>{@code @Slot} for content injection</b> — each plan's feature
 *       list ({@code <ul>}) is a slot that receives dynamically generated
 *       {@code <li>} children built with the JUX element API.</li>
 *   <li><b>Hybrid approach</b> — static layout in HTML, dynamic content
 *       in Java. This demonstrates the sweet spot of jux-html: designers
 *       can own the markup while developers own the data.</li>
 * </ul>
 *
 * @see Html
 * @see HtmlId
 * @see Slot
 * @see HtmlLoader
 */
@Html("components/pricing-table.html")
public class PricingTableHtml extends Component {

    /* ── Plan 1 injected elements ── */

    @HtmlId("plan-1-name")
    private Element plan1Name;

    @HtmlId("plan-1-price")
    private Element plan1Price;

    @HtmlId("plan-1-desc")
    private Element plan1Desc;

    @Slot("plan-1-features")
    private Element plan1Features;

    @HtmlId("plan-1-cta")
    private Element plan1Cta;

    /* ── Plan 2 injected elements ── */

    @HtmlId("plan-2-name")
    private Element plan2Name;

    @HtmlId("plan-2-price")
    private Element plan2Price;

    @HtmlId("plan-2-desc")
    private Element plan2Desc;

    @Slot("plan-2-features")
    private Element plan2Features;

    @HtmlId("plan-2-cta")
    private Element plan2Cta;

    /* ── Plan 3 injected elements ── */

    @HtmlId("plan-3-name")
    private Element plan3Name;

    @HtmlId("plan-3-price")
    private Element plan3Price;

    @HtmlId("plan-3-desc")
    private Element plan3Desc;

    @Slot("plan-3-features")
    private Element plan3Features;

    @HtmlId("plan-3-cta")
    private Element plan3Cta;

    /**
     * Load the pricing table template, populate plan details, and inject
     * dynamically generated feature lists into the slot elements.
     *
     * @return the root element of the pricing table
     */
    @Override
    public Element render() {
        Element root = HtmlLoader.load(this);

        /* ── Plan 1: Starter ── */
        plan1Name.text("Starter");
        plan1Price.text("$9");
        plan1Desc.text("Perfect for individuals and small side projects.");
        plan1Cta.text("Get Started");
        plan1Features.children(buildFeatureList(List.of(
                "Up to 3 projects",
                "1 GB storage",
                "Basic analytics",
                "Email support"
        )));

        /* ── Plan 2: Professional (highlighted) ── */
        plan2Name.text("Professional");
        plan2Price.text("$29");
        plan2Desc.text("Ideal for growing teams and production workloads.");
        plan2Cta.text("Start Free Trial");
        plan2Features.children(buildFeatureList(List.of(
                "Unlimited projects",
                "50 GB storage",
                "Advanced analytics",
                "Priority support",
                "Custom domains",
                "Team collaboration"
        )));

        /* ── Plan 3: Enterprise ── */
        plan3Name.text("Enterprise");
        plan3Price.text("$99");
        plan3Desc.text("For large organisations with advanced security needs.");
        plan3Cta.text("Contact Sales");
        plan3Features.children(buildFeatureList(List.of(
                "Everything in Professional",
                "500 GB storage",
                "SSO / SAML",
                "Audit logging",
                "Dedicated account manager",
                "99.99% SLA",
                "On-premise option"
        )));

        return root;
    }

    /**
     * Build a list of feature {@code <li>} elements with check-mark icons.
     *
     * @param features the feature description strings
     * @return a list of {@code <li>} elements
     */
    private List<Element> buildFeatureList(List<String> features) {
        return features.stream()
                .map(feature -> li().cls("flex", "items-center", "gap-2").children(
                        /* Checkmark icon */
                        span().cls("text-violet-400").ariaHidden(true).text("\u2713"),
                        span().text(feature)
                ))
                .toList();
    }
}
