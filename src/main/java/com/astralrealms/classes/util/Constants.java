package com.astralrealms.classes.util;

import lombok.experimental.UtilityClass;

/**
 * Centralized constants for magic numbers.
 * Documents intent and improves maintainability.
 */
@UtilityClass
public final class Constants {

    /**
     * Display constants for damage indicators and similar visual elements.
     */
    public static final class Display {
        /**
         * How far in front of the face to spawn damage indicators.
         */
        public static final double DISTANCE = 2.3;

        /**
         * How far to the left to spawn damage indicators.
         */
        public static final double SIDE_OFFSET = 0.8;

        /**
         * How high above the eyes to spawn damage indicators.
         */
        public static final double HEIGHT_OFFSET = 0.2;

        /**
         * Scale for damage indicator text.
         */
        public static final float TEXT_SCALE = 0.75f;

        /**
         * Milliseconds before cleaning up damage indicator displays.
         */
        public static final long CLEANUP_MS = 600L;
    }

}
