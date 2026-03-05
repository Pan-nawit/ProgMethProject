package enemy;

import Player.Player;

public class Runners extends BaseEnemy {
    public Runners(float x, float y) {
        super(1, 2, 1, x,y);
    }

    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {

            if (isCooldownReady()) { // ใช้ Helper จากตัวแม่เช็คเวลา
                p.onAttacked(strength, null);
                p.applyKnockback(20);

                //System.out.println("Zombie Bit You!");
                resetCooldown(); // รีเซ็ตเวลา
            }
        }
    }
}
