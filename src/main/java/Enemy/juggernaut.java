package Enemy;
import Player.Player;
public class juggernaut extends BaseEnemy {
    public juggernaut(float x, float y) { super(5,1,1,x,y); setHeight(48); setWidth(48); }
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
    @Override public javafx.scene.paint.Color getEnemyColor() { return javafx.scene.paint.Color.web("#922b21"); }
}