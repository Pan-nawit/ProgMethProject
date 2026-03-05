package enemy;
import Player.Player;
public class Screamers extends BaseEnemy {
    public Screamers(float x, float y) { super(1,1,1,x,y); }
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
    @Override public javafx.scene.paint.Color getEnemyColor() { return javafx.scene.paint.Color.web("#e91e8c"); }
}