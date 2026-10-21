
package com.digitalsignal;

public final class Palindrome {
    private Palindrome() {}

    public static String longest(String s) {
        if (s == null || s.isEmpty()) return "";

        int n = s.length();
        int[] odd = new int[n];
        int left = 0, right = -1;
        int bestStart = 0, bestLength = 1;

        // Odd-length palindromes.
        for (int i = 0; i < n; i++) {
            int k = (i > right) ? 1
                    : Math.min(odd[left + right - i], right - i + 1);

            while (i - k >= 0 && i + k < n
                    && s.charAt(i - k) == s.charAt(i + k)) k++;

            odd[i] = k;
            int length = 2 * k - 1;
            if (length > bestLength) {
                bestLength = length;
                bestStart = i - k + 1;
            }

            if (i + k - 1 > right) {
                left = i - k + 1;
                right = i + k - 1;
            }
        }

        int[] even = new int[n];
        left = 0;
        right = -1;

        // Even-length palindromes.
        for (int i = 0; i < n; i++) {
            int k = (i > right) ? 0
                    : Math.min(even[left + right - i + 1], right - i + 1);

            while (i - k - 1 >= 0 && i + k < n
                    && s.charAt(i - k - 1) == s.charAt(i + k)) k++;

            even[i] = k;
            int length = 2 * k;
            if (length > bestLength) {
                bestLength = length;
                bestStart = i - k;
            }

            if (i + k - 1 > right) {
                left = i - k;
                right = i + k - 1;
            }
        }

        return s.substring(bestStart, bestStart + bestLength);
    }
}

