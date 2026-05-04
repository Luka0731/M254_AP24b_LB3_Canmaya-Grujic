package ch.concertticketwatcherengine.core.util;

public class Log {

    // |----- helper method -----|

    private static void printlnStyled(String text, ConsoleStyle style, String logType) {
        System.out.println(style.format("[" + logType + "]") + " -> " + text);
    }

    // |----- log methods -----|

    public static void error(String text) {
        printlnStyled(text + " !", ConsoleStyle.RED, "ERROR");
    }

    public static void info(String text) {
        printlnStyled(text, ConsoleStyle.BLUE, "INFO");
    }

    public static void success(String text) {
        printlnStyled(text, ConsoleStyle.GREEN, "SUCCESS");
    }

    public static void debug(String text) {
        printlnStyled(text, ConsoleStyle.YELLOW, "DEBUG");
    }
}
