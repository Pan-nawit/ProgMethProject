package Item;

import Interface.Pickable;
import Player.Player;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Rectangle;
import java.net.URL;

public abstract class Item implements Pickable {
    protected String name;
    protected int amount;
    protected int x, y;
    protected int width = 16;
    protected int height = 16;

    public Item(String name, int amount) {
        setName(name);
        setAmount(amount);
    }
    public void addAmount(int amount) {
        setAmount(getAmount()+amount);
    }
    protected void reduceAmount(){
        if(amount>0) setAmount(getAmount()-1);
    }
    public boolean isEmpty() {
        return amount <= 0;
    }
    @Override
    public void onPickUp(Player player) {
        player.addItem(this); // เมื่อเก็บไอเทม ให้เพิ่มตัวเองลงใน inventory ของ player
        System.out.println("Picked up: " + this.name);
    }
    public abstract void use(Player player);

    public static void playGunSound() {
        new Thread(() -> {
            try {
                URL url = Item.class.getResource("/Sound/Gun/gunshot.wav");
                if (url != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                } else {
                    System.out.println("⚠️ Warning: Sound not found at /Sounds/Gun/gunshot.wav");
                }
            } catch (Exception e) {
                System.out.println("❌ Error playing sound: " + e.getMessage());
            }
        }).start();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}
