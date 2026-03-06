package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Item.Item;
import Player.Player;

/** MachineGun — 60 shots, rapid fire */
public class MachineGun extends Gun {
    public MachineGun() {
        super("MachineGun", 60, 1, 120);
    }

    @Override
    public double getRecoilAmount() { return 10.0; }

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