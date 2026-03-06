package Enemy;
import Player.Player;
public class zombie extends BaseEnemy {
    public zombie(float x, float y) { super(1,1,1,x,y); this.attackCooldown = 1000; }
    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {
            if (isCooldownReady()) {
                p.onAttacked(strength, null);
                p.applyKnockback(20);
                resetCooldown();
            }
        }
    }
}