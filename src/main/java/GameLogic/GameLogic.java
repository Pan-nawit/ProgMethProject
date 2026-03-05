package GameLogic;

import Item.Bullet.Bullet;
import Item.Weapon.Weapon;
import Player.Player;
import enemy.BaseEnemy;
import enemy.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {

    // --- Characters & Enemies ---
    public Player player;
    public List<BaseEnemy> enemies;

    public static List<Bullet> bullets;
    public List<Item.Item> itemsOnGround;

    // --- Game State ---
    public boolean isGameOver = false;
    public boolean isWon = false;
    public int wave = 1;
    public int score = 0;

    // --- Stage / Timer ---
    private int currentStage = 1;
    private int stageDurationSeconds = 30; // set by initGame(stage)
    private long stageStartTime = 0;

    // --- Spawning ---
    private long lastSpawnTime = 0;
    private long spawnCooldown = 2000;
    private Random random = new Random();

    private int screenWidth = 800;
    private int screenHeight = 600;

    public GameLogic() {
        initGame(1);
    }

    public void initGame(int stage) {
        currentStage = stage;
        // Stage durations: Stage 1 = 30s, Stage 2 = 60s, Stage 3 = 90s
        stageDurationSeconds = stage * 30;
        // Scale difficulty per stage
        spawnCooldown = switch (stage) {
            case 1 -> 2000;   // 1 enemy every 2s
            case 2 -> 1200;   // 1 enemy every 1.2s
            case 3 -> 700;    // 1 enemy every 0.7s
            default -> 2000;
        };

        player = new Player();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        itemsOnGround = new ArrayList<>();
        isGameOver = false;
        isWon = false;
        wave = 1;
        score = 0;
        player.setHp(5);
        stageStartTime = System.currentTimeMillis();
        lastSpawnTime = stageStartTime;
    }

    // kept for backward compat
    public void initGame() { initGame(1); }

    public static void addBullet(Bullet b) {
        if (bullets != null) bullets.add(b);
    }

    /** Returns elapsed seconds since stage started */
    public double getElapsedSeconds() {
        return (System.currentTimeMillis() - stageStartTime) / 1000.0;
    }

    public void update(boolean w, boolean a, boolean s, boolean d, boolean isMousePressed) {
        if (isGameOver || isWon) return;

        // ── Win condition: survived long enough ──────────────
        if (getElapsedSeconds() >= stageDurationSeconds) {
            isWon = true;
            // Stop all status timers
            player.getStatusList().forEach(status -> player.removeStatus(status.getName()));
            return;
        }

        // Wave counter (every 10s advance wave)
        wave = (int)(getElapsedSeconds() / 10) + 1;

        // 1. Player movement
        if (w) player.move('w');
        if (s) player.move('s');
        if (a) player.move('a');
        if (d) player.move('d');
        player.recoverRecoil();

        // 2. Shooting
        if (isMousePressed) {
            Weapon currentWeapon = player.getEquippedWeapon();
            if (currentWeapon != null) {
                currentWeapon.use(player);
                if (currentWeapon.isEmpty()) {
                    player.removeItem(currentWeapon);
                }
            }
        }

        // 3. Bullet update
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.getX() < 0 || b.getX() > screenWidth || b.getY() < 0 || b.getY() > screenHeight) {
                bullets.remove(i--);
            }
        }

        // 4. Item pickup
        for (int i = 0; i < itemsOnGround.size(); i++) {
            Item.Item item = itemsOnGround.get(i);
            if (player.getBounds().intersects(item.getBounds())) {
                item.onPickUp(player);
                itemsOnGround.remove(i--);
            }
        }

        // 5. Spawning & enemy update
        handleSpawning();
        for (int i = 0; i < enemies.size(); i++) {
            BaseEnemy e = enemies.get(i);
            e.update(player);
            checkBulletHit(e);

            if (e.isDead()) {
                enemies.remove(i--);
                score += 10 * currentStage; // higher stages = more score
            }
        }

        // 6. Game Over check
        if (player.getHp() <= 0) {
            isGameOver = true;
            player.getStatusList().forEach(status -> player.removeStatus(status.getName()));
        }
    }

    private void checkBulletHit(BaseEnemy enemy) {
        java.awt.Rectangle enemyRect = new java.awt.Rectangle(
                enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (b.getBounds().intersects(enemyRect)) {
                enemy.takeDamage(b.getDamage());
                bullets.remove(i);
                break;
            }
        }
    }

    private void handleSpawning() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime > spawnCooldown) {

            // Spawn off-screen edges
            int spawnX, spawnY;
            int edge = random.nextInt(4);
            switch (edge) {
                case 0 -> { spawnX = random.nextInt(screenWidth); spawnY = -40; }
                case 1 -> { spawnX = random.nextInt(screenWidth); spawnY = screenHeight + 10; }
                case 2 -> { spawnX = -40; spawnY = random.nextInt(screenHeight); }
                default -> { spawnX = screenWidth + 10; spawnY = random.nextInt(screenHeight); }
            }

            // Spawn variety scales with stage and wave
            int roll = random.nextInt(100);
            BaseEnemy spawned;
            if (currentStage == 1) {
                spawned = roll < 30 ? new Runners(spawnX, spawnY) : new zombie(spawnX, spawnY);
            } else if (currentStage == 2) {
                if (roll < 20) spawned = new juggernaut(spawnX, spawnY);
                else if (roll < 50) spawned = new Runners(spawnX, spawnY);
                else if (roll < 70) spawned = new AnimalZombies(spawnX, spawnY);
                else spawned = new zombie(spawnX, spawnY);
            } else {
                // Stage 3 – hardest mix
                if (roll < 15) spawned = new juggernaut(spawnX, spawnY);
                else if (roll < 35) spawned = new Screamers(spawnX, spawnY);
                else if (roll < 55) spawned = new Runners(spawnX, spawnY);
                else if (roll < 75) spawned = new AnimalZombies(spawnX, spawnY);
                else spawned = new zombie(spawnX, spawnY);
            }
            enemies.add(spawned);
            lastSpawnTime = currentTime;

            // Gradually speed up spawning (cap per stage)
            long minCooldown = switch (currentStage) { case 1 -> 800; case 2 -> 500; default -> 300; };
            if (spawnCooldown > minCooldown) spawnCooldown -= 5;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(java.awt.Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillRect(b.getX(), b.getY(), 4, 4);
        }
        for (Item.Item item : itemsOnGround) {
            if (item.getImage() != null) {
                g.drawImage(item.getImage(), item.getX(), item.getY(), null);
            }
        }
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        for (BaseEnemy e : enemies) {
            e.draw(g);
        }
    }
}