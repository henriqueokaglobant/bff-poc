package com.globant.study.utils;

public class Utils {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_MAGENTA = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static String red(String content) {
        return ANSI_RED + content + ANSI_RESET;
    }

    public static String green(String content) {
        return ANSI_GREEN + content + ANSI_RESET;
    }

    public static String yellow(String content) {
        return ANSI_YELLOW + content + ANSI_RESET;
    }

    public static String blue(String content) {
        return ANSI_BLUE + content + ANSI_RESET;
    }

    public static String magenta(String content) {
        return ANSI_MAGENTA + content + ANSI_RESET;
    }

    public static String cyan(String content) {
        return ANSI_CYAN + content + ANSI_RESET;
    }

    public static String white(String content) {
        return ANSI_WHITE + content + ANSI_RESET;
    }
}
