package io.hp77creator.github.awsscannerservice.worker.scanner.detector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects AWS Access Keys.
 * Pattern: AKIA followed by 16 alphanumeric characters
 */
@Component
public class AwsKeyDetector implements Detector {

    // AWS Access Key pattern: AKIA + 16 uppercase alphanumeric chars
    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("AKIA[0-9A-Z]{16}");

    @Override
    public List<DetectorMatch> scan(String content) {
        List<DetectorMatch> matches = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return matches;
        }

        Matcher matcher = AWS_KEY_PATTERN.matcher(content);
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
        return "AWS_ACCESS_KEY";
    }
}
