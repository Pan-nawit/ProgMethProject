package Item.Weapon;
import Item.Item;
import Player.Player;

public abstract class Weapon extends Item {
    public int getDamage() {
        return damage;
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    protected int damage;
    public Weapon(String name,int amount,int damage,String imagePath, String soundPath){
        super(name,amount,imagePath,soundPath);
        setDamage(damage);
    }
    @Override
    public void use(Player player){
        if(!isEmpty()){
            shoot(player);
            applyWeaponRecoil(player);
            reduceAmount();
            System.out.println("Used: " + name +  " | Damage: " + damage);
        }
        else{
            System.out.println(name + " is empty!");
        }
    }
    public abstract void shoot(Player player);
    public abstract double getRecoilAmount();
    private void applyWeaponRecoil(Player player) {
        player.addRecoil(getRecoilAmount());
    }
}
