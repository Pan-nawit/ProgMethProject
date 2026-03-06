package item.bullet;


import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import java.awt.Rectangle;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Bullet Tests")
class BulletTest {


    static class Bullet {
        float x, y;
        double dx, dy;
        int damage;
        static final int SPEED = 12;


        Bullet(int x, int y, double dx, double dy, int speed, int damage, String owner) {
            this.x = x; this.y = y; this.damage = damage;
            double len = Math.sqrt(dx*dx + dy*dy);
            this.dx = len > 0 ? dx / len : 0;
            this.dy = len > 0 ? dy / len : 0;
        }


        void update() { x += dx * SPEED; y += dy * SPEED; }


        Rectangle getBounds() { return new Rectangle((int)x - 3, (int)y - 3, 6, 6); }
    }


    // -- Movement --


    @Test @DisplayName("update moves bullet forward")
    void bulletMoves() {
        Bullet b = new Bullet(100, 100, 1, 0, 12, 1, "test");
        float before = b.x; b.update();
        assertTrue(b.x > before);
    }


    @Test @DisplayName("Bullet moves left with negative dx")
    void bulletMovesLeft() {
        Bullet b = new Bullet(100, 100, -1, 0, 12, 1, "test");
        b.update(); assertTrue(b.x < 100);
    }


    @Test @DisplayName("Bullet moves down with positive dy")
    void bulletMovesDown() {
        Bullet b = new Bullet(100, 100, 0, 1, 12, 1, "test");
        b.update(); assertTrue(b.y > 100);
    }


    @Test @DisplayName("Bullet moves diagonally: both X and Y change")
    void bulletMovesDiagonal() {
        Bullet b = new Bullet(100, 100, 1, 1, 12, 1, "test");
        b.update(); assertTrue(b.x > 100); assertTrue(b.y > 100);
    }


    // -- Direction Normalization --


    @Test @DisplayName("Direction vector is normalized to length 1")
    void directionNormalized() {
        Bullet b = new Bullet(0, 0, 3, 4, 12, 1, "test");
        double len = Math.sqrt(b.dx*b.dx + b.dy*b.dy);
        assertEquals(1.0, len, 0.001);
    }


    @ParameterizedTest(name = "dx={0}, dy={1} normalized to length 1")
    @CsvSource({"1,0", "0,1", "1,1", "3,4", "5,12"})
    void variousDirectionsNormalized(double dx, double dy) {
        Bullet b = new Bullet(0, 0, dx, dy, 12, 1, "test");
        double len = Math.sqrt(b.dx*b.dx + b.dy*b.dy);
        assertEquals(1.0, len, 0.001);
    }


    // -- Damage --


    @ParameterizedTest(name = "Bullet damage = {0}")
    @CsvSource({"1", "2", "3"})
    void bulletDamage(int dmg) {
        assertEquals(dmg, new Bullet(0, 0, 1, 0, 12, dmg, "test").damage);
    }


    // -- Bounds --


    @Test @DisplayName("getBounds returns 6x6 rectangle")
    void boundsSize() {
        Rectangle r = new Bullet(50, 50, 1, 0, 12, 1, "test").getBounds();
        assertEquals(6, r.width); assertEquals(6, r.height);
    }


    @Test @DisplayName("getBounds is centered at bullet position")
    void boundsCenter() {
        Rectangle r = new Bullet(50, 60, 1, 0, 12, 1, "test").getBounds();
        assertEquals(50 - 3, r.x); assertEquals(60 - 3, r.y);
    }


    @Test @DisplayName("Two close bullets intersect")
    void bulletsIntersect() {
        Bullet b1 = new Bullet(100, 100, 1, 0, 12, 1, "a");
        Bullet b2 = new Bullet(101, 100, 1, 0, 12, 1, "b");
        assertTrue(b1.getBounds().intersects(b2.getBounds()));
    }


    @Test @DisplayName("Two far bullets do not intersect")
    void bulletsFar() {
        Bullet b1 = new Bullet(0,   0, 1, 0, 12, 1, "a");
        Bullet b2 = new Bullet(500, 0, 1, 0, 12, 1, "b");
        assertFalse(b1.getBounds().intersects(b2.getBounds()));
    }
}

package status;


import org.junit.jupiter.api.*;
        import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Status Effect Tests")
class StatusTest {


    static class Player {
        int hp = 5, maxHp = 5, speed = 3, defaultSpeed = 3;
        void setHp(int v)    { hp    = Math.max(0, Math.min(v, maxHp)); }
        void setSpeed(int v) { speed = v; }
    }


