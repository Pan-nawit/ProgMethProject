package Item;


public abstract class Gun extends Weapon{
    public Gun(String name,int amount,int damage){
        super(name, amount, damage);
    }
    public void shoot(){
        System.out.println(name+" fire!");
        playGunSound();
    }
    public abstract void playGunSound();
}
