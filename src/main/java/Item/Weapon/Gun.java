package Item.Weapon;

import Interface.Cooldownable;
import Item.Bullet.Bullet;
import GameLogic.GameLogic;
import Player.Player;

public abstract class Gun extends Weapon implements Cooldownable {
    protected long lastFiredTime = 0;
    protected long fireRate;

    public Gun(String name, int amount, int damage, String imagePath, String soundPath, long fireRate) {
        super(name, amount, damage, imagePath, soundPath);
        this.fireRate = fireRate;
    }

    @Override public boolean isReady() {
        return System.currentTimeMillis() - lastFiredTime >= fireRate;
    }
    @Override public void startCooldown() { lastFiredTime = System.currentTimeMillis(); }
    @Override public long getCooldownMillis() { return fireRate; }

    @Override
    public void use(Player player) {
        if (!isEmpty() && isReady()) {
            shoot(player);
            startCooldown();
            reduceAmount();
        } else if (isEmpty()) {
            System.out.println(name + " out of ammo!");
        }
    }

    /** Default shoot — uses cursor direction if available, else lastFacing */
    public void shoot(Player player) {
        float cx = player.getX() + player.getWidth()  / 2f;
        float cy = player.getY() + player.getHeight() / 2f;

        float dx, dy;
        if (player.getMouseX() >= 0) {
            // Shoot toward cursor
            dx = player.getMouseX() - cx;
            dy = player.getMouseY() - cy;
        } else {
            // Fallback to WASD facing
            dx = 0; dy = 0;
            switch (player.getLastFacing()) {
                case 'w' -> dy = -1;
                case 's' -> dy =  1;
                case 'a' -> dx = -1;
                case 'd' -> dx =  1;
            }
        }

        Bullet bullet = new Bullet(cx, cy, dx, dy, 20, this.damage, this.name);
        GameLogic.addBullet(bullet);
        playGunSound();
    }

    public abstract void playGunSound();
}