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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hardcoded product catalog for the JUX Store demo.
 *
 * <p>Contains 20 products across 5 categories, sample reviews,
 * and a demo shopping cart. No database required — all data lives
 * in static collections.</p>
 *
 * <p>In a real application, this data would come from JPA repositories
 * backed by a database. The JUX page code would look identical — only
 * the data source changes.</p>
 */
public final class StoreData {

    private StoreData() {} // utility class

    /* ── Categories ──────────────────────────────────────────────── */

    /** All store categories, indexed by slug. */
    private static final Map<String, Category> CATEGORIES = new LinkedHashMap<>();

    /** All products, indexed by slug. */
    private static final Map<String, Product> PRODUCTS = new LinkedHashMap<>();

    /** Product reviews, indexed by product slug. */
    private static final Map<String, List<Review>> REVIEWS = new LinkedHashMap<>();

    static {
        /* ── 5 Categories ──────────────────────────────────────── */
        addCategory(new Category("electronics", "Electronics",
                "Smartphones, laptops, headphones, and cutting-edge gadgets.", 5, "\uD83D\uDCBB"));
        addCategory(new Category("clothing", "Clothing",
                "Stylish apparel for every season and occasion.", 4, "\uD83D\uDC55"));
        addCategory(new Category("home-kitchen", "Home & Kitchen",
                "Everything to make your living space shine.", 4, "\uD83C\uDFE0"));
        addCategory(new Category("sports-outdoors", "Sports & Outdoors",
                "Gear up for your next adventure.", 4, "\u26BD"));
        addCategory(new Category("books", "Books",
                "Bestsellers, classics, and hidden gems.", 3, "\uD83D\uDCDA"));

        /* ── 20 Products ───────────────────────────────────────── */

        // Electronics (5)
        addProduct(new Product(1, "wireless-headphones", "Wireless Noise-Canceling Headphones",
                "Premium over-ear headphones with active noise cancellation and 30-hour battery life.",
                "Experience immersive sound with our flagship wireless headphones. Featuring advanced active noise cancellation technology, these over-ear headphones block out the world so you can focus on what matters. With 30 hours of battery life, premium memory foam ear cushions, and Hi-Res Audio certification, they deliver studio-quality sound wherever you go. The built-in microphone ensures crystal-clear calls, while Bluetooth 5.3 provides a stable, low-latency connection.",
                149.99, 199.99,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop",
                "Black wireless over-ear headphones with cushioned ear cups on a dark background",
                "electronics", 4.7, 234, true, true,
                List.of("headphones", "wireless", "noise-canceling", "bluetooth", "audio"),
                "ELEC-HP-001",
                Map.of("Driver Size", "40mm", "Battery Life", "30 hours", "Bluetooth", "5.3",
                        "Weight", "250g", "Noise Cancellation", "Active (ANC)")));

        addProduct(new Product(2, "smart-watch-pro", "SmartWatch Pro X",
                "Advanced fitness tracking with AMOLED display and 7-day battery life.",
                "The SmartWatch Pro X combines elegant design with powerful health monitoring. Its vibrant 1.4\" AMOLED display is always visible, even in direct sunlight. Track over 100 workout modes, monitor blood oxygen levels, and analyze your sleep patterns. The 7-day battery means less time charging and more time living. Water-resistant to 50 meters, it's your perfect companion for any activity.",
                299.99, 0,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop",
                "Silver smart watch with round AMOLED display showing time and fitness metrics",
                "electronics", 4.5, 189, true, true,
                List.of("watch", "smartwatch", "fitness", "health", "wearable"),
                "ELEC-SW-002",
                Map.of("Display", "1.4\" AMOLED", "Battery", "7 days", "Water Resistance", "5 ATM",
                        "Sensors", "Heart rate, SpO2, GPS", "Compatibility", "iOS & Android")));

        addProduct(new Product(3, "portable-speaker", "Portable Bluetooth Speaker",
                "Rugged waterproof speaker with 360-degree sound and 12-hour playtime.",
                "Take your music anywhere with this rugged portable speaker. Its 360-degree sound design fills any space with rich, balanced audio. IP67 waterproof and dustproof rating means it can handle rain, sand, and even a dunk in the pool. The 12-hour battery keeps the party going, and the built-in power bank can charge your phone in a pinch.",
                79.99, 99.99,
                "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=400&fit=crop",
                "Cylindrical portable bluetooth speaker in teal color on a wooden surface",
                "electronics", 4.3, 156, true, false,
                List.of("speaker", "bluetooth", "portable", "waterproof", "audio"),
                "ELEC-SP-003",
                Map.of("Output", "20W", "Battery", "12 hours", "Waterproof", "IP67",
                        "Bluetooth", "5.0", "Weight", "540g")));

        addProduct(new Product(4, "laptop-stand", "Ergonomic Laptop Stand",
                "Adjustable aluminum stand for better posture and cooling.",
                "Elevate your workspace with this premium aluminum laptop stand. Six adjustable height settings let you find the perfect viewing angle, reducing neck and back strain. The open-air design improves airflow to keep your laptop cool during demanding tasks. Compatible with laptops from 10\" to 17\", its non-slip silicone pads protect your device while keeping it stable.",
                49.99, 0,
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop",
                "Silver aluminum laptop stand on a clean white desk with a laptop on it",
                "electronics", 4.6, 312, true, false,
                List.of("laptop", "stand", "ergonomic", "desk", "aluminum"),
                "ELEC-LS-004",
                Map.of("Material", "Aluminum alloy", "Compatibility", "10\"-17\" laptops",
                        "Height Settings", "6 levels", "Weight Capacity", "10kg", "Weight", "280g")));

        addProduct(new Product(5, "wireless-earbuds", "True Wireless Earbuds",
                "Compact earbuds with spatial audio and 8-hour battery.",
                "Discover freedom with these true wireless earbuds. Custom-tuned drivers deliver rich, detailed sound with spatial audio support for an immersive 3D listening experience. The ergonomic design with three sizes of silicone tips ensures a comfortable, secure fit. Get 8 hours of listening on a single charge, plus 24 more from the compact charging case.",
                89.99, 119.99,
                "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=400&h=400&fit=crop",
                "White wireless earbuds next to their open charging case on a light surface",
                "electronics", 4.4, 278, true, true,
                List.of("earbuds", "wireless", "bluetooth", "audio", "compact"),
                "ELEC-EB-005",
                Map.of("Driver", "12mm custom", "Battery (buds)", "8 hours",
                        "Battery (case)", "24 hours", "Bluetooth", "5.3", "Water Resistance", "IPX5")));

        // Clothing (4)
        addProduct(new Product(6, "classic-denim-jacket", "Classic Denim Jacket",
                "Timeless denim jacket with a modern slim fit and premium wash.",
                "A wardrobe essential reinvented. This classic denim jacket features a contemporary slim fit that pairs effortlessly with any outfit. Crafted from premium heavyweight denim with a vintage stone-wash finish, it softens with every wear. Metal buttons, two chest pockets, and adjustable waist tabs complete the iconic look.",
                89.99, 0,
                "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=400&h=400&fit=crop",
                "Blue denim jacket laid flat on a neutral background showing front details",
                "clothing", 4.5, 167, true, true,
                List.of("jacket", "denim", "classic", "outerwear", "casual"),
                "CLO-DJ-006",
                Map.of("Material", "100% Cotton Denim", "Fit", "Slim", "Wash", "Stone wash",
                        "Closure", "Metal buttons", "Sizes", "XS - 3XL")));

        addProduct(new Product(7, "merino-wool-sweater", "Merino Wool Crewneck Sweater",
                "Ultra-soft merino wool sweater that regulates temperature naturally.",
                "Luxurious comfort meets everyday practicality. This crewneck sweater is knitted from 100% Australian merino wool, renowned for its exceptional softness and natural temperature regulation. It keeps you warm in winter and cool in summer. The fine gauge knit gives it a polished look suitable for both office and weekend wear.",
                119.99, 149.99,
                "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=400&h=400&fit=crop",
                "Folded navy blue merino wool sweater on a wooden surface",
                "clothing", 4.8, 92, true, false,
                List.of("sweater", "merino", "wool", "knitwear", "warm"),
                "CLO-SW-007",
                Map.of("Material", "100% Merino Wool", "Gauge", "Fine knit", "Care", "Machine washable",
                        "Origin", "Australia", "Sizes", "S - 2XL")));

        addProduct(new Product(8, "running-sneakers", "Performance Running Sneakers",
                "Lightweight running shoes with responsive cushioning and breathable mesh.",
                "Engineered for speed and comfort. These performance running sneakers feature a responsive foam midsole that returns energy with every stride. The breathable engineered mesh upper keeps your feet cool during intense workouts. A durable rubber outsole with multi-directional tread provides reliable traction on both road and trail.",
                129.99, 0,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop",
                "Red and white running sneaker shown from the side on a gradient background",
                "clothing", 4.6, 445, true, true,
                List.of("shoes", "running", "sneakers", "athletic", "sport"),
                "CLO-RS-008",
                Map.of("Upper", "Engineered mesh", "Midsole", "Responsive foam",
                        "Outsole", "Rubber", "Drop", "8mm", "Weight", "240g (size 10)")));

        addProduct(new Product(9, "linen-summer-shirt", "Linen Summer Shirt",
                "Breathable pure linen shirt perfect for warm weather.",
                "Stay cool and look sharp with this pure linen button-up shirt. The natural linen fabric is exceptionally breathable, wicking moisture away to keep you comfortable on the hottest days. A relaxed fit with a camp collar gives it a casual elegance that works from beach to bistro. The more you wear and wash it, the softer it gets.",
                69.99, 89.99,
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop",
                "Light beige linen shirt hanging on a wooden hanger against a white wall",
                "clothing", 4.2, 88, true, false,
                List.of("shirt", "linen", "summer", "casual", "breathable"),
                "CLO-LS-009",
                Map.of("Material", "100% Linen", "Collar", "Camp collar", "Fit", "Relaxed",
                        "Care", "Machine wash cold", "Sizes", "S - 3XL")));

        // Home & Kitchen (4)
        addProduct(new Product(10, "ceramic-cookware-set", "Ceramic Non-Stick Cookware Set",
                "10-piece ceramic cookware set with toxin-free non-stick coating.",
                "Transform your kitchen with this professional-grade ceramic cookware set. The toxin-free ceramic coating provides effortless non-stick performance without PFOA, PTFE, or other harmful chemicals. The set includes everything you need: 8\" and 10\" fry pans, 1.5qt and 3qt saucepans with lids, a 5qt Dutch oven with lid, and a stainless steel steamer insert. Suitable for all cooktops including induction.",
                199.99, 279.99,
                "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop",
                "Set of gray ceramic cookware pots and pans arranged on a kitchen counter",
                "home-kitchen", 4.7, 523, true, true,
                List.of("cookware", "ceramic", "kitchen", "non-stick", "cooking"),
                "HK-CK-010",
                Map.of("Pieces", "10", "Coating", "Ceramic (toxin-free)", "Cooktops", "All including induction",
                        "Dishwasher Safe", "Yes", "Oven Safe", "Up to 450\u00b0F")));

        addProduct(new Product(11, "smart-air-purifier", "Smart Air Purifier",
                "HEPA air purifier with app control and real-time air quality monitoring.",
                "Breathe cleaner air with this smart HEPA air purifier. The three-stage filtration system captures 99.97% of particles as small as 0.3 microns, including dust, pollen, smoke, and pet dander. The real-time air quality sensor displays a color-coded AQI reading and automatically adjusts fan speed. Control it from anywhere via the companion app or your favorite voice assistant.",
                179.99, 0,
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400&h=400&fit=crop",
                "White cylindrical smart air purifier in a modern living room setting",
                "home-kitchen", 4.5, 198, true, false,
                List.of("air purifier", "smart home", "HEPA", "clean air", "health"),
                "HK-AP-011",
                Map.of("Filter", "True HEPA H13", "Coverage", "540 sq ft", "Noise", "24 dB (sleep mode)",
                        "Smart Features", "App, Voice, Auto mode", "Filter Life", "12 months")));

        addProduct(new Product(12, "artisan-coffee-grinder", "Artisan Burr Coffee Grinder",
                "Precision conical burr grinder with 40 grind settings.",
                "Unlock the full flavor of your coffee beans with this artisan burr grinder. The precision conical burrs provide a consistent grind from espresso-fine to French press-coarse across 40 settings. The low-speed motor preserves flavor compounds by minimizing heat buildup. A built-in scale ensures exact dosing, while the anti-static technology keeps your counter clean.",
                149.99, 0,
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=400&fit=crop",
                "Stainless steel burr coffee grinder with coffee beans scattered around it",
                "home-kitchen", 4.8, 342, true, false,
                List.of("coffee", "grinder", "burr", "kitchen", "brewing"),
                "HK-CG-012",
                Map.of("Burr Type", "Conical stainless steel", "Settings", "40 grind levels",
                        "Capacity", "340g beans", "Built-in Scale", "Yes", "Motor", "Low-speed DC")));

        addProduct(new Product(13, "organic-cotton-bedding", "Organic Cotton Bedding Set",
                "GOTS-certified organic cotton sheet set with duvet cover.",
                "Sleep sustainably in pure comfort. This bedding set is made from 100% GOTS-certified organic cotton, grown without synthetic pesticides or fertilizers. The 300-thread-count sateen weave has a silky-smooth hand feel that gets softer with every wash. The set includes a fitted sheet, flat sheet, two pillowcases, and a duvet cover with hidden button closure.",
                159.99, 199.99,
                "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=400&h=400&fit=crop",
                "Neatly made bed with crisp white organic cotton bedding in a bright bedroom",
                "home-kitchen", 4.6, 167, true, true,
                List.of("bedding", "organic", "cotton", "sheets", "bedroom"),
                "HK-BD-013",
                Map.of("Material", "100% Organic Cotton", "Thread Count", "300 (sateen)",
                        "Certification", "GOTS", "Pieces", "5 (fitted, flat, 2 pillowcases, duvet cover)",
                        "Sizes", "Twin - California King")));

        // Sports & Outdoors (4)
        addProduct(new Product(14, "yoga-mat-premium", "Premium Non-Slip Yoga Mat",
                "Extra-thick eco-friendly yoga mat with alignment markers.",
                "Elevate your practice with this premium yoga mat. The 6mm thickness provides superior cushioning for joints without sacrificing stability. Laser-etched alignment markers help you maintain proper form in every pose. Made from eco-friendly TPE material that is free of PVC, latex, and toxic glues. The textured non-slip surface grips better with moisture, making it perfect for hot yoga.",
                59.99, 79.99,
                "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=400&h=400&fit=crop",
                "Rolled purple yoga mat with alignment lines on a wooden studio floor",
                "sports-outdoors", 4.7, 567, true, true,
                List.of("yoga", "mat", "fitness", "exercise", "eco-friendly"),
                "SO-YM-014",
                Map.of("Material", "Eco-friendly TPE", "Thickness", "6mm", "Size", "72\" x 24\"",
                        "Features", "Alignment markers, non-slip", "Weight", "1.8 kg")));

        addProduct(new Product(15, "hiking-backpack-40l", "Trail Hiking Backpack 40L",
                "Ventilated 40-liter hiking backpack with rain cover and hydration compatibility.",
                "Conquer any trail with this purpose-built hiking backpack. The 40-liter capacity is ideal for multi-day adventures. An adjustable ventilated back panel keeps you cool on steep climbs. Multiple access points (top, front, bottom) make it easy to reach your gear. The integrated rain cover deploys in seconds when weather turns. Hydration-compatible with a dedicated sleeve and tube routing.",
                129.99, 0,
                "https://images.unsplash.com/photo-1622260614153-03223fb72052?w=400&h=400&fit=crop",
                "Green hiking backpack sitting on a rock with mountains in the background",
                "sports-outdoors", 4.4, 223, true, false,
                List.of("backpack", "hiking", "outdoor", "travel", "camping"),
                "SO-HB-015",
                Map.of("Volume", "40 liters", "Back Panel", "Ventilated mesh", "Rain Cover", "Integrated",
                        "Hydration", "Compatible (up to 3L)", "Weight", "1.3 kg")));

        addProduct(new Product(16, "resistance-band-set", "Resistance Band Set",
                "5-band set with handles, door anchor, and carrying bag.",
                "Build strength anywhere with this complete resistance band set. Five color-coded bands provide 10 to 50 pounds of resistance that can be stacked for up to 150 pounds total. Snap-resistant latex construction with reinforced connectors ensures durability. Includes cushioned handles, ankle straps, a door anchor, and a portable carrying bag for on-the-go workouts.",
                34.99, 49.99,
                "https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=400&h=400&fit=crop",
                "Colorful set of five resistance bands with handles laid out on a gym floor",
                "sports-outdoors", 4.3, 891, true, false,
                List.of("resistance bands", "fitness", "exercise", "strength", "home gym"),
                "SO-RB-016",
                Map.of("Bands", "5 (10-50 lbs each)", "Max Resistance", "150 lbs (stacked)",
                        "Material", "Natural latex", "Includes", "Handles, ankle straps, door anchor, bag",
                        "Length", "48 inches each")));

        addProduct(new Product(17, "camping-hammock", "Ultralight Camping Hammock",
                "Parachute nylon hammock with tree straps, supports up to 400 lbs.",
                "Relax in the great outdoors with this ultralight camping hammock. Made from ripstop parachute nylon, it supports up to 400 pounds while weighing just 12 ounces. The included tree-friendly straps have 20 adjustment points for easy setup in under 60 seconds. Triple-stitched seams and carabiner-grade clips ensure rock-solid reliability. Packs down to the size of a softball.",
                29.99, 0,
                "https://images.unsplash.com/photo-1520101244246-293f77ffc39e?w=400&h=400&fit=crop",
                "Orange camping hammock strung between two pine trees in a forest",
                "sports-outdoors", 4.6, 1243, true, true,
                List.of("hammock", "camping", "outdoor", "ultralight", "travel"),
                "SO-CH-017",
                Map.of("Material", "Ripstop parachute nylon", "Weight Capacity", "400 lbs",
                        "Weight", "12 oz", "Packed Size", "5\" x 8\"", "Setup Time", "< 60 seconds")));

        // Books (3)
        addProduct(new Product(18, "clean-code-handbook", "Clean Code: A Handbook",
                "The definitive guide to writing readable, maintainable software.",
                "Even bad code can function. But if code isn't clean, it can bring a development organization to its knees. Every year, countless hours and significant resources are lost because of poorly written code. But it doesn't have to be that way. This book is a must-read for any developer who wants to write code that is easy to read, easy to change, and easy to maintain.",
                39.99, 0,
                "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400&h=400&fit=crop",
                "Hardcover programming book standing upright on a desk next to a laptop",
                "books", 4.8, 2156, true, false,
                List.of("programming", "software", "clean code", "best practices", "development"),
                "BK-CC-018",
                Map.of("Author", "Robert C. Martin", "Pages", "464", "Format", "Hardcover & Digital",
                        "Language", "English", "Publisher", "Prentice Hall")));

        addProduct(new Product(19, "design-patterns-illustrated", "Design Patterns Illustrated",
                "Visual guide to the 23 GoF design patterns with modern examples.",
                "Design patterns are the building blocks of elegant software architecture. This beautifully illustrated guide brings the 23 classic Gang of Four patterns to life with clear diagrams, modern Java examples, and real-world use cases. Each pattern is explained with a visual metaphor that makes it intuitive and memorable. Whether you're a junior developer or a seasoned architect, this book will deepen your understanding.",
                44.99, 54.99,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=400&fit=crop",
                "Open technical book showing colorful diagrams and code examples",
                "books", 4.7, 876, true, true,
                List.of("design patterns", "software architecture", "programming", "java", "reference"),
                "BK-DP-019",
                Map.of("Author", "Various", "Pages", "352", "Format", "Softcover & Digital",
                        "Language", "English", "Edition", "2nd Edition")));

        addProduct(new Product(20, "mindful-productivity", "Mindful Productivity",
                "Science-backed strategies for focused work and intentional rest.",
                "In a world of constant distraction, the ability to focus deeply is a superpower. This book draws on neuroscience, psychology, and the habits of peak performers to present a practical system for getting meaningful work done without burning out. Learn to structure your day for deep work, manage energy instead of time, and build sustainable routines that compound over months and years.",
                24.99, 0,
                "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400&h=400&fit=crop",
                "Paperback book on a wooden table with reading glasses and a cup of coffee",
                "books", 4.5, 432, true, false,
                List.of("productivity", "mindfulness", "self-improvement", "focus", "habits"),
                "BK-MP-020",
                Map.of("Author", "Dr. Sarah Chen", "Pages", "288", "Format", "Paperback & Digital",
                        "Language", "English", "Published", "2025")));

        /* ── Reviews ───────────────────────────────────────────── */

        addReviews("wireless-headphones", List.of(
                new Review("Alex M.", 5, "Best headphones I've ever owned. The noise cancellation is incredible — I can't hear anything on my commute.", "Jan 10, 2026", true),
                new Review("Sarah K.", 4, "Great sound quality and comfortable for long sessions. Battery life is as advertised. Only wish the case was more compact.", "Dec 28, 2025", true),
                new Review("Mike R.", 5, "Bought these for working from home. The ANC blocks out my neighbors perfectly. Call quality is excellent too.", "Jan 22, 2026", true)
        ));

        addReviews("smart-watch-pro", List.of(
                new Review("Jordan T.", 5, "Tracks everything I need. The sleep analysis has actually helped me improve my rest patterns.", "Feb 1, 2026", true),
                new Review("Pat L.", 4, "Beautiful display and great fitness features. The 7-day battery is a game changer after coming from a daily-charge watch.", "Jan 15, 2026", true)
        ));

        addReviews("ceramic-cookware-set", List.of(
                new Review("Chef Diana", 5, "Finally, non-stick that works without the toxic chemicals. Heats evenly and cleans up in seconds.", "Jan 5, 2026", true),
                new Review("Tom H.", 5, "Excellent value for a 10-piece set. The Dutch oven alone is worth the price.", "Dec 20, 2025", true),
                new Review("Maria G.", 4, "Love cooking with these. The ceramic coating is genuinely non-stick. Just remember to use medium heat.", "Jan 30, 2026", true)
        ));

        addReviews("yoga-mat-premium", List.of(
                new Review("Yoga with Jen", 5, "The alignment markers are a game changer for my practice. Non-slip even during hot yoga.", "Jan 18, 2026", true),
                new Review("Dave P.", 4, "Thick, comfortable, and eco-friendly. My knees thank me during floor poses.", "Feb 3, 2026", true)
        ));

        addReviews("camping-hammock", List.of(
                new Review("Trail Runner", 5, "Weighs nothing, sets up in seconds, incredibly comfortable. Took it on a 5-day hike — no regrets.", "Jan 12, 2026", true),
                new Review("Weekend Camper", 5, "My kids fight over who gets to nap in this thing. Tree straps are easy to use.", "Dec 15, 2025", true),
                new Review("Outdoor Amy", 4, "Great quality for the price. Packed size is impressive. Wish it came in more colors.", "Jan 25, 2026", false)
        ));
    }

