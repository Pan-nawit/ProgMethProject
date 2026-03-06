package Enemy;
import Player.Player;
public class Runners extends BaseEnemy {
    public Runners(float x, float y) { super(1, 2, 1, x, y); }
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
    @Override public javafx.scene.paint.Color getEnemyColor() { return javafx.scene.paint.Color.web("#f39c12"); }
}