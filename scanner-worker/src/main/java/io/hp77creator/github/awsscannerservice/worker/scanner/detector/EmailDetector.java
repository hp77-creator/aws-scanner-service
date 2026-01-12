package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects email addresses.
 * Pattern: Basic email validation format
 */
@Component
public class EmailDetector implements Detector {

    // Email pattern: username@domain.tld
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[\\w\\.-]+@[\\w\\.-]+\\.\\w+\\b"
    );

    @Override
    public List<DetectorMatch> scan(String content) {
        List<DetectorMatch> matches = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return matches;
        }

        Matcher matcher = EMAIL_PATTERN.matcher(content);
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
        return "EMAIL";
    }
}
