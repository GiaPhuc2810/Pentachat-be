package com.hdtpt.pentachat.util;

/**
 * Utility class for generating unique IDs
 */
public class IdGenerator {
    /**
     * Generate a unique ID using UUID
     * 
     * @return unique string ID
     */
    public static Long generateId() {
        // Simple long ID generation for testing/mock purposes
        // In real JPA production, @GeneratedValue(strategy = GenerationType.IDENTITY)
        // is used
        return System.currentTimeMillis() + (long) (Math.random() * 1000);
    }
}
