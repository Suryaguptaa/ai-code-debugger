package com.debugger.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class SourceCodeReader {
    private static final int CONTEXT_LINES = 5; // How many lines before/after to read

    /**
     * Finds the file and reads the code around the error line.
     */
    public String readContext(String fileName, int lineNumber) throws IOException {
        // 1. Find the file path
        Path projectRoot = Paths.get(System.getProperty("user.dir")); // Start searching from current folder
        Path filePath = findFile(projectRoot, fileName);

        if (filePath == null) {
            return "File not found: " + fileName;
        }

        // 2. Read lines and extract the window
        List<String> allLines = Files.readAllLines(filePath);
        return extractLines(allLines, lineNumber);
    }

    private Path findFile(Path root, String fileName) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElse(null);
        }
    }

    private String extractLines(List<String> lines, int targetLine) {
        // Line numbers are 1-based, list indices are 0-based
        int targetIndex = targetLine - 1;

        // Calculate safe start and end indices
        int start = Math.max(0, targetIndex - CONTEXT_LINES);
        int end = Math.min(lines.size(), targetIndex + CONTEXT_LINES + 1);

        StringBuilder snippet = new StringBuilder();
        for (int i = start; i < end; i++) {
            snippet.append(i + 1) // Add line numbers for the AI
                    .append(i == targetIndex ? " -> " : "    ") // Mark the error line
                    .append(lines.get(i))
                    .append("\n");
        }
        return snippet.toString();
    }
}