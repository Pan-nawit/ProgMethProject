package enemy;

import Interface.Attackable;
import Player.Player;

import java.awt.*;

public abstract class BaseEnemy implements Attackable {
    protected int hp;
    protected int maxHp;
    protected int speed;
    protected int strength;

    protected float x, y;
    protected int width, height;
    protected boolean isHit = false;

    protected long lastAttackTime = 0;
    protected long attackCooldown = 1000;
    public BaseEnemy(int maxHp, int speed, int strength, float startX, float startY) {
        this.maxHp = maxHp;
        setHP(maxHp);
        this.speed = speed;
        this.strength = strength;
        this.x = startX;
        this.y = startY;
        setHeight(32);
        setWidth(32);
    }
    public void update(Player player) {
        if (isDead()) return;

        moveTowards(player);

        Attackplayer(player);

        isHit = false;
    }
    protected void moveTowards(Player p) {
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        float distance = (float) Math.sqrt((diffX * diffX) + (diffY * diffY));

        if (distance > 1) {
            x += (diffX / distance) * speed/5;
            y += (diffY / distance) * speed/5;
        }
    }
    protected boolean isCooldownReady() {
        return System.currentTimeMillis() - lastAttackTime > attackCooldown;
    }

    protected void resetCooldown() {
        lastAttackTime = System.currentTimeMillis();
    }
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    public void takeDamage(int dmg) {
        this.hp -= dmg;
        this.isHit = true;
    }

    public boolean isDead() {
        return hp <= 0;
    }
    public int getHP() {
        return hp;
    }
    public void setHP(int HP) {
        this.hp = HP;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getX(){
        return (int)x;
    }
    public int getY(){
        return (int)y;
    }

    public int getMaxHp() { return maxHp; }

    public javafx.scene.paint.Color getEnemyColor() {
        return javafx.scene.paint.Color.web("#c0392b");
    }

}