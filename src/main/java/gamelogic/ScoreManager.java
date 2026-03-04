package com.zombiesurvival.gamelogic;

public class ScoreManager {

    private int killCount = 0;

    public void addKill() {
        killCount++;
    }

    public int getKillCount() {
        return killCount;
    }

    public void reset() {
        killCount = 0;
    }
}