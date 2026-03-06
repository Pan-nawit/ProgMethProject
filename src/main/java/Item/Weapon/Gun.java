package Item.Weapon;

import Interface.Cooldownable;
import Player.Player;

public abstract class Gun extends Weapon implements Cooldownable {
    protected long lastFiredTime = 0;
    protected long fireRate;
    protected double targetX, targetY;
    private static final java.util.Random RNG = new java.util.Random();

    public Gun(String name, int amount, int damage, long fireRate) {
        super(name, amount, damage);
        this.fireRate = fireRate;
    }
    @Override
    public boolean isReady() {
        return System.currentTimeMillis() - lastFiredTime >= fireRate;
    }
    @Override
    public void startCooldown() {
        lastFiredTime = System.currentTimeMillis();
    }
    @Override
    public void use(Player player) {
        if (!isEmpty() && isReady()) {
            shoot(player);
            startCooldown();
            reduceAmount();
        } else if (isEmpty()) {
            System.out.println(name + " out of ammo!");
        }
    }
    public abstract void shoot(Player player);

    protected double[] applyRecoil(double ddx, double ddy) {
        double maxRad = Math.toRadians(getRecoilAmount());
        double angle = (RNG.nextDouble() * 2.0 - 1.0) * maxRad;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new double[]{ ddx*cos - ddy*sin, ddx*sin + ddy*cos };
    }
    public void setMouseTarget(double mx, double my) {
        this.targetX = mx;
        this.targetY = my;
    }
}