    static class Bleeding {
        boolean timerStarted = false, timerCancelled = false;
        void apply(Player p) { timerStarted   = true; }
        void undo(Player p)  { timerCancelled = true; }
        void tick(Player p)  { if (p.hp > 0) p.setHp(p.hp - 1); }
        String getName()     { return "Bleeding"; }
    }


    static class Limping {
        void apply(Player p) { p.setSpeed(p.speed / 2); }
        void undo(Player p)  { p.setSpeed(p.defaultSpeed); }
        String getName()     { return "Limping"; }
    }


    static class PlayerWithStatus {
        int hp = 5, maxHp = 5, speed = 3, defaultSpeed = 3;
        java.util.List<Object> statusList = new java.util.ArrayList<>();


        private Player asPlayer() {
            PlayerWithStatus outer = this;
            Player p = new Player() {
                @Override public void setHp(int v)    { outer.hp    = Math.max(0, Math.min(v, outer.maxHp)); }
                @Override public void setSpeed(int v) { outer.speed = v; }
            };
            p.hp = hp; p.speed = speed; p.maxHp = maxHp; p.defaultSpeed = defaultSpeed;
            return p;
        }


        void addBleeding() {
            if (!hasBleeding()) { Bleeding b = new Bleeding(); b.apply(asPlayer()); statusList.add(b); }
        }
        void addLimping() {
            if (!hasLimping()) { Limping l = new Limping(); l.apply(asPlayer()); statusList.add(l); }
        }
        void removeBleeding() {
            statusList.removeIf(s -> { if (s instanceof Bleeding b) { b.undo(asPlayer()); return true; } return false; });
        }
        void removeLimping() {
            statusList.removeIf(s -> { if (s instanceof Limping l) { l.undo(asPlayer()); return true; } return false; });
        }
        boolean hasBleeding() { return statusList.stream().anyMatch(s -> s instanceof Bleeding); }
        boolean hasLimping()  { return statusList.stream().anyMatch(s -> s instanceof Limping);  }
    }


    // -- Limping --


    @Test @DisplayName("Limping: name is 'Limping'")
    void limpingName() { assertEquals("Limping", new Limping().getName()); }


    @Test @DisplayName("Limping apply: speed halved (3 -> 1)")
    void limpingHalvesSpeed() { Player p = new Player(); new Limping().apply(p); assertEquals(1, p.speed); }


    @Test @DisplayName("Limping undo: speed restored to defaultSpeed")
    void limpingUndoRestores() {
        Player p = new Player(); Limping l = new Limping();
        l.apply(p); l.undo(p);
        assertEquals(p.defaultSpeed, p.speed);
    }


    @Test @DisplayName("Limping apply: speed remains non-negative")
    void limpingSpeedNonNegative() { Player p = new Player(); p.speed = 1; new Limping().apply(p); assertTrue(p.speed >= 0); }


    @Test @DisplayName("Limping applied twice stacks the reduction")
    void limpingStackDoublesReduction() {
        Player p = new Player();
        new Limping().apply(p); // 3 -> 1
        new Limping().apply(p); // 1 -> 0
        assertEquals(0, p.speed);
    }


    // -- Bleeding --


    @Test @DisplayName("Bleeding: name is 'Bleeding'")
    void bleedingName() { assertEquals("Bleeding", new Bleeding().getName()); }


    @Test @DisplayName("Bleeding apply: timer is started")
    void bleedingApplyStartsTimer() { Player p = new Player(); Bleeding b = new Bleeding(); b.apply(p); assertTrue(b.timerStarted); }


    @Test @DisplayName("Bleeding undo: timer is cancelled")
    void bleedingUndoCancelsTimer() {
        Player p = new Player(); Bleeding b = new Bleeding();
        b.apply(p); b.undo(p); assertTrue(b.timerCancelled);
    }


    @Test @DisplayName("Bleeding tick: reduces HP by 1")
    void bleedingTickReducesHp() { Player p = new Player(); new Bleeding().tick(p); assertEquals(4, p.hp); }


    @Test @DisplayName("Bleeding tick x3: reduces HP by 3")
    void bleedingThreeTicks() {
        Player p = new Player(); Bleeding b = new Bleeding();
        b.tick(p); b.tick(p); b.tick(p); assertEquals(2, p.hp);
    }


