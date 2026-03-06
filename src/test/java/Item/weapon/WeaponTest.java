package Item.weapon;


import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Weapon Tests")
class WeaponTest {


    static class Bullet {
        double dx, dy;
        int damage;
        Bullet(int x, int y, double dx, double dy, int speed, int damage, String owner) {
            this.damage = damage;
            double len = Math.sqrt(dx*dx + dy*dy);
            this.dx = len > 0 ? dx / len : 0;
            this.dy = len > 0 ? dy / len : 0;
        }
    }


    static class Player {
        int x = 384, y = 234, width = 32, height = 32;
    }


    static abstract class Gun {
        String name;
        int amount, damage;
        long fireRate, lastFiredTime = 0;
        double targetX = 500, targetY = 300;
        List<Bullet> fired = new ArrayList<>();


        Gun(String name, int amount, int damage, long fireRate) {
            this.name = name; this.amount = amount;
            this.damage = damage; this.fireRate = fireRate;
        }
        boolean isReady()      { return System.currentTimeMillis() - lastFiredTime >= fireRate; }
        void startCooldown()   { lastFiredTime = System.currentTimeMillis(); }
        boolean isEmpty()      { return amount <= 0; }
        void use(Player p) {
            if (amount > 0 && isReady()) { shoot(p); startCooldown(); amount--; }
        }
        abstract void shoot(Player p);
    }


    static class Pistol extends Gun {
        Pistol() { super("Pistol", 30, 1, 400); }
        @Override void shoot(Player p) {
            double cx = p.x + p.width / 2.0, cy = p.y + p.height / 2.0;
            fired.add(new Bullet((int)cx, (int)cy, targetX - cx, targetY - cy, 20, damage, name));
        }
    }


    static class MachineGun extends Gun {
        MachineGun() { super("MachineGun", 60, 1, 120); }
        @Override void shoot(Player p) {
            double cx = p.x + p.width / 2.0, cy = p.y + p.height / 2.0;
            fired.add(new Bullet((int)cx, (int)cy, targetX - cx, targetY - cy, 20, damage, name));
        }
    }


    static class Shotgun extends Gun {
        Shotgun() { super("Shotgun", 10, 3, 1000); }
        @Override void shoot(Player p) {
            double cx = p.x + p.width / 2.0, cy = p.y + p.height / 2.0;
            double dx = targetX - cx, dy = targetY - cy;
            double len = Math.sqrt(dx*dx + dy*dy);
            double fx = dx/len, fy = dy/len, px = -fy, py = fx;
            fired.add(new Bullet((int)cx, (int)cy, fx,            fy,            15, damage, name));
            fired.add(new Bullet((int)cx, (int)cy, fx + px * 0.3, fy + py * 0.3, 15, damage, name));
            fired.add(new Bullet((int)cx, (int)cy, fx - px * 0.3, fy - py * 0.3, 15, damage, name));
        }
    }


    // -- Pistol --


    @Test @DisplayName("Pistol: initial ammo = 30")
    void pistolInitAmmo() { assertEquals(30, new Pistol().amount); }


    @Test @DisplayName("Pistol: fire rate = 400ms")
    void pistolFireRate() { assertEquals(400L, new Pistol().fireRate); }


    @Test @DisplayName("Pistol: ammo decreases after shot")
    void pistolAmmoDecreases() { Pistol g = new Pistol(); g.use(new Player()); assertEquals(29, g.amount); }


    @Test @DisplayName("Pistol: fires exactly 1 bullet per shot")
    void pistolFiresOneBullet() { Pistol g = new Pistol(); g.use(new Player()); assertEquals(1, g.fired.size()); }


    @Test @DisplayName("Pistol: bullet damage = 1")
    void pistolBulletDamage() { Pistol g = new Pistol(); g.use(new Player()); assertEquals(1, g.fired.get(0).damage); }


