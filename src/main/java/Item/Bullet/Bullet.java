package Item.Bullet;

import java.awt.*;

public class Bullet {
    private int x, y;
    private int speed;
    private int damage;
    private int dirX, dirY;
    private String type;
    public Bullet(int x, int y, int dirX, int dirY, int speed, int damage, String type) {
        this.x = x;
        this.y = y;
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.damage = damage;
        this.type = type;
    }
    public void update() {
        x += dirX * speed;
        y += dirY * speed;
    }
    public Rectangle getBounds() {
        return new Rectangle(x, y, 4, 4);
    }
    public int getDamage() { return damage; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
}
// ตัวอย่าง: GamePanel.addBullet(bullet);
