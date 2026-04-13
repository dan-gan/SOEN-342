package com.taskmanager.cli;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUtils {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int PAGE_SIZE = 20;

    // ANSI colors
    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GRAY   = "\u001B[90m";
    private static final String BOLD   = "\u001B[1m";

    private static final String ANSI_PATTERN = "\u001B\\[[\\d;]*m";

    public static String colorPriority(String priority) {
        return switch (priority) {
            case "HIGH"   -> BOLD + RED    + priority + RESET;
            case "MEDIUM" -> BOLD + YELLOW + priority + RESET;
            case "LOW"    -> BOLD + GREEN  + priority + RESET;
            default       -> priority;
        };
    }

    public static String colorStatus(String status) {
        return switch (status) {
            case "OPEN"      -> BOLD + CYAN + status + RESET;
            case "COMPLETED" -> BOLD + GREEN + status + RESET;
            case "CANCELLED" -> GRAY + status + RESET;
            default          -> status;
        };
    }

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("  Input cannot be empty. Please try again.");
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) return val;
            System.out.println("  Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static Optional<LocalDate> readOptionalDate(String prompt) {
        System.out.print(prompt + " (YYYY-MM-DD, or blank to skip): ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return Optional.empty();
        try {
            return Optional.of(LocalDate.parse(line));
        } catch (DateTimeParseException e) {
            System.out.println("  Invalid date format. Skipping.");
            return Optional.empty();
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("  Please enter y or n.");
        }
    }

    public static void printTable(List<String> headers, List<List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) widths[i] = headers.get(i).length();
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(row.size(), widths.length); i++) {
                String cell = row.get(i);
                if (cell != null) {
                    int vlen = visibleLength(cell);
                    if (vlen > widths[i]) widths[i] = Math.min(vlen, 40);
                }
            }
        }

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildRow(headers, widths));
        System.out.println(separator);

        if (rows.isEmpty()) {
            System.out.println("  (no results)");
        } else {
            int page = 0;
            int total = rows.size();
            while (page * PAGE_SIZE < total) {
                int start = page * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, total);
                for (int i = start; i < end; i++) {
                    System.out.println(buildRow(rows.get(i), widths));
                }
                if (end < total) {
                    System.out.print("  Showing " + end + " of " + total + " rows. Press Enter for more...");
                    scanner.nextLine();
                }
                page++;
            }
        }
        System.out.println(separator);
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        return sb.toString();
    }

    private static String buildRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.size() && cells.get(i) != null) ? cells.get(i) : "";
            int vlen = visibleLength(cell);
            if (vlen > 40) cell = stripAnsi(cell).substring(0, 37) + "...";
            // Pad based on visible length so ANSI codes don't shift columns
            int pad = widths[i] - visibleLength(cell);
            sb.append(" ").append(cell).append(" ".repeat(Math.max(0, pad))).append(" |");
        }
        return sb.toString();
    }

    private static int visibleLength(String s) {
        return s.replaceAll(ANSI_PATTERN, "").length();
    }

    private static String stripAnsi(String s) {
        return s.replaceAll(ANSI_PATTERN, "");
    }

    public static void println(String msg) { System.out.println(msg); }
    public static void print(String msg) { System.out.print(msg); }

    public static void printError(String msg) { System.out.println("[ERROR] " + msg); }
    public static void printSuccess(String msg) { System.out.println("[OK] " + msg); }
    public static void printWarning(String msg) { System.out.println("[WARN] " + msg); }
}
