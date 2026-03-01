package Item;

import Player.Player;

public abstract class HealingItems extends Item{
    public HealingItems(String name,int amount){
        super(name,amount);
    }
    @Override
    public void use(Player player) {
        if (!isEmpty()) {
            Healing(player);
            reduceAmount();
            System.out.println("Used: " + name);
        } else {
            System.out.println(name + " is empty!");
        }
    }
    public abstract void Healing(Player player);
}
