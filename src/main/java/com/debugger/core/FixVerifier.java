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

            if (lineNumber > 0 && lineNumber <= lines.size()) {
                lines.set(lineNumber - 1, fixedLineOfCode);
            }


            String originalName = originalFilePath.getFileName().toString();
            String testFileName = originalName.replace(".java", "_Test.java");

            String className = originalName.replace(".java", "");
            String testClassName = className + "_Test";

            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, lines.get(i).replace("class " + className, "class " + testClassName));
            }

            Path testFile = originalFilePath.resolveSibling(testFileName);
            Files.write(testFile, lines);

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                System.err.println("❌ Error: No Java Compiler found. Are you running with a JDK?");
                return false;
            }

            int result = compiler.run(null, null, null, testFile.toString());

            Files.deleteIfExists(testFile);
            Files.deleteIfExists(testFile.resolveSibling(testClassName + ".class"));

            return result == 0;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}