    @Test @DisplayName("Bleeding tick: HP cannot go below 0")
    void bleedingStopsAtZeroHp() {
        Player p = new Player(); p.setHp(1); Bleeding b = new Bleeding();
        b.tick(p); b.tick(p); assertEquals(0, p.hp);
    }


    // -- Status stacking --


    @Test @DisplayName("addBleeding: player has Bleeding status")
    void canAddBleeding() { PlayerWithStatus p = new PlayerWithStatus(); p.addBleeding(); assertTrue(p.hasBleeding()); }


    @Test @DisplayName("addLimping: player has Limping and speed is reduced")
    void canAddLimping() { PlayerWithStatus p = new PlayerWithStatus(); p.addLimping(); assertTrue(p.hasLimping()); assertEquals(1, p.speed); }


    @Test @DisplayName("addBleeding twice: no duplicate")
    void bleedingNoDuplicate() { PlayerWithStatus p = new PlayerWithStatus(); p.addBleeding(); p.addBleeding(); assertEquals(1, p.statusList.size()); }


    @Test @DisplayName("addLimping twice: no duplicate")
    void limpingNoDuplicate() { PlayerWithStatus p = new PlayerWithStatus(); p.addLimping(); p.addLimping(); assertEquals(1, p.statusList.size()); }


    @Test @DisplayName("Both Bleeding and Limping can be active at the same time")
    void bothStatusesActive() {
        PlayerWithStatus p = new PlayerWithStatus(); p.addBleeding(); p.addLimping();
        assertTrue(p.hasBleeding() && p.hasLimping()); assertEquals(2, p.statusList.size());
    }


    @Test @DisplayName("removeBleeding keeps Limping active")
    void removeBleedingKeepsLimping() {
        PlayerWithStatus p = new PlayerWithStatus(); p.addBleeding(); p.addLimping();
        p.removeBleeding(); assertFalse(p.hasBleeding()); assertTrue(p.hasLimping());
    }


    @Test @DisplayName("removeLimping restores speed to default")
    void removeLimpingRestoresSpeed() {
        PlayerWithStatus p = new PlayerWithStatus(); p.addLimping();
        p.removeLimping(); assertFalse(p.hasLimping()); assertEquals(3, p.speed);
    }
}

package item.weapon;


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

package logic.Player;


import org.junit.jupiter.api.*;
        import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Player Tests")
class PlayerTest {


    interface Status {
        String getName();
        void apply(Player p);
        void undo(Player p);
    }


    static class FakeStatus implements Status {
        final String name;
        boolean applied = false;
        boolean undone  = false;
        FakeStatus(String name) { this.name = name; }
        @Override public String getName()      { return name; }
        @Override public void apply(Player p)  { applied = true; }
        @Override public void undo(Player p)   { undone  = true; }
    }


    static class Limping implements Status {
        @Override public String getName()     { return "Limping"; }
        @Override public void apply(Player p) { p.speed = p.speed / 2; }
        @Override public void undo(Player p)  { p.speed = p.defaultSpeed; }
    }


    static class Player {
        int hp = 5, maxHp = 5, speed = 3, defaultSpeed = 3;
        int x = 384, y = 234, width = 32, height = 32;
        char lastFacing  = 'w';
        double recoil = 0.0, maxRecoil = 10.0;
        java.util.List<Status> statusList = new java.util.ArrayList<>();


        void setHp(int v) { hp = Math.max(0, Math.min(v, maxHp)); }


        void move(char dir) {
            dir = Character.toLowerCase(dir);
            lastFacing = dir;
            switch (dir) {
                case 'w' -> { y -= speed; if (y < 0)            y = 0; }
                case 's' -> { y += speed; if (y > 500 - height) y = 500 - height; }
                case 'a' -> { x -= speed; if (x < 0)            x = 0; }
                case 'd' -> { x += speed; if (x > 800 - width)  x = 800 - width; }
            }
        }


        void applyKnockback(int force) {
            switch (lastFacing) {
                case 'w' -> y += force; case 's' -> y -= force;
                case 'a' -> x += force; case 'd' -> x -= force;
            }
        }


        void addRecoil(double amount)  { recoil = Math.min(recoil + amount, maxRecoil); }
        void recoverRecoil()           { recoil = Math.max(0.0, recoil - 0.5); }