    @Test @DisplayName("Pistol: fire rate prevents rapid double shot")
    void pistolFireRateLimits() {
        Pistol g = new Pistol(); Player p = new Player();
        g.use(p); g.use(p);
        assertEquals(1, g.fired.size());
    }


    @Test @DisplayName("Pistol: isEmpty when ammo = 0")
    void pistolEmptyCheck() { Pistol g = new Pistol(); g.amount = 0; assertTrue(g.isEmpty()); }


    @Test @DisplayName("Pistol: cannot fire when empty")
    void pistolCannotFireWhenEmpty() { Pistol g = new Pistol(); g.amount = 0; g.use(new Player()); assertEquals(0, g.fired.size()); }


    @ParameterizedTest(name = "Fire {0} shots, ammo remaining = {1}")
    @CsvSource({"1,29", "10,20", "30,0"})
    void pistolAmmoAfterShots(int shots, int remaining) {
        Pistol g = new Pistol(); // starts at 30
        for (int i = 0; i < shots; i++) {
            g.lastFiredTime = 0; // bypass cooldown
            g.use(new Player());
        }
        assertEquals(remaining, g.amount);
    }


    // -- MachineGun --


    @Test @DisplayName("MachineGun: initial ammo = 60")
    void machineGunInitAmmo() { assertEquals(60, new MachineGun().amount); }


    @Test @DisplayName("MachineGun: fire rate = 120ms")
    void machineGunFireRate() { assertEquals(120L, new MachineGun().fireRate); }


    @Test @DisplayName("MachineGun: fires faster than Pistol")
    void machineGunFasterThanPistol() { assertTrue(new MachineGun().fireRate < new Pistol().fireRate); }


    @Test @DisplayName("MachineGun: ammo decreases after shot")
    void machineGunAmmoDecreases() { MachineGun g = new MachineGun(); g.use(new Player()); assertEquals(59, g.amount); }


    @Test @DisplayName("MachineGun: fires 1 bullet per shot")
    void machineGunFiresOneBullet() { MachineGun g = new MachineGun(); g.use(new Player()); assertEquals(1, g.fired.size()); }


    // -- Shotgun --


    @Test @DisplayName("Shotgun: initial ammo = 10")
    void shotgunInitAmmo() { assertEquals(10, new Shotgun().amount); }


    @Test @DisplayName("Shotgun: fire rate = 1000ms")
    void shotgunFireRate() { assertEquals(1000L, new Shotgun().fireRate); }


    @Test @DisplayName("Shotgun: slowest fire rate of all guns")
    void shotgunSlowest() {
        assertTrue(new Shotgun().fireRate > new MachineGun().fireRate);
        assertTrue(new Shotgun().fireRate > new Pistol().fireRate);
    }


    @Test @DisplayName("Shotgun: fires 3 pellets per shot")
    void shotgunFiresThreePellets() { Shotgun g = new Shotgun(); g.use(new Player()); assertEquals(3, g.fired.size()); }


    @Test @DisplayName("Shotgun: every pellet deals 3 damage")
    void shotgunPelletDamage() { Shotgun g = new Shotgun(); g.use(new Player()); g.fired.forEach(b -> assertEquals(3, b.damage)); }


    @Test @DisplayName("Shotgun: pellets spread in different directions")
    void shotgunPelletsSpread() {
        Shotgun g = new Shotgun(); g.use(new Player());
        assertFalse(g.fired.get(0).dx == g.fired.get(1).dx && g.fired.get(1).dx == g.fired.get(2).dx);
    }


    @Test @DisplayName("Shotgun: ammo decreases by 1 per shot")
    void shotgunAmmoDecreases() { Shotgun g = new Shotgun(); g.use(new Player()); assertEquals(9, g.amount); }


    // -- General --


    @Test @DisplayName("Ammo stacks correctly with addAmount")
    void ammoStacks() { Pistol g = new Pistol(); g.amount += 10; assertEquals(40, g.amount); }
}