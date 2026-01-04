package com.debugger.model;

public record ErrorContext(
        String exceptionType, // e.g., java.lang.NullPointerException
        String fileName,      // e.g., UserService.java
        int lineNumber        // e.g., 42
) {}