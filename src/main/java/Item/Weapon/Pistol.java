package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

/** Pistol — 30 shots, fires 1 bullet per click */
public class Pistol extends Gun {
    public Pistol() {
        super("Pistol", 30, 1, "", "", 400);
    }

    @Override
    public double getRecoilAmount() { return 1.0; }

    @Override
    public void shoot(Player player) {
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;
        double ddx = targetX - cx, ddy = targetY - cy;
        double len = Math.sqrt(ddx*ddx + ddy*ddy);
        if (len == 0) return;
        ddx /= len; ddy /= len;

        GameLogic.addBullet(new Bullet((int)cx, (int)cy, ddx, ddy, 20, damage, name));
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override
    public void playGunSound() { playSound(); }
}