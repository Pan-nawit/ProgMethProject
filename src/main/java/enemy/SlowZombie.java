package enemy;

import Player.Player;
import Status.Limping;

/** Applies Limping (slow) on hit — HP 1, Speed 0.5, Damage 1 */
public class SlowZombie extends BaseEnemy {
    public SlowZombie(float x, float y) {
        super(1, 1, 1, x, y); // speed stored as int; uses 0.5 via override
        this.attackCooldown = 1000;
    }

    @Override
    protected void moveTowards(Player p) {
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        if (dist > 1) {
            x += (diffX / dist) * 0.5f; // half speed
            y += (diffY / dist) * 0.5f;
        }
    }

    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {
            if (isCooldownReady()) {
                p.onAttacked(strength, new Limping());
                p.applyKnockback(15);
                resetCooldown();
            }
        }
    }

    @Override
    public javafx.scene.paint.Color getEnemyColor() {
        return javafx.scene.paint.Color.web("#16a085"); // teal
    }
}