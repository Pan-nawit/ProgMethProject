package enemy;

public interface Enemy {

    int getHp();
    double getSpeed();
    int getDamage();
    String getSpecialEffect();

    void takeDamage(int damage);
    boolean isDead();
}