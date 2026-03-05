package GameLogic;

import Item.Bullet.Bullet;
import Item.HealingItem.Medkit;
import Item.HealingItem.Bandage;
import Item.Weapon.MachineGun;
import Item.Weapon.Pistol;
import Item.Weapon.Weapon;
import Item.Weapon.Gun;
import Player.Player;
import enemy.BaseEnemy;
import enemy.*;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {

    public Player player;
    public List<BaseEnemy> enemies;
    public static List<Bullet> bullets;
    public List<Item.Item> itemsOnGround;

    public boolean isGameOver = false;
    public boolean isWon = false;
    public int wave = 1;
    public int score = 0;

    private int currentStage = 1;
    private int stageDurationSeconds = 30;
    private long stageStartTime = 0;

    private long lastSpawnTime = 0;
    private long spawnCooldown = 2000;
    private long lastItemDropTime = 0;
    private long itemDropCooldown = 8000; // drop item every 8s
    private Random random = new Random();

    private int screenWidth = 800;
    private int screenHeight = 500; // 600 - 52 HUD - 48 item bar

    public GameLogic() { initGame(1); }

    public void initGame(int stage) {
        currentStage = stage;
        stageDurationSeconds = stage * 30;
        spawnCooldown = switch (stage) {
            case 1 -> 2000;
            case 2 -> 1200;
            case 3 -> 700;
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
        player.setSpeed(player.getDefaultSpeed());

        // Give player a starting pistol
        player.addItem(new Pistol());

        stageStartTime = System.currentTimeMillis();
        lastSpawnTime = stageStartTime;
        lastItemDropTime = stageStartTime;
    }

    public void initGame() { initGame(1); }

    public static void addBullet(Bullet b) {
        if (bullets != null) bullets.add(b);
    }

    public double getElapsedSeconds() {
        return (System.currentTimeMillis() - stageStartTime) / 1000.0;
    }

    public void update(boolean w, boolean a, boolean s, boolean d, boolean isMousePressed, double mouseX, double mouseY) {
        if (isGameOver || isWon) return;

        if (getElapsedSeconds() >= stageDurationSeconds) {
            isWon = true;
            player.getStatusList().forEach(st -> player.removeStatus(st.getName()));
            return;
        }

        wave = (int) (getElapsedSeconds() / 10) + 1;

        if (w) player.move('w');
        if (s) player.move('s');
        if (a) player.move('a');
        if (d) player.move('d');
        player.recoverRecoil();

        if (isMousePressed) {
            Weapon currentWeapon = player.getEquippedWeapon();
            if (currentWeapon instanceof Gun gun) {  // cast + check ในบรรทัดเดียว (Java 16+)
                gun.setMouseTarget(mouseX, mouseY);
                gun.use(player);
                if (gun.isEmpty()) player.removeItem(gun);
            }
        }

        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.getX() < 0 || b.getX() > screenWidth || b.getY() < 0 || b.getY() > screenHeight) {
                bullets.remove(i--);
            }
        }

        for (int i = 0; i < itemsOnGround.size(); i++) {
            Item.Item item = itemsOnGround.get(i);
            if (player.getBounds().intersects(item.getBounds())) {
                item.onPickUp(player);
                itemsOnGround.remove(i--);
            }
        }

        handleSpawning();
        handleItemDrops();

        for (int i = 0; i < enemies.size(); i++) {
            BaseEnemy e = enemies.get(i);
            e.update(player);
            checkBulletHit(e);
            if (e.isDead()) {
                enemies.remove(i--);
                score += 10 * currentStage;
            }
        }

        if (player.getHp() <= 0) {
            isGameOver = true;
            player.getStatusList().forEach(st -> player.removeStatus(st.getName()));
        }
    }

    private void checkBulletHit(BaseEnemy enemy) {
        java.awt.Rectangle rect = new java.awt.Rectangle(
                enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (b.getBounds().intersects(rect)) {
                enemy.takeDamage(b.getDamage());
                bullets.remove(i);
                break;
            }
        }
    }

    private void handleSpawning() {
        long now = System.currentTimeMillis();
        if (now - lastSpawnTime > spawnCooldown) {
            int cx = screenWidth / 2;
            int cy = screenHeight / 2;
            int spread = 200;
            int sx = Math.max(0, Math.min(cx + random.nextInt(spread * 2) - spread, screenWidth - 32));
            int sy = Math.max(0, Math.min(cy + random.nextInt(spread * 2) - spread, screenHeight - 32));

            int roll = random.nextInt(100);
            BaseEnemy spawned;
            if (currentStage == 1) {
                // Stage 1: basic zombie (hp1 spd1 dmg1) + runner (hp1 spd2 dmg1)
                spawned = roll < 35 ? new Runners(sx, sy) : new zombie(sx, sy);
            } else if (currentStage == 2) {
                // Stage 2: adds juggernaut, bleed, slow
                if      (roll < 15) spawned = new juggernaut(sx, sy);
                else if (roll < 35) spawned = new Runners(sx, sy);
                else if (roll < 55) spawned = new AnimalZombies(sx, sy);
                else if (roll < 70) spawned = new SlowZombie(sx, sy);
                else                spawned = new zombie(sx, sy);
            } else {
                // Stage 3: all types including damage-2 and screamers
                if      (roll < 12) spawned = new juggernaut(sx, sy);
                else if (roll < 27) spawned = new Screamers(sx, sy);
                else if (roll < 44) spawned = new Runners(sx, sy);
                else if (roll < 60) spawned = new AnimalZombies(sx, sy);
                else if (roll < 75) spawned = new SlowZombie(sx, sy);
                else if (roll < 88) spawned = new HeavyZombie(sx, sy);
                else                spawned = new zombie(sx, sy);
            }
            enemies.add(spawned);
            lastSpawnTime = now;

            long minCD = switch (currentStage) { case 1 -> 800; case 2 -> 500; default -> 300; };
            if (spawnCooldown > minCD) spawnCooldown -= 5;
        }
    }

    private void handleItemDrops() {
        long now = System.currentTimeMillis();
        if (now - lastItemDropTime > itemDropCooldown) {
            int ix = random.nextInt(screenWidth - 20);
            int iy = random.nextInt(screenHeight - 20);
            int roll = random.nextInt(100);
            Item.Item drop;
            if      (roll < 25) drop = new Medkit();
            else if (roll < 50) drop = new Bandage();
            else if (roll < 75) drop = new Pistol();
            else                drop = new MachineGun();
            drop.setX(ix);
            drop.setY(iy);
            itemsOnGround.add(drop);
            lastItemDropTime = now;
        }
    }

    public void draw(Graphics2D g) {
        // legacy swing draw — not used in JavaFX path
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        for (BaseEnemy e : enemies) e.draw(g);
    }
}