package Item;

import Interface.Pickable;
import Player.Player;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.image.BufferedImage;
import java.net.URL;

public abstract class Item implements Pickable {
    protected String name;
    protected int amount;
    protected String imagePath;
    protected String soundPath;
    protected BufferedImage image;

    public Item(String name,int amount,String imagePath, String soundPath) {
        setName(name);
        setAmount(amount);
        this.imagePath = imagePath;
        this.soundPath = soundPath;
        loadImage();
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

    private void loadImage() {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // ค้นหาไฟล์ภาพจากโฟลเดอร์ res
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    this.image = ImageIO.read(imageUrl);
                } else {
                    System.out.println("⚠️ Warning: Image not found at " + imagePath);
                }
            } catch (Exception e) {
                System.out.println("❌ Error loading image: " + e.getMessage());
            }
        }
    }
    public void playSound() {
        if (soundPath != null && !soundPath.isEmpty()) {
            try {
                // ค้นหาไฟล์เสียงจากโฟลเดอร์ res
                URL soundUrl = getClass().getResource(soundPath);
                if (soundUrl != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start(); // สั่งเล่นเสียง
                } else {
                    System.out.println("⚠️ Warning: Sound not found at " + soundPath);
                }
            } catch (Exception e) {
                System.out.println("❌ Error playing sound: " + e.getMessage());
            }
        }
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
    public BufferedImage getImage() { return image; }
}
