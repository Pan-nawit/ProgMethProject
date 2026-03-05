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
        int dx = 0, dy = 0;
        switch (player.getLastFacing()) {
            case 'w' -> dy = -1;
            case 's' -> dy =  1;
            case 'a' -> dx = -1;
            case 'd' -> dx =  1;
        }
        int spawnX = player.getX() + player.getWidth()  / 2;
        int spawnY = player.getY() + player.getHeight() / 2;
        Bullet b = new Bullet(spawnX, spawnY, dx, dy, 20, damage, name);
        GameLogic.addBullet(b);
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override
    public void playGunSound() { playSound(); }
}