        void addStatus(Status s) {
            if (statusList.stream().noneMatch(e -> e.getName().equals(s.getName()))) {
                s.apply(this); statusList.add(s);
            }
        }
        void removeStatus(String name) {
            statusList.removeIf(s -> { if (s.getName().equals(name)) { s.undo(this); return true; } return false; });
        }
        boolean hasStatus(String name) {
            return statusList.stream().anyMatch(s -> s.getName().equals(name));
        }
    }


    Player player;


    @BeforeEach void setUp() { player = new Player(); }


    // -- HP --


    @Test @DisplayName("Initial HP equals maxHp (5)")
    void initialHpIsMax() { assertEquals(5, player.hp); }


    @Test @DisplayName("setHp cannot exceed maxHp")
    void hpCappedAtMax() { player.setHp(999); assertEquals(5, player.hp); }


    @Test @DisplayName("setHp cannot go below 0")
    void hpFloorZero() { player.setHp(-50); assertEquals(0, player.hp); }


    @ParameterizedTest(name = "HP {0} - damage {1} = {2}")
    @CsvSource({"5,1,4", "5,5,0", "3,2,1", "5,10,0"})
    void takeDamage(int startHp, int dmg, int expected) {
        player.setHp(startHp);
        player.setHp(player.hp - dmg);
        assertEquals(expected, player.hp);
    }


    // -- Movement & Boundary --


    @Test @DisplayName("Move W decreases Y")
    void moveW() { int b = player.y; player.move('w'); assertTrue(player.y < b); }


    @Test @DisplayName("Move S increases Y")
    void moveS() { int b = player.y; player.move('s'); assertTrue(player.y > b); }


    @Test @DisplayName("Move A decreases X")
    void moveA() { int b = player.x; player.move('a'); assertTrue(player.x < b); }


    @Test @DisplayName("Move D increases X")
    void moveD() { int b = player.x; player.move('d'); assertTrue(player.x > b); }


    @Test @DisplayName("Left boundary: X >= 0")
    void leftBoundary() { for (int i=0;i<500;i++) player.move('a'); assertEquals(0, player.x); }


    @Test @DisplayName("Top boundary: Y >= 0")
    void topBoundary() { for (int i=0;i<500;i++) player.move('w'); assertEquals(0, player.y); }


    @Test @DisplayName("Right boundary: X <= 800 - 32")
    void rightBoundary() { for (int i=0;i<500;i++) player.move('d'); assertEquals(768, player.x); }


    @Test @DisplayName("Bottom boundary: Y <= 500 - 32")
    void bottomBoundary() { for (int i=0;i<500;i++) player.move('s'); assertEquals(468, player.y); }


    @Test @DisplayName("lastFacing updates per direction")
    void lastFacingUpdates() {
        player.move('d'); assertEquals('d', player.lastFacing);
        player.move('s'); assertEquals('s', player.lastFacing);
        player.move('a'); assertEquals('a', player.lastFacing);
        player.move('w'); assertEquals('w', player.lastFacing);
    }


    // -- Knockback --


    @Test @DisplayName("Knockback W: Y increases (pushed down)")
    void knockbackW() { player.move('w'); int y=player.y; player.applyKnockback(20); assertEquals(y+20,player.y); }


    @Test @DisplayName("Knockback S: Y decreases (pushed up)")
    void knockbackS() { player.move('s'); int y=player.y; player.applyKnockback(20); assertEquals(y-20,player.y); }


    @Test @DisplayName("Knockback A: X increases (pushed right)")
    void knockbackA() { player.move('a'); int x=player.x; player.applyKnockback(20); assertEquals(x+20,player.x); }


    @Test @DisplayName("Knockback D: X decreases (pushed left)")
    void knockbackD() { player.move('d'); int x=player.x; player.applyKnockback(20); assertEquals(x-20,player.x); }


    // -- Recoil --


    @Test @DisplayName("Recoil starts at 0")
    void recoilStartsZero() { assertEquals(0.0, player.recoil); }


    @Test @DisplayName("addRecoil increases recoil value")
    void addRecoilWorks() { player.addRecoil(5.0); assertEquals(5.0, player.recoil, 0.001); }


    @Test @DisplayName("Recoil capped at maxRecoil (10)")
    void recoilCapped() { player.addRecoil(100.0); assertEquals(player.maxRecoil, player.recoil, 0.001); }


    @Test @DisplayName("recoverRecoil decreases by 0.5 per tick")
    void recoverRecoilStep() { player.addRecoil(3.0); player.recoverRecoil(); assertEquals(2.5, player.recoil, 0.001); }


