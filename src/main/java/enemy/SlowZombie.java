package enemy;

import Player.Player;
import Status.Limping;

public class SlowZombie extends BaseEnemy {
    public SlowZombie(float x, float y) {
        super(1, 1, 1, x, y);
        this.attackCooldown = 1000;
    }

    @Override
    protected void moveTowards(Player p) {
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        if (dist > 1) {
            x += (diffX / dist) * 0.5f;
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