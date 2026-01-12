package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects US phone numbers in various formats.
 * Patterns: (XXX) XXX-XXXX, XXX-XXX-XXXX, XXX.XXX.XXXX, XXXXXXXXXX
 */
@Component
public class PhoneDetector implements Detector {

    // US Phone pattern: 10 digits with optional separators
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b\\d{3}[-\\.\\s]?\\d{3}[-\\.\\s]?\\d{4}\\b"
    );

    @Override
    public List<DetectorMatch> scan(String content) {
        List<DetectorMatch> matches = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return matches;
        }

        Matcher matcher = PHONE_PATTERN.matcher(content);
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
        return "US_PHONE";
    }
}
