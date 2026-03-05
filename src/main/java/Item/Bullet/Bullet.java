package Item.Bullet;

import java.awt.Rectangle;

public class Bullet {
    private float x, y;
    private float dx, dy;
    private int speed;
    private int damage;
    private String ownerName;

    /** Used by Gun subclasses — dx/dy as int direction vector */
    public Bullet(int x, int y, int dx, int dy, int speed, int damage, String ownerName) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.speed = speed;
        this.damage = damage;
        this.ownerName = ownerName;
    }

    /** Used by simple char-direction code */
    public Bullet(int x, int y, char direction, int damage) {
        this.x = x;
        this.y = y;
        this.speed = 12;
        this.damage = damage;
        this.ownerName = "";
        switch (Character.toLowerCase(direction)) {
            case 'w' -> { this.dx =  0; this.dy = -1; }
            case 's' -> { this.dx =  0; this.dy =  1; }
            case 'a' -> { this.dx = -1; this.dy =  0; }
            case 'd' -> { this.dx =  1; this.dy =  0; }
        }
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