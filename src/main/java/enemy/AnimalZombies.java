package enemy;

import Player.Player;
import Status.Bleeding;

public class AnimalZombies extends BaseEnemy {
    public AnimalZombies(float x, float y) {
        super(1,1,1,x,y);
        setHeight(16);
    }

    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {

            if (isCooldownReady()) { // ใช้ Helper จากตัวแม่เช็คเวลา
                p.onAttacked(strength, new Bleeding());
                p.applyKnockback(20);

                //System.out.println("Zombie Bit You!");
                resetCooldown(); // รีเซ็ตเวลา
            }
        }
    }
}
