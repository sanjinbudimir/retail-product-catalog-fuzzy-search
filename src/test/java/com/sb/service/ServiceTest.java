package com.sb.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchTest {

    @Test
    void testIdenticalStrings() {
        assertEquals(0, Search.distance("test", "test"));
    }

    @Test
    void testOneEdit() {
        assertEquals(1, Search.distance("test", "tost"));
    }

    @Test
    void testOneTransposition() {
        assertEquals(1, Search.distance("test", "tets"));
    }

    @Test
    void testCompletelyDifferent() {
        assertEquals(3, Search.distance("abc", "xyz"));
    }

    @Test
    void testEmptyStrings() {
        assertEquals(0, Search.distance("", ""));
    }

    @Test
    void testEmptyAndNonEmpty() {
        assertEquals(3, Search.distance("", "abc"));
    }

    @Test
    void testLongStrings() {
        assertEquals(3, Search.distance("kitten", "sitting"));
    }

    @Test
    void testNullFirstString() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> Search.distance(null, "test"));
        assertEquals("Input strings cannot be null", ex.getMessage());
    }

    @Test
    void testNullSecondString() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> Search.distance("test", null));
        assertEquals("Input strings cannot be null", ex.getMessage());
    }

    @Test
    void testCaseSensitivity() {
        assertEquals(1, Search.distance("Test", "test"));
    }

    @Test
    void testWhitespaceHandling() {
        assertEquals(1, Search.distance("test", "test "));
    }

    @Test
    void testRepeatedCharacters() {
        assertEquals(1, Search.distance("banana", "bananna"));
    }
}
