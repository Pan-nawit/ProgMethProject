package enemy;

import Player.Player;

public class Shooter extends BaseEnemy {

    private int range = 32; // ระยะยิง 32 pixel

    public Shooter(float x, float y) {
        super(1, 1, 1, x, y);
        this.attackCooldown = 5000; // ยิงทุก 5 วิ
    }

    @Override
    public void Attackplayer(Player p) {
        // คำนวณระยะห่าง
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        double distance = Math.sqrt(diffX*diffX + diffY*diffY);

        // Logic: ถ้าระยะห่าง น้อยกว่าระยะยิง ให้ทำการยิง
        if (distance < range) {

            if (isCooldownReady()) {
                p.onAttacked(strength, null);
                p.applyKnockback(20);

                //System.out.println("Zombie Bit You!");
                resetCooldown(); // รีเซ็ตเวลา
            }
        }
    }

    // Override การเดิน: ถ้าเข้าใกล้ระยะยิงแล้ว ให้หยุดเดิน (ไม่ต้องเดินไปชน)
    @Override
    protected void moveTowards(Player p) {
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        double distance = Math.sqrt(diffX*diffX + diffY*diffY);

        if (distance > range - 50) { // เดินเข้าหาจนกว่าจะถึงระยะยิง แล้วหยุด
            super.moveTowards(p);
        }
    }
}