    @Test @DisplayName("recoverRecoil cannot go below 0")
    void recoverRecoilFloor() { player.recoverRecoil(); assertEquals(0.0, player.recoil, 0.001); }


    // -- Status --


    @Test @DisplayName("addStatus adds entry and calls apply()")
    void addStatusApplied() {
        FakeStatus st = new FakeStatus("Bleeding");
        player.addStatus(st);
        assertTrue(player.hasStatus("Bleeding"));
        assertTrue(st.applied);
    }


    @Test @DisplayName("addStatus with duplicate name does not add twice")
    void addStatusNoDuplicate() {
        player.addStatus(new FakeStatus("Bleeding"));
        player.addStatus(new FakeStatus("Bleeding"));
        assertEquals(1, player.statusList.size());
    }


    @Test @DisplayName("removeStatus removes entry and calls undo()")
    void removeStatusCallsUndo() {
        FakeStatus st = new FakeStatus("Limping");
        player.addStatus(st);
        player.removeStatus("Limping");
        assertFalse(player.hasStatus("Limping"));
        assertTrue(st.undone);
    }


    @Test @DisplayName("removeStatus on absent status does not throw")
    void removeAbsentStatusSafe() { assertDoesNotThrow(() -> player.removeStatus("Ghost")); }


    @Test @DisplayName("Limping halves player speed")
    void limpingHalvesSpeed() { player.addStatus(new Limping()); assertEquals(1, player.speed); }


    @Test @DisplayName("Limping undo restores defaultSpeed")
    void limpingUndoRestores() {
        player.addStatus(new Limping());
        player.removeStatus("Limping");
        assertEquals(player.defaultSpeed, player.speed);
    }


    @Test @DisplayName("defaultSpeed is unchanged when speed changes")
    void defaultSpeedUnchanged() { player.speed = 1; assertEquals(3, player.defaultSpeed); }
}

package status;


// ══════════════════════════════════════════════════════
//  StatusTest.java
//  ทดสอบ logic ของ Bleeding และ Limping
//  ไม่มี JavaFX / Sound / Timer จริง
// ══════════════════════════════════════════════════════


import org.junit.jupiter.api.*;
        import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Status Effect Tests")
class StatusTest {


    // ── Stub: Player ──────────────────────────────────
    static class Player {
        int hp          = 5;
        int speed       = 3;
        int maxHp       = 5;
        int defaultSpeed = 3;


        void setHp(int v)    { hp    = Math.max(0, Math.min(v, maxHp)); }
        void setSpeed(int v) { speed = v; }
    }


    // ── Bleeding (ไม่ใช้ Timer จริง — deterministic) ─
    static class Bleeding {
        static final String NAME = "Bleeding";
        boolean timerStarted   = false;
        boolean timerCancelled = false;


        void apply(Player p) {
            timerStarted = true;
            // ใน game จริง: สร้าง Timer ที่ลด HP ทุก 10s
        }


        void undo(Player p) {
            timerCancelled = true;
        }


        // simulate 1 tick (ใช้ใน test แทน Timer)
        void tick(Player p) {
            if (p.hp > 0) p.setHp(p.hp - 1);
        }


        String getName() { return NAME; }
    }


    // ── Limping ───────────────────────────────────────
    static class Limping {
        static final String NAME = "Limping";


        void apply(Player p) {
            p.setSpeed(p.speed / 2); // ลด speed ครึ่งหนึ่ง
        }


        void undo(Player p) {
            p.setSpeed(p.defaultSpeed); // คืนค่าเดิม
        }


        String getName() { return NAME; }
    }


    // ── Status list helper (mirror Player logic) ──────
    static class PlayerWithStatus {
        int hp          = 5;
        int speed       = 3;
        int maxHp       = 5;
        int defaultSpeed = 3;


        java.util.List<Object> statusList = new java.util.ArrayList<>();


        void setHp(int v)    { hp    = Math.max(0, Math.min(v, maxHp)); }
        void setSpeed(int v) { speed = v; }


        void addBleeding() {
            if (!hasBleeding()) {
                Bleeding b = new Bleeding(); b.apply(toPlayer()); statusList.add(b);
            }
        }


        void addLimping() {
            if (!hasLimping()) {
                Limping l = new Limping(); l.apply(toPlayer()); statusList.add(l);
            }
        }


        void removeBleeding() {
            statusList.removeIf(s -> {
                if (s instanceof Bleeding) { ((Bleeding) s).undo(toPlayer()); return true; }
                return false;
            });
        }


