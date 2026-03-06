package Enemy;
import Player.Player;
import Status.Bleeding;
public class AnimalZombies extends BaseEnemy {
    public AnimalZombies(float x, float y) { super(1,1,1,x,y); setHeight(16); }
    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {
            if (isCooldownReady()) {
                p.onAttacked(strength, new Bleeding());
                p.applyKnockback(20);
                resetCooldown();
            }
        }
    }
    @Override public javafx.scene.paint.Color getEnemyColor() { return javafx.scene.paint.Color.web("#e67e22"); }
}