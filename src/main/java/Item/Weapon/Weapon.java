package Item.Weapon;

import Item.Item;

/** Base class for all weapons */
public abstract class Weapon extends Item {
    protected int damage;

    public Weapon(String name, int ammo, int damage) {
        super(name, ammo);
        this.damage = damage;
    }

    public abstract double getRecoilAmount();
}