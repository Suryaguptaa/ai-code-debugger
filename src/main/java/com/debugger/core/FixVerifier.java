package com.debugger.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class FixVerifier {

    public boolean verifies(Path originalFilePath, String fixedLineOfCode, int lineNumber) {
        try {

            List<String> lines = Files.readAllLines(originalFilePath);

            // 2. Apply the Fix (Replace the bad line)
            // Line numbers are 1-based, list is 0-based
            if (lineNumber > 0 && lineNumber <= lines.size()) {
                lines.set(lineNumber - 1, fixedLineOfCode);
            }

            // 3. Create a Temporary File (e.g., Demo_Test.java)
            String originalName = originalFilePath.getFileName().toString();
            String testFileName = originalName.replace(".java", "_Test.java");

            // We need to rename the class inside the file too, or Java complains!
            String className = originalName.replace(".java", "");
            String testClassName = className + "_Test";

            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, lines.get(i).replace("class " + className, "class " + testClassName));
            }

            Path testFile = originalFilePath.resolveSibling(testFileName);
            Files.write(testFile, lines);

            // 4. Run the Java Compiler
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                System.err.println("❌ Error: No Java Compiler found. Are you running with a JDK?");
                return false;
            }

            // Compile the temp file. 0 result means Success.
            int result = compiler.run(null, null, null, testFile.toString());

            // 5. Cleanup (Delete the temp file and the .class file)
            Files.deleteIfExists(testFile);
            Files.deleteIfExists(testFile.resolveSibling(testClassName + ".class"));

            return result == 0; // True if compiled successfully

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}