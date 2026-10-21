
package com.digitalsignal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PalindromeTest {

    @Test
    void findsEvenPalindrome() {
        assertEquals("100001", Palindrome.longest("100001011"));
    }

    @Test
    void findsWholeString() {
        assertEquals("101101", Palindrome.longest("101101"));
    }

    @Test
    void handlesRepeatedBits() {
        assertEquals("1111", Palindrome.longest("1111"));
    }

    @Test
    void handlesSingleBit() {
        assertEquals("1", Palindrome.longest("1"));
    }

    @Test
    void handlesEmptyString() {
        assertEquals("", Palindrome.longest(""));
    }
}
