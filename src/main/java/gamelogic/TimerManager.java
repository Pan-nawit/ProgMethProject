package com.zombiesurvival.gamelogic;

public class TimerManager {

    private int timeRemaining;

    public void start(int seconds) {
        timeRemaining = seconds;
    }

    public void tick() {
        if (timeRemaining > 0) {
            timeRemaining--;
        }
    }

    public boolean isTimeUp() {
        return timeRemaining <= 0;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }
}