        void removeLimping() {
            statusList.removeIf(s -> {
                if (s instanceof Limping) { ((Limping) s).undo(toPlayer()); return true; }
                return false;
            });
        }


        boolean hasBleeding() { return statusList.stream().anyMatch(s -> s instanceof Bleeding); }
        boolean hasLimping()  { return statusList.stream().anyMatch(s -> s instanceof Limping); }


        private Player toPlayer() {
            PlayerWithStatus outer = this;
            Player p = new Player() {
                @Override public void setHp(int v)    { outer.hp    = Math.max(0, Math.min(v, outer.maxHp)); }
                @Override public void setSpeed(int v) { outer.speed = v; }
            };
            p.hp           = this.hp;
            p.speed        = this.speed;
            p.maxHp        = this.maxHp;
            p.defaultSpeed = this.defaultSpeed;
            return p;
        }
    }


    // ════════════════════════════════════════════════
    //  Limping Tests
    // ════════════════════════════════════════════════


    @Test @DisplayName("Limping: name = 'Limping'")
    void limpingName() {
        assertEquals("Limping", new Limping().getName());
    }


    @Test @DisplayName("Limping apply → speed ลดครึ่งหนึ่ง (3 → 1)")
    void limpingHalvesSpeed() {
        Player  p = new Player(); // speed = 3
        Limping l = new Limping();
        l.apply(p);
        assertEquals(1, p.speed); // 3 / 2 = 1 (int division)
    }


    @Test @DisplayName("Limping undo → speed คืนเป็น defaultSpeed")
    void limpingUndoRestores() {
        Player  p = new Player();
        Limping l = new Limping();
        l.apply(p);
        l.undo(p);
        assertEquals(p.defaultSpeed, p.speed);
    }


    @Test @DisplayName("Limping apply → speed ≥ 0")
    void limpingSpeedNonNegative() {
        Player  p = new Player(); p.speed = 1;
        Limping l = new Limping();
        l.apply(p);
        assertTrue(p.speed >= 0);
    }


    @Test @DisplayName("Limping apply ซ้ำ → speed ลดทบ")
    void limpingStackDoublesReduction() {
        Player  p  = new Player(); // speed = 3
        Limping l1 = new Limping();
        Limping l2 = new Limping();
        l1.apply(p); // 3 → 1
        l2.apply(p); // 1 → 0
        assertEquals(0, p.speed);
    }


    // ════════════════════════════════════════════════
    //  Bleeding Tests
    // ════════════════════════════════════════════════


    @Test @DisplayName("Bleeding: name = 'Bleeding'")
    void bleedingName() {
        assertEquals("Bleeding", new Bleeding().getName());
    }


    @Test @DisplayName("Bleeding apply → timerStarted = true")
    void bleedingApplyStartsTimer() {
        Player   p = new Player();
        Bleeding b = new Bleeding();
        b.apply(p);
        assertTrue(b.timerStarted);
    }


    @Test @DisplayName("Bleeding undo → timerCancelled = true")
    void bleedingUndoCancelsTimer() {
        Player   p = new Player();
        Bleeding b = new Bleeding();
        b.apply(p);
        b.undo(p);
        assertTrue(b.timerCancelled);
    }


    @Test @DisplayName("Bleeding tick → HP ลด 1")
    void bleedingTickReducesHp() {
        Player   p = new Player(); // hp = 5
        Bleeding b = new Bleeding();
        b.tick(p);
        assertEquals(4, p.hp);
    }


    @Test @DisplayName("Bleeding 3 ticks → HP ลด 3")
    void bleedingThreeTicks() {
        Player   p = new Player();
        Bleeding b = new Bleeding();
        b.tick(p); b.tick(p); b.tick(p);
        assertEquals(2, p.hp);
    }


    @Test @DisplayName("Bleeding tick ที่ HP = 0 → ไม่ลดต่อ")
    void bleedingStopsAtZeroHp() {
        Player   p = new Player(); p.setHp(1);
        Bleeding b = new Bleeding();
        b.tick(p); // hp = 0
        b.tick(p); // hp ยังเป็น 0 (ไม่ tick เมื่อ hp ≤ 0)
        assertEquals(0, p.hp);
    }


    // ════════════════════════════════════════════════
    //  Status Stacking (via PlayerWithStatus)
    // ════════════════════════════════════════════════


