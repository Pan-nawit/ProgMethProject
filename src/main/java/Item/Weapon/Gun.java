package Item.Weapon;

import Interface.Cooldownable;
import Item.Bullet.Bullet;
import Player.Player;
import GameLogic.GameLogic;

public abstract class Gun extends Weapon implements Cooldownable {
    protected long lastFiredTime = 0; // เวลาที่ยิงนัดล่าสุด
    protected long fireRate;
    protected double targetX, targetY;// ระยะห่างระหว่างนัด (Cooldown) เช่น 100ms, 500ms
    public Gun(String name,int amount,int damage,String imagePath, String soundPath,long fireRate){
        super(name, amount, damage,imagePath,soundPath);
        this.fireRate=fireRate;
    }
    @Override
    public boolean isReady() {
        // เช็คว่าเวลาปัจจุบัน ห่างจากนัดล่าสุดเกินค่า fireRate หรือยัง
        return System.currentTimeMillis() - lastFiredTime >= fireRate;
    }
    @Override
    public void startCooldown() {
        lastFiredTime = System.currentTimeMillis();
    }
    @Override
    public long getCooldownMillis() {
        return fireRate;
    }
    @Override
    public void use(Player player) {
        // 1. เช็คว่ามีกระสุนไหม และ 2. เช็คว่าคูลดาวน์เสร็จหรือยัง (Fire Rate)
        if (!isEmpty() && isReady()) {
            shoot(player);      // ยิงออกไป
            startCooldown();    // เริ่มนับเวลาถอยหลังสำหรับนัดถัดไป
            reduceAmount();     // ลดจำนวนกระสุน
        } else if (isEmpty()) {
            System.out.println(name + " out of ammo!");
        }
    }
    public void shoot(Player player) {
        // จุดกึ่งกลาง player
        double cx = player.getX() + player.getWidth()  / 2.0;
        double cy = player.getY() + player.getHeight() / 2.0;

        // vector จาก player ไป mouse
        double ddx = targetX - cx;
        double ddy = targetY - cy;

        // normalize
        double len = Math.sqrt(ddx * ddx + ddy * ddy);
        if (len == 0) return;
        ddx /= len;
        ddy /= len;

        Bullet bullet = new Bullet(
                (int) cx, (int) cy,
                ddx, ddy,
                20, this.damage, this.name
        );
        GameLogic.addBullet(bullet);
        playGunSound();
    }
    public abstract void playGunSound();
    public void setMouseTarget(double mx, double my) {
        this.targetX = mx;
        this.targetY = my;
    }
}
