package com.debugger.core;

import com.debugger.model.ErrorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

public class LogParser {


    private static final Pattern TRACE_PATTERN = Pattern.compile(
            "at\\s+([\\w.$]+)\\(([^:]+\\.java):(\\d+)\\)"
    );

    public ErrorContext parse(String logContent) {
        // We split by new lines to check line-by-line
        String[] lines = logContent.split("\\R");

        String exceptionType = "Unknown Exception";


        if (lines.length > 0) {
            String firstLine = lines[0];
            if (firstLine.contains("Exception") || firstLine.contains("Error")) {
                exceptionType = firstLine.split(":")[0].trim();
            }
        }

        for (String line : lines) {
            Matcher matcher = TRACE_PATTERN.matcher(line.trim());
            if (matcher.find()) {
                String className = matcher.group(1);
                String fileName = matcher.group(2);
                int lineNumber = Integer.parseInt(matcher.group(3));

                // Return the first match we find (usually the root cause in simple logs)
                return new ErrorContext(exceptionType, fileName, lineNumber);
            }
        }

        throw new IllegalArgumentException("Could not find a valid stack trace in the logs.");
    }
}