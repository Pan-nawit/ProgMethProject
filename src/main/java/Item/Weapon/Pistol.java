package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

public class Pistol extends Gun {
    public Pistol() { super("Pistol", 30, 1, "", "", 400); }

    @Override public double getRecoilAmount() { return 1.0; }

    @Override
    public void shoot(Player player) {
        float cx = player.getX() + player.getWidth()  / 2f;
        float cy = player.getY() + player.getHeight() / 2f;
        float dx = player.getMouseX() - cx;
        float dy = player.getMouseY() - cy;
        GameLogic.addBullet(new Bullet(cx, cy, dx, dy, 20, damage, name));
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override public void playGunSound() { playSound(); }
}