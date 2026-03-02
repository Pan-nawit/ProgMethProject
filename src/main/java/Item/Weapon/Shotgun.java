package Item.Weapon;

import Item.Bullet.Bullet;
import Player.Player;

public class Shotgun extends Gun {
    public Shotgun(){
        super("Shotgun",2,3,"","",3000);
    }
    @Override
    public double getRecoilAmount() {
        // ทุกครั้งที่ยิง ค่า Recoil ใน Player จะเพิ่มขึ้น 1.5
        return 5.0;
    }
    @Override
    public void shoot(Player player) {
        int dx = 0, dy = 0;
        // 1. หาหน้าทิศทางหลักที่ผู้เล่นหันไปก่อน (สั้นๆ คลีนๆ)
        switch (player.getLastFacing()) {
            case 'w' -> dy = -1;
            case 's' -> dy = 1;
            case 'a' -> dx = -1;
            case 'd' -> dx = 1;
        }
        int spawnX = player.getX() + (player.getWidth() / 2);
        int spawnY = player.getY() + (player.getHeight() / 2);
        // 2. พระเอกของงานนี้: คำนวณแกนที่ต้องกระจาย (Spread Axis)
        // ถ้า dx เป็น 0 (ยิงขึ้น/ลง) ให้กระจายแกน X เป็น 1
        // ถ้า dy เป็น 0 (ยิงซ้าย/ขวา) ให้กระจายแกน Y เป็น 1
        int spreadX = (dx == 0) ? 1 : 0;
        int spreadY = (dy == 0) ? 1 : 0;
        // 3. สร้างกระสุน 3 นัดด้วยคณิตศาสตร์ล้วนๆ ไม่ต้องพึ่ง switch-case ยาวๆ
        Bullet b1 = new Bullet(spawnX, spawnY, dx, dy, 15, damage, name);                       // นัดหลัก (ตรงกลาง)
        Bullet b2 = new Bullet(spawnX, spawnY, dx - spreadX, dy - spreadY, 15, damage, name);   // นัดเฉียงฝั่งลบ
        Bullet b3 = new Bullet(spawnX, spawnY, dx + spreadX, dy + spreadY, 15, damage, name);   // นัดเฉียงฝั่งบวก
        // (อย่าลืมนำ b1, b2, b3 ไป Add ใส่ List ใน GameLoop)
        player.applyKnockback(5);
        playGunSound();
    }
    @Override
    public void playGunSound() {
        playSound();
    }
}
