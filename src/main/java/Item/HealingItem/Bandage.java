package Item.HealingItem;

import Player.Player;

/** Restores 1 HP and clears Bleeding + Limping */
public class Bandage extends HealingItems {
    public Bandage() {
        super("Bandage", 1, null, null);
    }

    @Override
    public void Healing(Player player) {
        player.setHp(player.getHp() + 1);
        player.removeStatus("Bleeding");
        player.removeStatus("Limping");
        System.out.println("[Bandage] HP +1, status cleared.");
    }
}