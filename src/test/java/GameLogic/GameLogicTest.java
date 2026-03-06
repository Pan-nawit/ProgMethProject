package Gamelogic;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.junit.jupiter.api.Assertions.*;


@DisplayName("GameLogic Tests")
class GameLogicTest {


    static class GameLogic {
        boolean isGameOver = false, isWon = false, isEndless;
        int wave = 1, score = 0, currentStage, stageDurationSeconds;
        long spawnCooldown, itemDropCooldown, stageStartTime;
        double finalElapsedSeconds = 0;


        GameLogic(int stage) {
            currentStage         = stage;
            isEndless            = (stage == 4);
            stageDurationSeconds = stage * 30;
            spawnCooldown = switch (stage) {
                case 1 -> 2000L; case 2 -> 1200L; case 3 -> 700L; case 4 -> 700L; default -> 2000L;
            };
            itemDropCooldown = isEndless ? 4000L : 8000L;
            stageStartTime   = System.currentTimeMillis();
        }


        double getElapsedSeconds() { return (System.currentTimeMillis() - stageStartTime) / 1000.0; }
        int calcWave(double elapsed) { return (int)(elapsed / 10) + 1; }
        boolean isTimeUp() { return !isEndless && getElapsedSeconds() >= stageDurationSeconds; }
        void killEnemy()   { score += 10 * (isEndless ? 4 : currentStage); }
    }


    // -- Initial State --


    @Test @DisplayName("isGameOver is false at start")
    void gameOverFalseAtStart() { assertFalse(new GameLogic(1).isGameOver); }


    @Test @DisplayName("isWon is false at start")
    void wonFalseAtStart() { assertFalse(new GameLogic(1).isWon); }


    @Test @DisplayName("score is 0 at start")
    void scoreZeroAtStart() { assertEquals(0, new GameLogic(1).score); }


    @Test @DisplayName("wave is 1 at start")
    void waveOneAtStart() { assertEquals(1, new GameLogic(1).wave); }


    @Test @DisplayName("finalElapsedSeconds is 0 at start")
    void finalElapsedZeroAtStart() { assertEquals(0.0, new GameLogic(1).finalElapsedSeconds, 0.001); }


    // -- Stage / Endless --


    @Test @DisplayName("Stages 1-3 are not endless")
    void nonEndlessStages() {
        assertFalse(new GameLogic(1).isEndless);
        assertFalse(new GameLogic(2).isEndless);
        assertFalse(new GameLogic(3).isEndless);
    }


    @Test @DisplayName("Stage 4 is endless mode")
    void stage4IsEndless() { assertTrue(new GameLogic(4).isEndless); }


    @ParameterizedTest(name = "Stage {0}: duration = {1}s")
    @CsvSource({"1,30", "2,60", "3,90", "4,120"})
    void stageDuration(int stage, int expectedSeconds) {
        assertEquals(expectedSeconds, new GameLogic(stage).stageDurationSeconds);
    }


    // -- Wave Calculation --


    @ParameterizedTest(name = "Elapsed {0}s -> Wave {1}")
    @CsvSource({"0,1", "5,1", "9,1", "10,2", "19,2", "20,3", "30,4", "50,6"})
    void waveFromElapsed(double elapsed, int expectedWave) {
        assertEquals(expectedWave, new GameLogic(1).calcWave(elapsed));
    }


    // -- Spawn Cooldown --


    @ParameterizedTest(name = "Stage {0}: spawnCooldown = {1}ms")
    @CsvSource({"1,2000", "2,1200", "3,700", "4,700"})
    void spawnCooldownPerStage(int stage, long expected) {
        assertEquals(expected, new GameLogic(stage).spawnCooldown);
    }


    @Test @DisplayName("Stage 3 and 4 have the same spawn rate")
    void stage3And4SameSpawnRate() {
        assertEquals(new GameLogic(3).spawnCooldown, new GameLogic(4).spawnCooldown);
    }


    @Test @DisplayName("Spawn rate accelerates each stage")
    void spawnAcceleratesOverStages() {
        assertTrue(new GameLogic(1).spawnCooldown > new GameLogic(2).spawnCooldown);
        assertTrue(new GameLogic(2).spawnCooldown > new GameLogic(3).spawnCooldown);
    }


    // -- Item Drop Cooldown --


    @Test @DisplayName("Normal stages: itemDropCooldown = 8000ms")
    void normalItemDropCooldown() {
        assertEquals(8000L, new GameLogic(1).itemDropCooldown);
        assertEquals(8000L, new GameLogic(2).itemDropCooldown);
        assertEquals(8000L, new GameLogic(3).itemDropCooldown);
    }


    @Test @DisplayName("Endless mode: itemDropCooldown = 4000ms (more frequent)")
    void endlessItemDropFaster() { assertEquals(4000L, new GameLogic(4).itemDropCooldown); }


    @Test @DisplayName("Endless item drop is faster than normal stages")
    void endlessItemDropFasterThanNormal() {
        assertTrue(new GameLogic(4).itemDropCooldown < new GameLogic(1).itemDropCooldown);
    }


    // -- Score --


    @ParameterizedTest(name = "Stage {0}: 1 kill = {1} score")
    @CsvSource({"1,10", "2,20", "3,30"})
    void scorePerKill(int stage, int expectedScore) {
        GameLogic gl = new GameLogic(stage); gl.killEnemy(); assertEquals(expectedScore, gl.score);
    }


    @Test @DisplayName("Endless mode: 1 kill = 40 score (4x multiplier)")
    void endlessScoreMultiplier() { GameLogic gl = new GameLogic(4); gl.killEnemy(); assertEquals(40, gl.score); }


    @Test @DisplayName("Score accumulates correctly over multiple kills")
    void scoreAccumulates() {
        GameLogic gl = new GameLogic(1);
        gl.killEnemy(); gl.killEnemy(); gl.killEnemy();
        assertEquals(30, gl.score);
    }


    @Test @DisplayName("Score multiplier increases with stage number")
    void scoreMultiplierScalesWithStage() {
        GameLogic g1 = new GameLogic(1); g1.killEnemy();
        GameLogic g2 = new GameLogic(2); g2.killEnemy();
        GameLogic g3 = new GameLogic(3); g3.killEnemy();
        assertTrue(g1.score < g2.score && g2.score < g3.score);
    }


    // -- Timer --


    @Test @DisplayName("isTimeUp is false immediately after init")
    void notTimeUpAtStart() { assertFalse(new GameLogic(1).isTimeUp()); }


    @Test @DisplayName("Endless mode: isTimeUp is never true")
    void endlessNeverTimesUp() {
        GameLogic gl = new GameLogic(4);
        gl.stageStartTime -= 999_999_999L;
        assertFalse(gl.isTimeUp());
    }


    @Test @DisplayName("getElapsedSeconds is non-negative")
    void elapsedNonNegative() { assertTrue(new GameLogic(1).getElapsedSeconds() >= 0); }


    @Test @DisplayName("getElapsedSeconds increases over time")
    void elapsedIncreases() throws InterruptedException {
        GameLogic gl = new GameLogic(1);
        double t1 = gl.getElapsedSeconds();
        Thread.sleep(50);
        assertTrue(gl.getElapsedSeconds() > t1);
    }
}

