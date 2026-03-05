package Item.Weapon;

import Interface.Cooldownable;
import Item.Bullet.Bullet;
import Item.Item;
import Player.Player;
import GameLogic.GameLogic;

public abstract class Gun extends Weapon implements Cooldownable {
    protected long lastFiredTime = 0; // เวลาที่ยิงนัดล่าสุด
    protected long fireRate;
    protected double targetX, targetY;// ระยะห่างระหว่างนัด (Cooldown) เช่น 100ms, 500ms
    private static final java.util.Random RNG = new java.util.Random();
    public Gun(String name, int amount, int damage, long fireRate) {
        super(name, amount, damage);
        this.fireRate = fireRate;
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

        double recoilSpread = player.getRecoil() / player.getMaxRecoil(); // 0.0–1.0
        double maxAngle = getRecoilAmount() * 0.08; // ยิ่ง recoilAmount มาก ยิ่งกระจาย
        double angleOffset = (RNG.nextDouble() * 2 - 1) * maxAngle * recoilSpread;

        // หมุน direction vector ตาม angleOffset (radians)
        double cos = Math.cos(angleOffset);
        double sin = Math.sin(angleOffset);
        double newDdx = ddx * cos - ddy * sin;
        double newDdy = ddx * sin + ddy * cos;
        ddx = newDdx;
        ddy = newDdy;

        // normalize
        double len = Math.sqrt(ddx * ddx + ddy * ddy);
        if (len == 0) return;
        ddx /= len;
        ddy /= len;

        GameLogic.addBullet(new Bullet((int) cx, (int) cy, ddx, ddy, 20, this.damage, this.name));
        Item.playGunSound();
    }
    protected double[] applyRecoil(double ddx, double ddy) {
        double maxRad = Math.toRadians(getRecoilAmount());
        double angle = (RNG.nextDouble() * 2.0 - 1.0) * maxRad;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new double[]{ ddx*cos - ddy*sin, ddx*sin + ddy*cos };
    }
    public void setMouseTarget(double mx, double my) {
        this.targetX = mx;
        this.targetY = my;
    }
}
