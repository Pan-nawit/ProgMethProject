package Item.HealingItem;

import Player.Player;

/** Restores 3 HP */
public class Medkit extends HealingItems {
    public Medkit() {
        super("Medkit", 1);
    }

    @Override
    public void Healing(Player player) {
        player.setHp(player.getHp() + 3);
        System.out.println("[Medkit] HP +3. Current HP: " + player.getHp());
    }
}