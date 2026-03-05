package enemy;

import Player.Player;

public class juggernaut extends BaseEnemy {
    public juggernaut(float x, float y) {
        super(5,1,1,x,y);
        setHeight(48);
        setWidth(48);
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
