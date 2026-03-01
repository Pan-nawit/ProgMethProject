package Item;
import Player.Player;

public abstract class Weapon extends Item{
    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    protected int damage;
    public Weapon(String name,int amount,int damage){
        super(name,amount);
        setDamage(damage);
    }
    @Override
    public void use(){
        if(!isEmpty()){
            shoot();
            reduceAmount();
            System.out.println("Used: " + name);
        }
        else{
            System.out.println(name + " is empty!");
        }
    }
    public abstract void shoot();
    public abstract double getRecoilAmount();
    private void applyWeaponRecoil() {
        Player.setRecoil(getRecoilAmount());
    }
}
