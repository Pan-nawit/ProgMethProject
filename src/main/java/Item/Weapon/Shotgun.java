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
        int dx = 0, dy = 0;
        switch (player.getLastFacing()) {
            case 'w' -> dy = -1;
            case 's' -> dy =  1;
            case 'a' -> dx = -1;
            case 'd' -> dx =  1;
        }
        int spawnX = player.getX() + player.getWidth()  / 2;
        int spawnY = player.getY() + player.getHeight() / 2;
        int spreadX = (dx == 0) ? 1 : 0;
        int spreadY = (dy == 0) ? 1 : 0;

        Bullet b1 = new Bullet(spawnX, spawnY, dx,           dy,           15, damage, name);
        Bullet b2 = new Bullet(spawnX, spawnY, dx - spreadX, dy - spreadY, 15, damage, name);
        Bullet b3 = new Bullet(spawnX, spawnY, dx + spreadX, dy + spreadY, 15, damage, name);

        GameLogic.addBullet(b1);
        GameLogic.addBullet(b2);
        GameLogic.addBullet(b3);

        player.applyKnockback(5);
        player.addRecoil(getRecoilAmount());
        playGunSound();
    }

    @Override
    public void playGunSound() { playSound(); }
}