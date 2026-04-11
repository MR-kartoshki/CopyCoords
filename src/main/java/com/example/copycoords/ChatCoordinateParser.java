package com.example.copycoords;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ChatCoordinateParser {
    private static final String OVERWORLD_ID = "minecraft:overworld";
    private static final String NETHER_ID = "minecraft:the_nether";
    private static final String END_ID = "minecraft:the_end";
    private static final String NUMBER_PATTERN = "-?\\d+(?:\\.\\d+)?";
    private static final int DIMENSION_SEARCH_MARGIN = 40;

    private static final Pattern XYZ_PATTERN = Pattern.compile(
            "(?i)(?<!\\w)x\\s*[:=]\\s*(" + NUMBER_PATTERN + ")\\s*[,; ]+y\\s*[:=]\\s*(" + NUMBER_PATTERN
                    + ")\\s*[,; ]+z\\s*[:=]\\s*(" + NUMBER_PATTERN + ")");

    private static final Pattern BRACKET_PATTERN = Pattern.compile(
            "\\[\\s*(" + NUMBER_PATTERN + ")\\s*,\\s*(" + NUMBER_PATTERN + ")\\s*,\\s*(" + NUMBER_PATTERN + ")\\s*\\]");

    private static final Pattern SPACE_PATTERN = Pattern.compile(
            "(?<![\\w.])(" + NUMBER_PATTERN + ")\\s+(" + NUMBER_PATTERN + ")\\s+(" + NUMBER_PATTERN + ")(?![\\w.])");

    private static final Pattern DIMENSION_PATTERN = Pattern.compile(
            "(?i)(minecraft:overworld|minecraft:the_nether|minecraft:the_end|overworld|the\\s+nether|nether|end)");

    private ChatCoordinateParser() {
    }

    static List<DetectedCoordinate> detect(String text, int maxResults) {
        if (text == null || text.isBlank() || maxResults <= 0) {
            return List.of();
        }

        List<DetectedCoordinate> results = new ArrayList<>();
        Set<String> dedupe = new LinkedHashSet<>();
        List<Range> occupied = new ArrayList<>();

        collectMatches(text, XYZ_PATTERN, maxResults, results, dedupe, occupied);
        collectMatches(text, BRACKET_PATTERN, maxResults, results, dedupe, occupied);
        collectMatches(text, SPACE_PATTERN, maxResults, results, dedupe, occupied);

        return results;
    }

    static String normalizeDimensionHint(String raw) {
        if (raw == null) {
            return null;
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT)
                .replace('(', ' ')
                .replace(')', ' ')
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isEmpty() || normalized.equals("unknown")) {
            return null;
        }

        if (normalized.equals("minecraft:overworld") || normalized.equals("overworld")) {
            return OVERWORLD_ID;
        }
        if (normalized.equals("minecraft:the_nether") || normalized.equals("nether") || normalized.equals("the nether")) {
            return NETHER_ID;
        }
        if (normalized.equals("minecraft:the_end") || normalized.equals("end")) {
            return END_ID;
        }

        return null;
    }

    static String toCommandNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static void collectMatches(String text,
                                       Pattern pattern,
                                       int maxResults,
                                       List<DetectedCoordinate> results,
                                       Set<String> dedupe,
                                       List<Range> occupied) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find() && results.size() < maxResults) {
            if (overlapsExisting(occupied, matcher.start(), matcher.end())) {
                continue;
            }

            double x;
            double y;
            double z;
            try {
                x = Double.parseDouble(matcher.group(1));
                y = Double.parseDouble(matcher.group(2));
                z = Double.parseDouble(matcher.group(3));
            } catch (NumberFormatException error) {
                continue;
            }

            if (!looksLikeCoordinates(x, y, z)) {
                continue;
            }

            String dimensionId = extractDimensionHint(text, matcher.start(), matcher.end());
            String key = toCommandNumber(x) + "|" + toCommandNumber(y) + "|" + toCommandNumber(z) + "|" + dimensionId;
            if (!dedupe.add(key)) {
                continue;
            }

            results.add(new DetectedCoordinate(x, y, z, dimensionId, matcher.start(), matcher.end()));
            occupied.add(new Range(matcher.start(), matcher.end()));
        }
    }

    private static boolean overlapsExisting(List<Range> occupied, int start, int end) {
        for (Range range : occupied) {
            if (start < range.end && end > range.start) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeCoordinates(double x, double y, double z) {
        return Math.abs(x) <= 30_000_000
                && Math.abs(z) <= 30_000_000
                && y >= -2048
                && y <= 2048;
    }

    private static String extractDimensionHint(String text, int start, int end) {
        int suffixStart = Math.max(0, end);
        int suffixEnd = Math.min(text.length(), end + DIMENSION_SEARCH_MARGIN);
        String suffix = text.substring(suffixStart, suffixEnd);
        String normalized = normalizeMatchedDimension(suffix);
        if (normalized != null) {
            return normalized;
        }

        int prefixStart = Math.max(0, start - DIMENSION_SEARCH_MARGIN);
        int prefixEnd = Math.min(text.length(), start);
        String prefix = text.substring(prefixStart, prefixEnd);
        return normalizeMatchedDimension(prefix);
    }

    private static String normalizeMatchedDimension(String text) {
        Matcher matcher = DIMENSION_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        return normalizeDimensionHint(matcher.group(1));
    }

    static final class DetectedCoordinate {
        final double x;
        final double y;
        final double z;
        final String dimensionId;
        final int matchStart;
        final int matchEnd;

        DetectedCoordinate(double x, double y, double z, String dimensionId, int matchStart, int matchEnd) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimensionId = dimensionId;
            this.matchStart = matchStart;
            this.matchEnd = matchEnd;
        }
    }

    private static final class Range {
        final int start;
        final int end;

        private Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
