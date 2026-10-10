package com.example.demo.util;

public final class WordNormalizer {
    private static final int MAX_WORD_LENGTH = 50;

    private WordNormalizer() {}

    /**
     * Trim, lowercase, keep letters only, truncate to max length.
     * Returns empty string if nothing remains.
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(Math.min(raw.length(), MAX_WORD_LENGTH));
        for (int i = 0; i < raw.length() && sb.length() < MAX_WORD_LENGTH; i++) {
            char c = raw.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) (c + 32));
            } else if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