    /* ── Helpers ──────────────────────────────────────────────────── */

    private static void addCategory(Category c) { CATEGORIES.put(c.slug(), c); }
    private static void addProduct(Product p) { PRODUCTS.put(p.slug(), p); }
    private static void addReviews(String slug, List<Review> reviews) { REVIEWS.put(slug, reviews); }

    /* ── Public API ──────────────────────────────────────────────── */

    /** Returns all categories in insertion order. */
    public static List<Category> allCategories() {
        return List.copyOf(CATEGORIES.values());
    }

    /** Finds a category by its slug. */
    public static Optional<Category> findCategory(String slug) {
        return Optional.ofNullable(CATEGORIES.get(slug));
    }

    /** Returns all products in insertion order. */
    public static List<Product> allProducts() {
        return List.copyOf(PRODUCTS.values());
    }

    /** Finds a product by its slug. */
    public static Optional<Product> findProduct(String slug) {
        return Optional.ofNullable(PRODUCTS.get(slug));
    }

    /** Returns products in a given category. */
    public static List<Product> productsByCategory(String categorySlug) {
        return PRODUCTS.values().stream()
                .filter(p -> p.category().equals(categorySlug))
                .toList();
    }

    /** Returns products marked as featured. */
    public static List<Product> featuredProducts() {
        return PRODUCTS.values().stream()
                .filter(Product::featured)
                .toList();
    }

