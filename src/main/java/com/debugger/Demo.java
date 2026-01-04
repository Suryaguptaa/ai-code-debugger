package com.debugger;

public class Demo {
    // Just some dummy lines to fill space
    public void start() {
        System.out.println("Starting app...");
        System.out.println("Loading data...");
    }

    public void processData() {
        String s = null;
        // This is line 15, where our fake error points to
        System.out.println(s.length());
    }

    public void end() {
        System.out.println("Ending app...");
    }
}