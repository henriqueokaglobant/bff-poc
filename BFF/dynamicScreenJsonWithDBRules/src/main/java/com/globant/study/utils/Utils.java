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
        return redNoReset(content) + ANSI_RESET;
    }

    public static String redNoReset(String content) {
        return ANSI_RED + content;
    }

    public static String green(String content) {
        return greenNoReset(content) + ANSI_RESET;
    }

    public static String greenNoReset(String content) {
        return ANSI_GREEN + content;
    }

    public static String yellow(String content) {
        return yellowNoReset(content) + ANSI_RESET;
    }

    public static String yellowNoReset(String content) {
        return ANSI_YELLOW + content;
    }

    public static String blue(String content) {
        return blueNoReset(content) + ANSI_RESET;
    }

    public static String blueNoReset(String content) {
        return ANSI_BLUE + content;
    }

    public static String magenta(String content) {
        return magentaNoReset(content) + ANSI_RESET;
    }

    public static String magentaNoReset(String content) {
        return ANSI_MAGENTA + content;
    }

    public static String cyan(String content) {
        return cyanNoReset(content) + ANSI_RESET;
    }

    public static String cyanNoReset(String content) {
        return ANSI_CYAN + content;
    }

    public static String white(String content) {
        return whiteNoReset(content) + ANSI_RESET;
    }

    public static String whiteNoReset(String content) {
        return ANSI_WHITE + content;
    }

    public static String resetColor(String content) {
        return content + ANSI_RESET;
    }
}
