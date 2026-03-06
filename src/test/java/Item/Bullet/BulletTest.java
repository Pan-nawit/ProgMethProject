package Item.Bullet;


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