    /** Searches products by name, description, or tags (case-insensitive). */
    public static List<Product> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        var q = query.toLowerCase().trim();
        return PRODUCTS.values().stream()
                .filter(p -> p.name().toLowerCase().contains(q)
                        || p.description().toLowerCase().contains(q)
                        || p.tags().stream().anyMatch(t -> t.toLowerCase().contains(q)))
                .toList();
    }

    /** Returns reviews for a product, or an empty list if none exist. */
    public static List<Review> reviewsFor(String productSlug) {
        return REVIEWS.getOrDefault(productSlug, List.of());
    }

    /** Returns related products (same category, excluding the given product). */
    public static List<Product> relatedProducts(String productSlug, int limit) {
        return findProduct(productSlug)
                .map(p -> PRODUCTS.values().stream()
                        .filter(other -> other.category().equals(p.category())
                                && !other.slug().equals(productSlug))
                        .limit(limit)
                        .toList())
                .orElse(List.of());
    }

    /** Returns a sample cart with 2 items for the demo. */
    public static List<CartItem> sampleCart() {
        var items = new ArrayList<CartItem>();
        findProduct("wireless-headphones").ifPresent(p -> items.add(new CartItem(p, 1)));
        findProduct("yoga-mat-premium").ifPresent(p -> items.add(new CartItem(p, 2)));
        return List.copyOf(items);
    }

    /**
     * Paginates a list of products.
     *
     * @param products the full list to paginate
     * @param page     1-based page number
     * @param pageSize number of items per page
     * @return the subset of products for the requested page
     */
    public static List<Product> paginate(List<Product> products, int page, int pageSize) {
        int start = (page - 1) * pageSize;
        if (start >= products.size()) return List.of();
        int end = Math.min(start + pageSize, products.size());
        return products.subList(start, end);
    }

    /**
     * Sorts products by the given criteria.
     *
     * @param products the list to sort
     * @param sort     sort key: "price-asc", "price-desc", "rating", "name", or "newest"
     * @return a new sorted list
     */
    public static List<Product> sort(List<Product> products, String sort) {
        if (sort == null) return products;
        var sorted = new ArrayList<>(products);
        switch (sort) {
            case "price-asc" -> sorted.sort(Comparator.comparingDouble(Product::price));
            case "price-desc" -> sorted.sort(Comparator.comparingDouble(Product::price).reversed());
            case "rating" -> sorted.sort(Comparator.comparingDouble(Product::rating).reversed());
            case "name" -> sorted.sort(Comparator.comparing(Product::name));
            default -> {} // "newest" = insertion order (default)
        }
        return List.copyOf(sorted);
    }

    /** Formats a price as a USD string (e.g. "$149.99"). */
    public static String formatPrice(double price) {
        return String.format("$%.2f", price);
    }
}
