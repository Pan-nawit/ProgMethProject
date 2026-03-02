package GameLogic;

import Player.Player;
import enemy.BaseEnemy;
import enemy.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {

    // --- ตัวละครและศัตรู ---
    public Player player;
    public List<BaseEnemy> enemies;

    // --- สถานะเกม ---
    public boolean isGameOver = false;
    public int wave = 1;
    public int score = 0;

    // --- ตัวจัดการการเกิด (Spawning) ---
    private long lastSpawnTime = 0;
    private long spawnCooldown = 2000; // 2 วินาทีเกิด 1 ตัว (เริ่มต้น)
    private Random random = new Random();

    // --- ขนาดหน้าจอ (เพื่อสุ่มจุดเกิด) ---
    private int screenWidth = 800;
    private int screenHeight = 600;

    public GameLogic() {
        initGame();
    }

    public void initGame() {
        player = new Player(); // สร้างผู้เล่นใหม่
        enemies = new ArrayList<>();
        isGameOver = false;
        wave = 1;
        score = 0;
        player.setHp(5); // ตั้งเลือดเริ่มต้น
    }

    public void update(boolean w, boolean a, boolean s, boolean d) {
        if (isGameOver) return;

        // 1. อัปเดตผู้เล่น (เดิน)
        if (w) player.move('w');
        if (s) player.move('s');
        if (a) player.move('a');
        if (d) player.move('d');

        // ฟื้นฟูค่า Recoil หรือ Status ต่างๆ ของผู้เล่น
        player.recoverRecoil();

        // (Optional) อัปเดตสถานะผิดปกติ (Status Effects) 
        // ปกติ TimerTask ของ Bleeding ทำงานแยก Thread อยู่แล้ว แต่ถ้ามี status อื่นๆ ใส่ตรงนี้ได้

        // 2. ระบบเกิดศัตรู (Spawning Logic)
        handleSpawning();

        // 3. อัปเดตศัตรูทั้งหมด
        for (int i = 0; i < enemies.size(); i++) {
            BaseEnemy e = enemies.get(i);

            // สั่งให้ศัตรูเดินและโจมตีผู้เล่น
            e.update(player);

            // เช็คว่าศัตรูตายหรือยัง?
            if (e.isDead()) {
                enemies.remove(i);
                i--; // ถอย index กลับ
                score += 10; // ได้แต้ม
                System.out.println("Enemy Killed! Score: " + score);
            }
        }

        // 4. เช็ค Game Over
        if (player.getHp() <= 0) {
            isGameOver = true;
            System.out.println("=== GAME OVER ===");
            // หยุด Timer ของ Status ทั้งหมด (สำคัญมาก ไม่งั้นเลือดจะลดต่อหลังตาย)
            player.getStatusList().forEach(status -> player.removeStatus(status.getName()));
        }
    }

    private void handleSpawning() {
        long currentTime = System.currentTimeMillis();

        // ถ้าถึงเวลาเกิด
        if (currentTime - lastSpawnTime > spawnCooldown) {

            // สุ่มตำแหน่งเกิด (ให้เกิดขอบๆ จอ ไม่เกิดทับตัวผู้เล่น)
            int spawnX = random.nextInt(screenWidth);
            int spawnY = random.nextInt(screenHeight);

            // สุ่มประเภทศัตรู (30% เป็นหมาวิ่งเร็ว, 70% เป็นซอมบี้ธรรมดา)
            if (random.nextInt(100) < 30) {
                enemies.add(new AnimalZombies(spawnX, spawnY)); // หมา (วิ่งไว กัดติด Bleed)
            } else {
                // สมมติว่ามี Class Zombie ปกติ
                // enemies.add(new Zombie(spawnX, spawnY)); 
                // หรือใช้ AnimalZombies ไปก่อนถ้ายังไม่มี Zombie ธรรมดา
                enemies.add(new zombie(spawnX, spawnY));
            }

            lastSpawnTime = currentTime;

            // เพิ่มความยาก: ลดเวลาเกิดลงเรื่อยๆ (ต่ำสุด 0.5 วิ)
            if (spawnCooldown > 500) {
                spawnCooldown -= 10;
            }
        }
    }

    // ฟังก์ชันวาด (เรียกจากหน้าจอหลัก)
    public void draw(Graphics2D g) {
        // วาด Player
        // (คุณอาจต้องเขียน method draw ใน Player เพิ่ม หรือวาดสดตรงนี้)
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        // วาดศัตรู
        for (BaseEnemy e : enemies) {
            e.draw(g);
        }
    }
}