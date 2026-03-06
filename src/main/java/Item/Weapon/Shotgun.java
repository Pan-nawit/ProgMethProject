package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Item.Item;
import Player.Player;

/** Shotgun — 2 shots, 3 pellets per shot */
public class Shotgun extends Gun {
    public Shotgun() {
        super("Shotgun", 10, 3, 1000);
    }

    @Override
    public double getRecoilAmount() { return 7.5; }

    @Override
    public void shoot(Player player) {
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;
        double ddx = targetX - cx, ddy = targetY - cy;
        double len = Math.sqrt(ddx * ddx + ddy * ddy);
        if (len == 0) return;
        ddx /= len; ddy /= len;
        double[] dir = applyRecoil(ddx, ddy);
        double fx = dir[0], fy = dir[1];
        double perpX = -fy, perpY = fx;

        GameLogic.addBullet(new Bullet((int) cx, (int) cy, fx, fy, 15, damage, name));
        GameLogic.addBullet(new Bullet((int) cx, (int) cy, fx + perpX * 0.3, fy + perpY * 0.3, 15, damage, name));
        GameLogic.addBullet(new Bullet((int) cx, (int) cy, fx - perpX * 0.3, fy - perpY * 0.3, 15, damage, name));

        player.applyKnockback(5);
        player.addRecoil(getRecoilAmount());
        Item.playGunSound();
    }
}