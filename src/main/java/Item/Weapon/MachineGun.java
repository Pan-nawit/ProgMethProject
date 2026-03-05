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
    public double getRecoilAmount() { return 0.5; }

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
        GameLogic.addBullet(new Bullet(spawnX, spawnY, dx, dy, 20, damage, name));
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override
    public void playGunSound() { playSound(); }
}