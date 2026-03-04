package gamelogic;

import enemy.Enemy;

public class CombatManager {

    private StatusEffectManager effectManager;

    public CombatManager(StatusEffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void enemyAttack(Player player, Enemy enemy) {


        player.takeDamage(enemy.getDamage());


        effectManager.applyEffect(player, enemy.getSpecialEffect());
    }

    public boolean playerAttack(Enemy enemy, int damage) {

        enemy.takeDamage(damage);
        return enemy.isDead();
    }
}