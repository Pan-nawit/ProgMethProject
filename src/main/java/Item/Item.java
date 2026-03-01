package Item;

import Player.Player;

public abstract class Item {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    protected String name;
    protected int amount;
    public Item(String name,int amount){
        setName(name);
        setAmount(amount);
    }
    public void addAmount(int amount) {
        setAmount(getAmount()+amount);
    }
    protected void reduceAmount(){
        if(amount>0) setAmount(getAmount()-1);
    }
    public boolean isEmpty() {
        return amount <= 0;
    }
    public abstract void use(Player player);
}
