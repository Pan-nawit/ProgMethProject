package Item.Weapon;

import GameLogic.GameLogic;
import Item.Bullet.Bullet;
import Player.Player;

public class MachineGun extends Gun {
    public MachineGun() { super("MachineGun", 60, 1, "", "", 120); }

    @Override public double getRecoilAmount() { return 0.5; }

    @Override
    public void shoot(Player player) {
        float cx = player.getX() + player.getWidth()  / 2f;
        float cy = player.getY() + player.getHeight() / 2f;
        float dx = player.getMouseX() - cx;
        float dy = player.getMouseY() - cy;
        // slight random spread for machine gun feel
        float spread = (float)(Math.random() * 10 - 5);
        GameLogic.addBullet(new Bullet(cx, cy, dx + spread, dy + spread, 20, damage, name));
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override public void playGunSound() { playSound(); }
}