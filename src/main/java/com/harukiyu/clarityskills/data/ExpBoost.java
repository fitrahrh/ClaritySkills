package com.harukiyu.clarityskills.data;

import org.jetbrains.annotations.NotNull;

public class ExpBoost {
    private double multiplier;
    private long expiryTimestamp; // -1 for permanent, or System.currentTimeMillis() + duration

    public ExpBoost(double multiplier, long durationMillis) {
        this.multiplier = Math.max(1.0, multiplier);
        if (durationMillis <= 0) {
            this.expiryTimestamp = -1; // Permanent
        } else {
            this.expiryTimestamp = System.currentTimeMillis() + durationMillis;
        }
    }

    public ExpBoost(double multiplier, long expiryTimestamp, boolean isAbsoluteTimestamp) {
        this.multiplier = Math.max(1.0, multiplier);
        this.expiryTimestamp = expiryTimestamp;
    }

    public double getMultiplier() {
        if (!isActive()) {
            return 1.0;
        }
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = Math.max(1.0, multiplier);
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public boolean isPermanent() {
        return expiryTimestamp == -1;
    }

    public boolean isActive() {
        if (multiplier <= 1.0) return false;
        if (isPermanent()) return true;
        return System.currentTimeMillis() < expiryTimestamp;
    }

    public long getRemainingMillis() {
        if (!isActive()) return 0;
        if (isPermanent()) return Long.MAX_VALUE;
        return Math.max(0, expiryTimestamp - System.currentTimeMillis());
    }

    @NotNull
    public String getFormattedRemaining() {
        if (!isActive()) return "0s";
        if (isPermanent()) return "Permanent";

        long seconds = getRemainingMillis() / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02dh %02dm %02ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%02dm %02ds", minutes, secs);
        } else {
            return String.format("%02ds", secs);
        }
    }
}
