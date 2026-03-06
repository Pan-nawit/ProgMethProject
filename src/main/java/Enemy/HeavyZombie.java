package Enemy;

import Player.Player;

public class HeavyZombie extends BaseEnemy {
    public HeavyZombie(float x, float y) {
        super(1, 1, 2, x, y);
        this.attackCooldown = 1500;
    }

    @Override
    public void Attackplayer(Player p) {
        if (this.getBounds().intersects(p.getBounds())) {
            if (isCooldownReady()) {
                p.onAttacked(strength, null);
                p.applyKnockback(30);
                resetCooldown();
            }
        }
    }

    @Override
    public javafx.scene.paint.Color getEnemyColor() {
        return javafx.scene.paint.Color.web("#8e44ad"); // purple
    }
}