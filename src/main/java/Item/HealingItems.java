package Item;

import Player.Player;

public abstract class HealingItems extends Item{
    public HealingItems(String name,int amount,String imagePath, String soundPath){
        super(name, amount, imagePath, soundPath);
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
