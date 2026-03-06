package Item.Bullet;

import java.awt.Rectangle;

public class Bullet {
    private float x, y;
    private double dx, dy;
    private int speed;
    private int damage;
    private String ownerName;

    /** Used by Gun subclasses — dx/dy as int direction vector */
    public Bullet(int x, int y, double dx, double dy, int speed, int damage, String ownerName) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.speed = speed;
        this.damage = damage;
        this.ownerName = ownerName;
    }

    public void update() {
        x += dx * speed;
        y += dy * speed;
    }

    public Rectangle getBounds() { return new Rectangle((int)x - 3, (int)y - 3, 6, 6); }
    public int getX()        { return (int) x; }
    public int getY()        { return (int) y; }
    public int getDamage()   { return damage; }
    public String getOwnerName() { return ownerName; }
}