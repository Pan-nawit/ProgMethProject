package com.zombiesurvival.gamelogic;

import com.zombiesurvival.enemy.Enemy;

import java.util.ArrayList;
import java.util.List;

public class SpawnManager {

    private final List<Enemy> activeEnemies = new ArrayList<>();

    public void addEnemy(Enemy enemy) {
        activeEnemies.add(enemy);
    }

    public List<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void removeDeadEnemies() {
        activeEnemies.removeIf(Enemy::isDead);
    }
}