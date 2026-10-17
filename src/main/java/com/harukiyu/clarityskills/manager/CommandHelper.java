package com.harukiyu.clarityskills.manager;

import org.jetbrains.annotations.NotNull;

public final class CommandHelper {
    private CommandHelper() {}

    public static long parseDurationMillis(@NotNull String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent") || input.equals("-1")) {
            return -1;
        }

        try {
            long totalMillis = 0;
            String lower = input.toLowerCase();
            StringBuilder num = new StringBuilder();

            for (int i = 0; i < lower.length(); i++) {
                char c = lower.charAt(i);
                if (Character.isDigit(c)) {
                    num.append(c);
                } else {
                    if (num.length() == 0) continue;
                    long val = Long.parseLong(num.toString());
                    num.setLength(0);

                    switch (c) {
                        case 's' -> totalMillis += val * 1000L;
                        case 'm' -> totalMillis += val * 60L * 1000L;
                        case 'h' -> totalMillis += val * 3600L * 1000L;
                        case 'd' -> totalMillis += val * 86400L * 1000L;
                        default -> totalMillis += val * 1000L;
                    }
                }
            }

            if (num.length() > 0) {
                totalMillis += Long.parseLong(num.toString()) * 1000L;
            }

            return totalMillis;
        } catch (Exception e) {
            return 0;
        }
    }
}
