package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

public class Shotgun extends Gun {
    public Shotgun() {
        super("Shotgun", 2, 3, "", "", 3000);
    }

    @Override
    public double getRecoilAmount() { return 5.0; }

    @Override
    public void shoot(Player player) {
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;
        double ddx = targetX - cx, ddy = targetY - cy;
        double len = Math.sqrt(ddx*ddx + ddy*ddy);
        if (len == 0) return;
        ddx /= len; ddy /= len;

        // perpendicular vector สำหรับ spread
        double perpX = -ddy;
        double perpY =  ddx;

        GameLogic.addBullet(new Bullet((int)cx, (int)cy, ddx,               ddy,               15, damage, name));
        GameLogic.addBullet(new Bullet((int)cx, (int)cy, ddx + perpX * 0.3, ddy + perpY * 0.3, 15, damage, name));
        GameLogic.addBullet(new Bullet((int)cx, (int)cy, ddx - perpX * 0.3, ddy - perpY * 0.3, 15, damage, name));

        player.applyKnockback(5);
        player.addRecoil(getRecoilAmount());
        playGunSound();;
    }

    @Override
    public void playGunSound() { playSound(); }
}