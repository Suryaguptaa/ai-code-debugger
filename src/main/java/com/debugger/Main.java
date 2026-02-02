package com.debugger;

import com.debugger.core.LlmClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

@Command(name = "ai-doctor", mixinStandardHelpOptions = true, version = "4.1",
        description = "Interactive AI Code Doctor (Robust Edition).")
public class Main implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, description = "Path to the Java file (Optional).", required = false)
    private Path sourceFilePath;

    @Option(names = {"-d", "--desc"}, description = "Optional description of the bug.")
    private String bugDescription;

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    @Override
    public Integer call() {
        try {
            System.out.println("\n" + BOLD + "🩺 AI Code Doctor (Robust Edition)" + RESET);

            String fullCode = "";
            Path fileToDebug = sourceFilePath;

            // 1. INPUT HANDLING
            if (fileToDebug != null) {
                if (!Files.exists(fileToDebug)) {
                    System.err.println(RED + "❌ File not found: " + fileToDebug + RESET);
                    return 1;
                }
                System.out.println(CYAN + "📂 Reading File:   " + RESET + fileToDebug.getFileName());
                fullCode = Files.readString(fileToDebug);
            } else {
                fullCode = readFromConsole();
                if (fullCode.trim().isEmpty()) {
                    System.out.println(RED + "❌ No code provided." + RESET);
                    return 1;
                }
                fileToDebug = saveToTempFile(fullCode);
                System.out.println(CYAN + "💾 Code saved: " + RESET + fileToDebug.getFileName());
            }

            // 2. AI ANALYSIS
            System.out.print(YELLOW + "🧠 Scanning code... " + RESET);
            LlmClient aiClient = new LlmClient();
            String rawResponse = aiClient.analyzeBug(bugDescription, fullCode);
            System.out.println(GREEN + "Done!" + RESET);

            // 3. ROBUST JSON EXTRACTION (The Fix)
            String jsonArray = extractJsonArray(rawResponse);

            if (jsonArray == null) {
                System.out.println(RED + "❌ Could not find valid JSON in AI response." + RESET);
                System.out.println("Raw output: " + rawResponse);
                return 1;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode bugList = mapper.readTree(jsonArray);

            int bugCount = bugList.size();
            System.out.println("\n" + "=".repeat(50));
            if (bugCount == 0) {
                System.out.println(GREEN + "✅ NO BUGS FOUND! Your code looks clean." + RESET);
                System.out.println("=".repeat(50));
                return 0;
            }

            System.out.println(BOLD + "💡 FOUND " + bugCount + (bugCount == 1 ? " ISSUE" : " ISSUES") + RESET);
            System.out.println("=".repeat(50));

            List<String> codeLines = Files.readAllLines(fileToDebug);

            // 4. DISPLAY & APPLY FIXES
            for (JsonNode bug : bugList) {
                String explanation = bug.has("explanation") ? bug.get("explanation").asText() : "No details.";
                int lineNum = bug.has("lineNumber") ? bug.get("lineNumber").asInt() : -1;
                String fix = bug.has("fixedCode") ? bug.get("fixedCode").asText() : "";

                if (lineNum != -1) {
                    System.out.println(CYAN + "📍 Line " + lineNum + ": " + RESET + explanation);
                    System.out.println(GREEN + "   Fix: " + fix + RESET);

                    if (lineNum > 0 && lineNum <= codeLines.size()) {
                        String originalLine = codeLines.get(lineNum - 1);
                        if (originalLine.trim().endsWith("{") && !fix.trim().endsWith("{")) {
                            fix = fix + " {";
                        }
                        codeLines.set(lineNum - 1, fix);
                    }
                }
            }
            System.out.println("=".repeat(50));

            // 5. CUMULATIVE VERIFICATION
            System.out.print("\n" + YELLOW + "🧪 Verifying fixes... " + RESET);
            Files.write(fileToDebug, codeLines);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler != null) {
                int result = compiler.run(null, null, null, fileToDebug.toString());
                if (result == 0) {
                    System.out.println(GREEN + "✅ PASS" + RESET);
                } else {
                    System.out.println(RED + "❌ FAIL" + RESET);
                }
            } else {
                System.out.println(RED + "❌ No Compiler found." + RESET);
            }

            // Cleanup
            if (sourceFilePath == null && fileToDebug != null) {
                Files.deleteIfExists(fileToDebug);
                Files.deleteIfExists(Path.of(fileToDebug.toString().replace(".java", ".class")));
            }

            return 0;

        } catch (Exception e) {
            System.err.println(RED + "\n💥 Error: " + e.getMessage() + RESET);
            return 1;
        }
    }



    private String extractJsonArray(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private String readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        StringBuilder codeBuffer = new StringBuilder();
        System.out.println(GREEN + "👇 Please paste your Java code below." + RESET);
        System.out.println(CYAN + "   (Type 'END' on a new line when finished)" + RESET);
        System.out.println("-".repeat(40));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().equals("END")) break;
            codeBuffer.append(line).append("\n");
        }
        return codeBuffer.toString();
    }

    private Path saveToTempFile(String code) throws IOException {
        String className = "TempDebug";
        Matcher matcher = Pattern.compile("class\\s+(\\w+)").matcher(code);
        if (matcher.find()) className = matcher.group(1);
        Path tempFile = Path.of(className + ".java");
        Files.writeString(tempFile, code, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return tempFile;
    }
}