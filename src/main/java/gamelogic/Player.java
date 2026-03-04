package gamelogic;

public class Player {

    private int hp = 10;
    private double baseSpeed = 1.0;
    private double currentSpeed = 1.0;

    private int bleedTurns = 0;
    private int slowTurns = 0;


    public int getHp() {
        return hp;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public double getSpeed() {
        return currentSpeed;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > 10) hp = 10;
    }


    public void applyBleed(int turns) {
        bleedTurns = turns;
    }

    public void applySlow(int turns) {
        slowTurns = turns;
    }

    public void updateStatus() {

        // BLEED
        if (bleedTurns > 0) {
            hp -= 1;
            bleedTurns--;
            if (hp < 0) hp = 0;
        }

        // SLOW
        if (slowTurns > 0) {
            currentSpeed = baseSpeed * 0.5;
            slowTurns--;
        } else {
            currentSpeed = baseSpeed;
        }
    }
}