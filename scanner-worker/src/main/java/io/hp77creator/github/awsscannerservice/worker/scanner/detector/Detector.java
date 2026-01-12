package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import java.util.List;

/**
 * Interface for sensitive data detectors.
 * Each detector scans content for a specific type of sensitive information.
 */
public interface Detector {

    /**
     * Scans the provided content and returns a list of detected matches.
     * 
     * @param content The text content to scan
     * @return List of DetectorMatch objects containing match details
     */
    List<DetectorMatch> scan(String content);

    /**
     * Returns the name/type of this detector.
     * Used as the detector_type field in findings table.
     * 
     * @return Detector name (e.g., "SSN", "CREDIT_CARD", "AWS_ACCESS_KEY")
     */
    String getDetectorName();

    /**
    * Represents a single match found by a detector.
    */
    record DetectorMatch(String matchedValue, int startOffset, int endOffset) {

    }
}
