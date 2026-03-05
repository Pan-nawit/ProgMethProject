package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

/** MachineGun — 60 shots, rapid fire */
public class MachineGun extends Gun {
    public MachineGun() {
        super("MachineGun", 60, 1, "", "", 120);
    }

    @Override
    public double getRecoilAmount() { return 10.0; }

    @Override
    public void shoot(Player player) {
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;
        double ddx = targetX - cx, ddy = targetY - cy;
        double len = Math.sqrt(ddx*ddx + ddy*ddy);
        if (len == 0) return;
        ddx /= len; ddy /= len;
        double[] dir = applyRecoil(ddx, ddy); // ← เพิ่มบรรทัดนี้
        ddx = dir[0]; ddy = dir[1];                   // ← และนี้
        GameLogic.addBullet(new Bullet((int)cx, (int)cy, ddx, ddy, 20, damage, name));
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override
    public void playGunSound() { playSound(); }
}