    @Test @DisplayName("addBleeding → hasStatus = true")
    void canAddBleeding() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addBleeding();
        assertTrue(p.hasBleeding());
    }


    @Test @DisplayName("addLimping → hasStatus = true + speed ลด")
    void canAddLimping() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addLimping();
        assertTrue(p.hasLimping());
        assertEquals(1, p.speed);
    }


    @Test @DisplayName("addBleeding ซ้ำ → ไม่ duplicate")
    void bleedingNoDuplicate() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addBleeding();
        p.addBleeding();
        assertEquals(1, p.statusList.size());
    }


    @Test @DisplayName("addLimping ซ้ำ → ไม่ duplicate")
    void limpingNoDuplicate() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addLimping();
        p.addLimping();
        assertEquals(1, p.statusList.size());
    }


    @Test @DisplayName("ใส่ทั้ง Bleeding และ Limping พร้อมกัน")
    void bothStatusesActive() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addBleeding();
        p.addLimping();
        assertTrue(p.hasBleeding() && p.hasLimping());
        assertEquals(2, p.statusList.size());
    }


    @Test @DisplayName("ลบ Bleeding → Limping ยังอยู่")
    void removeBleedingKeepsLimping() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addBleeding();
        p.addLimping();
        p.removeBleeding();
        assertFalse(p.hasBleeding());
        assertTrue(p.hasLimping());
    }


    @Test @DisplayName("removeLimping → speed คืนค่า")
    void removeLimpingRestoresSpeed() {
        PlayerWithStatus p = new PlayerWithStatus();
        p.addLimping();
        p.removeLimping();
        assertFalse(p.hasLimping());
        assertEquals(3, p.speed);
    }
}

package gamelogic;


import org.junit.jupiter.api.*;
        import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.junit.jupiter.api.Assertions.*;


@DisplayName("GameLogic Tests")
class GameLogicTest {


    static class GameLogic {
        boolean isGameOver = false, isWon = false, isEndless;
        int wave = 1, score = 0, currentStage, stageDurationSeconds;
        long spawnCooldown, itemDropCooldown, stageStartTime;
        double finalElapsedSeconds = 0;


        GameLogic(int stage) {
            currentStage         = stage;
            isEndless            = (stage == 4);
            stageDurationSeconds = stage * 30;
            spawnCooldown = switch (stage) {
                case 1 -> 2000L; case 2 -> 1200L; case 3 -> 700L; case 4 -> 700L; default -> 2000L;
            };
            itemDropCooldown = isEndless ? 4000L : 8000L;
            stageStartTime   = System.currentTimeMillis();
        }


        double getElapsedSeconds() { return (System.currentTimeMillis() - stageStartTime) / 1000.0; }
        int calcWave(double elapsed) { return (int)(elapsed / 10) + 1; }
        boolean isTimeUp() { return !isEndless && getElapsedSeconds() >= stageDurationSeconds; }
        void killEnemy()   { score += 10 * (isEndless ? 4 : currentStage); }
    }


    // -- Initial State --


    @Test @DisplayName("isGameOver is false at start")
    void gameOverFalseAtStart() { assertFalse(new GameLogic(1).isGameOver); }


    @Test @DisplayName("isWon is false at start")
    void wonFalseAtStart() { assertFalse(new GameLogic(1).isWon); }


    @Test @DisplayName("score is 0 at start")
    void scoreZeroAtStart() { assertEquals(0, new GameLogic(1).score); }


    @Test @DisplayName("wave is 1 at start")
    void waveOneAtStart() { assertEquals(1, new GameLogic(1).wave); }


    @Test @DisplayName("finalElapsedSeconds is 0 at start")
    void finalElapsedZeroAtStart() { assertEquals(0.0, new GameLogic(1).finalElapsedSeconds, 0.001); }


    // -- Stage / Endless --


    @Test @DisplayName("Stages 1-3 are not endless")
    void nonEndlessStages() {
        assertFalse(new GameLogic(1).isEndless);
        assertFalse(new GameLogic(2).isEndless);
        assertFalse(new GameLogic(3).isEndless);
    }


    @Test @DisplayName("Stage 4 is endless mode")
    void stage4IsEndless() { assertTrue(new GameLogic(4).isEndless); }


    @ParameterizedTest(name = "Stage {0}: duration = {1}s")
    @CsvSource({"1,30", "2,60", "3,90", "4,120"})
    void stageDuration(int stage, int expectedSeconds) {
        assertEquals(expectedSeconds, new GameLogic(stage).stageDurationSeconds);
    }


