package enemy;

import Interface.Attackable;
import Player.Player;

import java.awt.*;

public abstract class BaseEnemy implements Attackable {
    // Stats
    protected int hp;
    protected int maxHp; // เพิ่มตัวนี้ เพื่อไว้คำนวณหลอดเลือด
    protected int speed;
    protected int strength; // damage

    // Position
    protected float x, y;
    protected int width, height;

    // Status
    protected boolean isHit = false;

    // Cooldown System (สำคัญมาก!)
    protected long lastAttackTime = 0;
    protected long attackCooldown = 1000; // 1000ms = 1 วินาที ตีได้ 1 ครั้ง

    public BaseEnemy(int maxHp, int speed, int strength, float startX, float startY) {
        this.maxHp = maxHp;
        setHP(maxHp);
        setSPEED(speed);
        setSTRENGTH(strength);
        this.x = startX;
        this.y = startY;
        setHeight(32);
        setWidth(32);
    }
    public void update(Player player) {
        if (isDead()) return;

        // 1. Logic การเดิน (เหมือนเดิม แต่จัดระเบียบ code)
        moveTowards(player);

        // 2. Logic การโจมตี (เพิ่มเข้ามา)
        Attackplayer(player);

        // รีเซ็ตสถานะ isHit (ในเกมจริงอาจต้องใช้ Timer นับถอยหลังสัก 0.2 วิ เพื่อให้กระพริบนานขึ้น)
        isHit = false;
    }
    protected void moveTowards(Player p) {
        float diffX = p.getX() - this.x;
        float diffY = p.getY() - this.y;
        float distance = (float) Math.sqrt((diffX * diffX) + (diffY * diffY));

        // เดินเข้าหาเมื่อยังไม่ชน (ระยะห่าง > 1)
        if (distance > 1) {
            x += (diffX / distance) * speed;
            y += (diffY / distance) * speed;
        }
    }
    protected boolean isCooldownReady() {
        return System.currentTimeMillis() - lastAttackTime > attackCooldown;
    }

    // Helper Function: รีเซ็ตเวลาโจมตี
    protected void resetCooldown() {
        lastAttackTime = System.currentTimeMillis();
    }
    public void draw(Graphics2D g) {
        if (isDead()) return;

        // วาดตัว (กระพริบเมื่อโดนตี)
        if (isHit) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.RED);
        }
        g.fillRect((int)x, (int)y, width, height);

        // วาดหลอดเลือด (แก้ไขสูตร)
        drawHealthBar(g);
    }
    private void drawHealthBar(Graphics2D g) {
        // พื้นหลังหลอดเลือด (สีดำ)
        g.setColor(Color.BLACK);
        g.fillRect((int)x, (int)y - 10, width, 5);

        // หลอดเลือดจริง (สีเขียว)
        g.setColor(Color.GREEN);

        // สูตร: (เลือดปัจจุบัน / เลือดเต็ม) * ความกว้าง
        double hpPercent = (double) hp / (double) maxHp;
        int barWidth = (int) (width * hpPercent);

        g.fillRect((int)x, (int)y - 10, barWidth, 5);

        // วาดขอบหลอดเลือดหน่อยจะได้สวย
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y - 10, width, 5);
    }
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    public void takeDamage(int dmg) {
        this.hp -= dmg;
        this.isHit = true;

        // (Optional) Knockback ศัตรูถอยหลังเล็กน้อย
        // ต้องรู้ทิศทางกระสุนถึงจะทำได้สมบูรณ์ แต่แบบง่ายคือสุ่มถอย หรือถอยตรงข้ามผู้เล่น
    }

    public boolean isDead() {
        return hp <= 0;
    }
    public int getHP() {
        return hp;
    }
    public void setHP(int HP) {
        this.hp = HP;
    }
    public int getSPEED() {
        return speed;
    }
    public void setSPEED(int SPEED) {
        this.speed = SPEED;
    }
    public int getSTRENGTH() {
        return strength;
    }
    public void setSTRENGTH(int STRENGTH) {
        this.strength = STRENGTH;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public void setAttackCooldown(long cooldown) {
        this.attackCooldown = cooldown;
    }
    public int getX(){
        return (int)x;
    }
    public int getY(){
        return (int)y;
    }


    public int getMaxHp() { return maxHp; }

    /** Override in subclasses to change color. Default = red */
    public javafx.scene.paint.Color getEnemyColor() {
        return javafx.scene.paint.Color.web("#c0392b");
    }

}