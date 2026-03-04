package com.zombiesurvival.gamelogic;

public class LevelManager {

    private int currentLevel;
    private int timeLimit;

    public void loadLevel(int level) {

        this.currentLevel = level;

        switch (level) {
            case 1:
                timeLimit = 30;
                break;
            case 2:
                timeLimit = 60;
                break;
            case 3:
                timeLimit = 90;
                break;
            default:
                throw new IllegalArgumentException("Invalid level");
        }
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}