    // -- Wave Calculation --


    @ParameterizedTest(name = "Elapsed {0}s -> Wave {1}")
    @CsvSource({"0,1", "5,1", "9,1", "10,2", "19,2", "20,3", "30,4", "50,6"})
    void waveFromElapsed(double elapsed, int expectedWave) {
        assertEquals(expectedWave, new GameLogic(1).calcWave(elapsed));
    }


    // -- Spawn Cooldown --


    @ParameterizedTest(name = "Stage {0}: spawnCooldown = {1}ms")
    @CsvSource({"1,2000", "2,1200", "3,700", "4,700"})
    void spawnCooldownPerStage(int stage, long expected) {
        assertEquals(expected, new GameLogic(stage).spawnCooldown);
    }


    @Test @DisplayName("Stage 3 and 4 have the same spawn rate")
    void stage3And4SameSpawnRate() {
        assertEquals(new GameLogic(3).spawnCooldown, new GameLogic(4).spawnCooldown);
    }


    @Test @DisplayName("Spawn rate accelerates each stage")
    void spawnAcceleratesOverStages() {
        assertTrue(new GameLogic(1).spawnCooldown > new GameLogic(2).spawnCooldown);
        assertTrue(new GameLogic(2).spawnCooldown > new GameLogic(3).spawnCooldown);
    }


    // -- Item Drop Cooldown --


    @Test @DisplayName("Normal stages: itemDropCooldown = 8000ms")
    void normalItemDropCooldown() {
        assertEquals(8000L, new GameLogic(1).itemDropCooldown);
        assertEquals(8000L, new GameLogic(2).itemDropCooldown);
        assertEquals(8000L, new GameLogic(3).itemDropCooldown);
    }


    @Test @DisplayName("Endless mode: itemDropCooldown = 4000ms (more frequent)")
    void endlessItemDropFaster() { assertEquals(4000L, new GameLogic(4).itemDropCooldown); }


    @Test @DisplayName("Endless item drop is faster than normal stages")
    void endlessItemDropFasterThanNormal() {
        assertTrue(new GameLogic(4).itemDropCooldown < new GameLogic(1).itemDropCooldown);
    }


    // -- Score --


    @ParameterizedTest(name = "Stage {0}: 1 kill = {1} score")
    @CsvSource({"1,10", "2,20", "3,30"})
    void scorePerKill(int stage, int expectedScore) {
        GameLogic gl = new GameLogic(stage); gl.killEnemy(); assertEquals(expectedScore, gl.score);
    }


    @Test @DisplayName("Endless mode: 1 kill = 40 score (4x multiplier)")
    void endlessScoreMultiplier() { GameLogic gl = new GameLogic(4); gl.killEnemy(); assertEquals(40, gl.score); }


    @Test @DisplayName("Score accumulates correctly over multiple kills")
    void scoreAccumulates() {
        GameLogic gl = new GameLogic(1);
        gl.killEnemy(); gl.killEnemy(); gl.killEnemy();
        assertEquals(30, gl.score);
    }


    @Test @DisplayName("Score multiplier increases with stage number")
    void scoreMultiplierScalesWithStage() {
        GameLogic g1 = new GameLogic(1); g1.killEnemy();
        GameLogic g2 = new GameLogic(2); g2.killEnemy();
        GameLogic g3 = new GameLogic(3); g3.killEnemy();
        assertTrue(g1.score < g2.score && g2.score < g3.score);
    }


    // -- Timer --


    @Test @DisplayName("isTimeUp is false immediately after init")
    void notTimeUpAtStart() { assertFalse(new GameLogic(1).isTimeUp()); }


    @Test @DisplayName("Endless mode: isTimeUp is never true")
    void endlessNeverTimesUp() {
        GameLogic gl = new GameLogic(4);
        gl.stageStartTime -= 999_999_999L;
        assertFalse(gl.isTimeUp());
    }


    @Test @DisplayName("getElapsedSeconds is non-negative")
    void elapsedNonNegative() { assertTrue(new GameLogic(1).getElapsedSeconds() >= 0); }


    @Test @DisplayName("getElapsedSeconds increases over time")
    void elapsedIncreases() throws InterruptedException {
        GameLogic gl = new GameLogic(1);
        double t1 = gl.getElapsedSeconds();
        Thread.sleep(50);
        assertTrue(gl.getElapsedSeconds() > t1);
    }
}

