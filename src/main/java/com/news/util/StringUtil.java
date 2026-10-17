package com.news.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * String utility methods used across the application.
 */
public class StringUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");

    /**
     * Converts a title to a URL-friendly slug.
     * e.g. "Hello World!" -> "hello-world"
     */
    public static String toSlug(String input) {
        if (input == null) return "";
        String now = Normalizer.normalize(input, Normalizer.Form.NFD);
        now = NON_LATIN.matcher(now).replaceAll("-");
        now = WHITESPACE.matcher(now).replaceAll("-");
        now = EDGE_DASHES.matcher(now).replaceAll("");
        return now.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Truncates a string to maxLen, appending "..." if truncated.
     */
    public static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen).trim() + "...";
    }

    /**
     * Strips HTML tags from a string for plain-text summaries.
     */
    public static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * Returns a safe string, defaulting to empty string if null.
     */
    public static String safe(String value) {
        return value != null ? value : "";
    }
}
