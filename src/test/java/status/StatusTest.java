package status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


        void apply(status.StatusTest.Player p) {
            timerStarted = true;
            // ใน game จริง: สร้าง Timer ที่ลด HP ทุก 10s
        }


        void undo(status.StatusTest.Player p) {
            timerCancelled = true;
        }


        // simulate 1 tick (ใช้ใน test แทน Timer)
        void tick(status.StatusTest.Player p) {
            if (p.hp > 0) p.setHp(p.hp - 1);
        }


        String getName() { return NAME; }
    }


    // ── Limping ───────────────────────────────────────
    static class Limping {
        static final String NAME = "Limping";


        void apply(status.StatusTest.Player p) {
            p.setSpeed(p.speed / 2); // ลด speed ครึ่งหนึ่ง
        }


        void undo(status.StatusTest.Player p) {
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
                status.StatusTest.Bleeding b = new status.StatusTest.Bleeding(); b.apply(toPlayer()); statusList.add(b);
            }
        }


        void addLimping() {
            if (!hasLimping()) {
                status.StatusTest.Limping l = new status.StatusTest.Limping(); l.apply(toPlayer()); statusList.add(l);
            }
        }


        void removeBleeding() {
            statusList.removeIf(s -> {
                if (s instanceof status.StatusTest.Bleeding) { ((status.StatusTest.Bleeding) s).undo(toPlayer()); return true; }
                return false;
            });
        }


        void removeLimping() {
            statusList.removeIf(s -> {
                if (s instanceof status.StatusTest.Limping) { ((status.StatusTest.Limping) s).undo(toPlayer()); return true; }
                return false;
            });
        }


        boolean hasBleeding() { return statusList.stream().anyMatch(s -> s instanceof status.StatusTest.Bleeding); }
        boolean hasLimping()  { return statusList.stream().anyMatch(s -> s instanceof status.StatusTest.Limping); }


        private status.StatusTest.Player toPlayer() {
            status.StatusTest.PlayerWithStatus outer = this;
            status.StatusTest.Player p = new status.StatusTest.Player() {
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


    @Test
    @DisplayName("Limping: name = 'Limping'")
    void limpingName() {
        assertEquals("Limping", new status.StatusTest.Limping().getName());
    }


    @Test @DisplayName("Limping apply → speed ลดครึ่งหนึ่ง (3 → 1)")
    void limpingHalvesSpeed() {
        status.StatusTest.Player p = new status.StatusTest.Player(); // speed = 3
        status.StatusTest.Limping l = new status.StatusTest.Limping();
        l.apply(p);
        assertEquals(1, p.speed); // 3 / 2 = 1 (int division)
    }


    @Test @DisplayName("Limping undo → speed คืนเป็น defaultSpeed")
    void limpingUndoRestores() {
        status.StatusTest.Player p = new status.StatusTest.Player();
        status.StatusTest.Limping l = new status.StatusTest.Limping();
        l.apply(p);
        l.undo(p);
        assertEquals(p.defaultSpeed, p.speed);
    }


    @Test @DisplayName("Limping apply → speed ≥ 0")
    void limpingSpeedNonNegative() {
        status.StatusTest.Player p = new status.StatusTest.Player(); p.speed = 1;
        status.StatusTest.Limping l = new status.StatusTest.Limping();
        l.apply(p);
        assertTrue(p.speed >= 0);
    }


    @Test @DisplayName("Limping apply ซ้ำ → speed ลดทบ")
    void limpingStackDoublesReduction() {
        status.StatusTest.Player p  = new status.StatusTest.Player(); // speed = 3
        status.StatusTest.Limping l1 = new status.StatusTest.Limping();
        status.StatusTest.Limping l2 = new status.StatusTest.Limping();
        l1.apply(p); // 3 → 1
        l2.apply(p); // 1 → 0
        assertEquals(0, p.speed);
    }


    // ════════════════════════════════════════════════
    //  Bleeding Tests
    // ════════════════════════════════════════════════


    @Test @DisplayName("Bleeding: name = 'Bleeding'")
    void bleedingName() {
        assertEquals("Bleeding", new status.StatusTest.Bleeding().getName());
    }


    @Test @DisplayName("Bleeding apply → timerStarted = true")
    void bleedingApplyStartsTimer() {
        status.StatusTest.Player p = new status.StatusTest.Player();
        status.StatusTest.Bleeding b = new status.StatusTest.Bleeding();
        b.apply(p);
        assertTrue(b.timerStarted);
    }


    @Test @DisplayName("Bleeding undo → timerCancelled = true")
    void bleedingUndoCancelsTimer() {
        status.StatusTest.Player p = new status.StatusTest.Player();
        status.StatusTest.Bleeding b = new status.StatusTest.Bleeding();
        b.apply(p);
        b.undo(p);
        assertTrue(b.timerCancelled);
    }


    @Test @DisplayName("Bleeding tick → HP ลด 1")
    void bleedingTickReducesHp() {
        status.StatusTest.Player p = new status.StatusTest.Player(); // hp = 5
        status.StatusTest.Bleeding b = new status.StatusTest.Bleeding();
        b.tick(p);
        assertEquals(4, p.hp);
    }


    @Test @DisplayName("Bleeding 3 ticks → HP ลด 3")
    void bleedingThreeTicks() {
        status.StatusTest.Player p = new status.StatusTest.Player();
        status.StatusTest.Bleeding b = new status.StatusTest.Bleeding();
        b.tick(p); b.tick(p); b.tick(p);
        assertEquals(2, p.hp);
    }


    @Test @DisplayName("Bleeding tick ที่ HP = 0 → ไม่ลดต่อ")
    void bleedingStopsAtZeroHp() {
        status.StatusTest.Player p = new status.StatusTest.Player(); p.setHp(1);
        status.StatusTest.Bleeding b = new status.StatusTest.Bleeding();
        b.tick(p); // hp = 0
        b.tick(p); // hp ยังเป็น 0 (ไม่ tick เมื่อ hp ≤ 0)
        assertEquals(0, p.hp);
    }


    // ════════════════════════════════════════════════
    //  Status Stacking (via PlayerWithStatus)
    // ════════════════════════════════════════════════


    @Test @DisplayName("addBleeding → hasStatus = true")
    void canAddBleeding() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addBleeding();
        assertTrue(p.hasBleeding());
    }


    @Test @DisplayName("addLimping → hasStatus = true + speed ลด")
    void canAddLimping() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addLimping();
        assertTrue(p.hasLimping());
        assertEquals(1, p.speed);
    }


    @Test @DisplayName("addBleeding ซ้ำ → ไม่ duplicate")
    void bleedingNoDuplicate() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addBleeding();
        p.addBleeding();
        assertEquals(1, p.statusList.size());
    }


    @Test @DisplayName("addLimping ซ้ำ → ไม่ duplicate")
    void limpingNoDuplicate() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addLimping();
        p.addLimping();
        assertEquals(1, p.statusList.size());
    }


    @Test @DisplayName("ใส่ทั้ง Bleeding และ Limping พร้อมกัน")
    void bothStatusesActive() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addBleeding();
        p.addLimping();
        assertTrue(p.hasBleeding() && p.hasLimping());
        assertEquals(2, p.statusList.size());
    }


    @Test @DisplayName("ลบ Bleeding → Limping ยังอยู่")
    void removeBleedingKeepsLimping() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addBleeding();
        p.addLimping();
        p.removeBleeding();
        assertFalse(p.hasBleeding());
        assertTrue(p.hasLimping());
    }


    @Test @DisplayName("removeLimping → speed คืนค่า")
    void removeLimpingRestoresSpeed() {
        status.StatusTest.PlayerWithStatus p = new status.StatusTest.PlayerWithStatus();
        p.addLimping();
        p.removeLimping();
        assertFalse(p.hasLimping());
        assertEquals(3, p.speed);
    }
}



