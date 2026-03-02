package Item.Weapon;

import Interface.Cooldownable;
import Item.Bullet.Bullet;
import Player.Player;

public abstract class Gun extends Weapon implements Cooldownable {
    protected long lastFiredTime = 0; // เวลาที่ยิงนัดล่าสุด
    protected long fireRate;         // ระยะห่างระหว่างนัด (Cooldown) เช่น 100ms, 500ms
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
        int dirX = 0;
        int dirY = 0;
        switch (player.getLastFacing()) {
            case 'w' -> dirY = -1; // ขึ้น
            case 's' -> dirY = 1;  // ลง
            case 'a' -> dirX = -1; // ซ้าย
            case 'd' -> dirX = 1;  // ขวา
        }
        Bullet bullet = new Bullet(
                player.getX() + (player.getWidth()/2), // ออกจากกลางตัว
                player.getY() + (player.getHeight()/2),
                dirX, dirY,
                20, this.damage, this.name
        );
        // 3. (สำคัญ) ส่งกระสุนเข้าสู่ GameWorld หรือ List ของกระสุน
        // ตัวอย่าง: GamePanel.addBullet(bullet);
        // 4. เล่นเสียงปืน
        playGunSound();
    }
    public abstract void playGunSound();
}
