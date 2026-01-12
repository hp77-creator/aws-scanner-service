package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects US Social Security Numbers (SSN).
 * Pattern: XXX-XX-XXXX (where X is a digit)
 */
@Component
public class SsnDetector implements Detector {

    // SSN pattern: 3 digits, dash, 2 digits, dash, 4 digits
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    @Override
    public List<DetectorMatch> scan(String content) {
        List<DetectorMatch> matches = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return matches;
        }

        Matcher matcher = SSN_PATTERN.matcher(content);
        while (matcher.find()) {
            matches.add(new DetectorMatch(
                matcher.group(),
                matcher.start(),
                matcher.end()
            ));
        }

        return matches;
    }

    @Override
    public String getDetectorName() {
        return "SSN";
    }
}
