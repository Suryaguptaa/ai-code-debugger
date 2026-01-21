# 🩺 AI Code Debugger (Java)

An intelligent CLI tool that debugs Java code using Generative AI. It scans source code for syntax errors, logic bugs, and concurrency issues, then auto-verifies the fixes using the local Java Compiler.



## 🚀 Features
* **Interactive Debugging:** Paste code directly into the terminal—no file setup required.
* **Multi-Bug Detection:** Identifies multiple issues (Syntax, Logic, Deadlocks) in a single pass.
* **Auto-Verification:** **Compiles the AI's fix** locally to ensure it works before suggesting it.
* **Powered by Gemini 2.5:** Uses Google's latest LLM for deep semantic analysis.

## 🛠️ Tech Stack
* **Language:** Java 17
* **AI Model:** Google Gemini 2.5 Flash (REST API)
* **CLI Framework:** Picocli
* **JSON Processing:** Jackson
* **Build Tool:** Maven

## 📦 Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/Suryaguptaa/ai-code-doctor.git](https://github.com/Suryaguptaa/ai-code-doctor.git)
    cd ai-code-doctor
    ```

2.  **Get a Gemini API Key**
    * Get a free key from [Google AI Studio](https://aistudio.google.com/).

3.  **Set the API Key**
    * **Windows (PowerShell):** `$env:GEMINI_KEY="your_key_here"`
    * **Mac/Linux:** `export GEMINI_KEY="your_key_here"`

4.  **Build & Run**
    ```bash
    mvn clean package
    java -jar target/ai-debugger-1.0-SNAPSHOT.jar
    ```

## 📸 Usage Example

**Input:**
```java
public class Demo {
    public static void main(String args) { // Error: Wrong signature
        int x = 5                          // Error: Missing semicolon
        System.out.println(x);
    }
}