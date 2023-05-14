package com.example.assignmentdistributed.server;

public enum Champion {
    CHAMPION_1(new int[] {50, 75, 100, 125, 150, 175, 200, 225, 250, 275}, new double[] {10, 12, 14, 16, 18, 20, 22, 24, 26, 28}),
    CHAMPION_2(new int[] {75, 100, 125, 150, 175, 200, 225, 250, 275, 300}, new double[] {12, 14, 16, 18, 20, 22, 24, 26, 28, 30}),
    CHAMPION_3(new int[] {100, 125, 150, 175, 200, 225, 250, 275, 300, 325}, new double[] {14, 16, 18, 20, 22, 24, 26, 28, 30, 32}),
    CHAMPION_4(new int[] {125, 150, 175, 200, 225, 250, 275, 300, 325, 350}, new double[] {16, 18, 20, 22, 24, 26, 28, 30, 32, 34}),
    CHAMPION_5(new int[] {150, 175, 200, 225, 250, 275, 300, 325, 350, 375}, new double[] {18, 20, 22, 24, 26, 28, 30, 32, 34, 36});

    private final int[] healthLevels;
    private final double[] damageLevels;

    private Champion(int[] healthLevels, double[] damageLevels) {
        this.healthLevels = healthLevels;
        this.damageLevels = damageLevels;
    }

    public int getHealth(int level) {
        if (level > healthLevels.length) {
            level = healthLevels.length; // default to maximum level if level is too high
        }
        return healthLevels[level - 1];
    }

    public int getDamage(int level) {
        if (level > damageLevels.length) {
            level = damageLevels.length; // default to maximum level if level is too high
        }
        return (int) damageLevels[level - 1];
    }
}