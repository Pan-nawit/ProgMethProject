package Item;

import Player.Player;

public class Bandage extends HealingItems{
    public Bandage(int healing){
        super("Bandage",1);
    }
    @Override
    public void Healing(Player player){
        int healingAmount = 2;
        player.setHp(player.getHp() + healingAmount);
    }
}
