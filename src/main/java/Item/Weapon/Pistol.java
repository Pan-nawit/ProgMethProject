package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Item.Item;
import Player.Player;

/** Pistol — 30 shots, fires 1 bullet per click */
public class Pistol extends Gun {
    public Pistol() {
        super("Pistol", 30, 1, 400);
    }

    @Override
    public double getRecoilAmount() { return 6.25; }

    @Override
    public void shoot(Player player) {
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;
        double ddx = targetX - cx, ddy = targetY - cy;
        double len = Math.sqrt(ddx * ddx + ddy * ddy);
        if (len == 0) return;
        ddx /= len; ddy /= len;
        double[] dir = applyRecoil(ddx, ddy);
        GameLogic.addBullet(new Bullet((int) cx, (int) cy, dir[0], dir[1], 20, damage, name));
        player.addRecoil(getRecoilAmount());
        Item.playGunSound();
    }
}