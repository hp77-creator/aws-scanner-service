package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import io.hp77creator.github.awsscannerservice.worker.scanner.util.LuhnValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects credit card numbers using regex pattern matching and Luhn validation.
 * Matches 13-19 digit card numbers with optional spaces or dashes between groups.
 */
@Component
public class CreditCardDetector implements Detector {

    // Credit card pattern: 13-19 digits with optional spaces/dashes
    // Matches patterns like: 4532-1234-5678-9010, 4532 1234 5678 9010, 4532123456789010
    private static final Pattern CC_PATTERN = Pattern.compile(
        "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{3,7}\\b"
    );

    @Override
    public List<DetectorMatch> scan(String content) {
        List<DetectorMatch> matches = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return matches;
        }

        Matcher matcher = CC_PATTERN.matcher(content);
        while (matcher.find()) {
            String match = matcher.group();
            
            // Validate using Luhn algorithm to reduce false positives
            if (LuhnValidator.validate(match)) {
                matches.add(new DetectorMatch(
                    match,
                    matcher.start(),
                    matcher.end()
                ));
            }
        }

        return matches;
    }

    @Override
    public String getDetectorName() {
        return "CREDIT_CARD";
    }
}
