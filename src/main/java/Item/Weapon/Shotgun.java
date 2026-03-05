package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

public class Shotgun extends Gun {
    public Shotgun() { super("Shotgun", 2, 3, "", "", 3000); }

    @Override public double getRecoilAmount() { return 5.0; }

    @Override
    public void shoot(Player player) {
        float cx = player.getX() + player.getWidth()  / 2f;
        float cy = player.getY() + player.getHeight() / 2f;
        float dx = player.getMouseX() - cx;
        float dy = player.getMouseY() - cy;
        // 3 pellets with spread perpendicular to aim direction
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float nx = len > 0 ? dx / len : 1; // normalized
        float ny = len > 0 ? dy / len : 0;
        float px = -ny; // perpendicular axis
        float py =  nx;
        float sp = 0.3f; // spread amount
        GameLogic.addBullet(new Bullet(cx, cy, nx,        ny,        15, damage, name));
        GameLogic.addBullet(new Bullet(cx, cy, nx - px*sp, ny - py*sp, 15, damage, name));
        GameLogic.addBullet(new Bullet(cx, cy, nx + px*sp, ny + py*sp, 15, damage, name));
        player.applyKnockback(5);
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override public void playGunSound() { playSound(); }
}