package Player;

import static org.junit.jupiter.api.Assertions.*;

import Player.Player;
import Item.Item;
import Item.Weapon.Weapon;
import Status.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        // สร้าง instance ใหม่ก่อนเริ่มทุก test case
        player = new Player();
    }

    @Test
    @DisplayName("ตรวจสอบค่าเริ่มต้นของ Player เมื่อสร้างตัวละคร")
    void testInitialStats() {
        assertEquals(5, player.getHp(), "HP เริ่มต้นควรเป็น 5");
        assertEquals(3, player.getSpeed(), "Speed เริ่มต้นควรเป็น 3");
        assertEquals(384, player.getX());
        assertEquals(234, player.getY());
    }

    @Test
    @DisplayName("ทดสอบการเคลื่อนที่และขอบเขตของจอ (Boundaries)")
    void testMovement() {
        // เดินขึ้น (W)
        player.move('w');
        assertEquals(231, player.getY());

        // เดินซ้ายจนสุดขอบจอ (A)
        for(int i=0; i<200; i++) player.move('a');
        assertEquals(0, player.getX(), "X ไม่ควรน้อยกว่า 0");

        // เดินขวาจนสุดขอบจอ (D)
        for(int i=0; i<400; i++) player.move('d');
        assertTrue(player.getX() <= 800 - player.getWidth(), "X ไม่ควรเกินขอบขวา");
    }

    @Test
    @DisplayName("ทดสอบการรับดาเมจและการจำกัด HP")
    void testTakeDamage() {
        player.onAttacked(2, null);
        assertEquals(3, player.getHp());

        player.onAttacked(10, null);
        assertEquals(0, player.getHp(), "HP ไม่ควรติดลบ");

        player.setHp(100);
        assertEquals(5, player.getHp(), "HP ไม่ควรเกิน MaxHP (5)");
    }


    @Test
    @DisplayName("ทดสอบการจัดการ Recoil")
    void testRecoilSystem() {
        player.addRecoil(5.0);
        assertEquals(5.0, player.getRecoil());

        player.addRecoil(20.0); // เกิน Max (10.0)
        assertEquals(10.0, player.getRecoil());

        player.recoverRecoil();
        assertEquals(9.5, player.getRecoil());
    }

    @Test
    @DisplayName("ทดสอบ Knockback ตามทิศทางล่าสุด")
    void testKnockback() {
        // สมมติเดินขวา (d) ล่าสุด
        player.move('d');
        int currentX = player.getX();

        player.applyKnockback(10);
        // ตาม logic ใน code: case 'd' -> x -= force
        assertEquals(currentX - 10, player.getX());
    }
}