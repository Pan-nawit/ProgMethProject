package Player;
import Item.Item;
import Item.Weapon.Weapon;
import Status.Status;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
public class Player {
    private int hp;
    private int speed;
    private int maxHp = 5;
    private int defaultSpeed = 3;

    private int x, y;
    private int width = 32;  // ความกว้างของตัวละคร
    private int height = 32;
    private char lastFacing = 'w';
    //
    private double recoil = 0.0;
    private double maxRecoil = 10.0;
    private float mouseX = -1; // cursor position in logical coords
    private float mouseY = -1;

    private List<Status> statusList = new ArrayList<>();
    private List<Item> inventory = new ArrayList<>();
    private int selectedItemIndex = 0; // เพิ่มเพื่อระบุว่าเลือกไอเทมชิ้นไหนใน inventory

    public Player(){
        this.x = 384;
        this.y = 234; // center of 500px play area
        setHp(maxHp);
        setSpeed(defaultSpeed);
    }

    public void move(char direction) {
        int currentSpeed = getSpeed();
        direction = Character.toLowerCase(direction);
        this.lastFacing=direction;
        switch (direction) {
            case 'w' -> {
                y -= currentSpeed;
                if (y < 0) y = 0; // ถ้าเกินขอบบน ให้ล็อกไว้ที่ 0
            }
            case 's' -> {
                y += currentSpeed;
                if (y > 500 - height) y = 500 - height; // play area height
            }
            case 'a' -> {
                x -= currentSpeed;
                if (x < 0) x = 0; // ถ้าเกินขอบซ้าย ให้ล็อกไว้ที่ 0
            }
            case 'd' -> {
                x += currentSpeed;
                if (x > 800 - width) x = 800 - width; // หักลบความกว้างของ Player ด้วย
            }
        }
    }
    // ดึงอาวุธที่กำลังถืออยู่ (สมมติว่าเป็นไอเทมที่เลือกอยู่ใน index ปัจจุบัน)
    public Weapon getEquippedWeapon() {
        if (!inventory.isEmpty() && selectedItemIndex < inventory.size()) {
            Item item = inventory.get(selectedItemIndex);
            if (item instanceof Weapon) {
                return (Weapon) item;
            }
        }
        return null;
    }

    // ลบไอเทมออกจากตัว (เรียกใช้เมื่อกระสุนหมด)
    public void removeItem(Item item) {
        inventory.remove(item);
        if (selectedItemIndex >= inventory.size()) {
            selectedItemIndex = Math.max(0, inventory.size() - 1);
        }
    }

    // เปลี่ยนอาวุธ (เผื่อคุณเพิ่มปุ่มเปลี่ยนปืนในอนาคต)
    public void selectNextItem() {
        if (!inventory.isEmpty()) {
            selectedItemIndex = (selectedItemIndex + 1) % inventory.size();
        }
    }
    public void onAttacked(int damage, Status incomingStatus) {
        setHp(getHp() - damage);
        if (incomingStatus != null) {
            if (!hasStatus(incomingStatus.getName())) {
                addStatus(incomingStatus);
            }
        }
    }
    public void applyKnockback(int force) {
        switch (lastFacing) {
            case 'w' -> y += force; // หันหน้าขึ้น (ยิงขึ้น) ตัวกระเด็นลง
            case 's' -> y -= force; // หันหน้าลง ตัวกระเด็นขึ้น
            case 'a' -> x += force; // หันซ้าย ตัวกระเด็นขวา
            case 'd' -> x -= force; // หันขวา ตัวกระเด็นซ้าย
        }
    }

    public void addRecoil(double amount) {
        double actual = amount * (0.5 + Math.random() * 0.5); // สุ่ม 50–100%
        recoil += actual;
        if (recoil > maxRecoil) recoil = maxRecoil;
    }
    public void recoverRecoil() {
        if (recoil > 0) {
            recoil -= 0.5; // ค่อยๆ ลดลงเรื่อยๆ
            if (recoil < 0) recoil = 0;
        }
    }

    public void addItem(Item newItem) {
        for (Item existingItem : inventory) {
            if (existingItem.getName().equals(newItem.getName())) {
                existingItem.addAmount(newItem.getAmount());
                return; // ถ้าเจอชื่อซ้ำ ให้บวกจำนวนแล้วจบการทำงานเลย
            }
        }
        inventory.add(newItem); // ถ้าไม่เคยมี ให้เพิ่มเป็นชิ้นใหม่
    }

    public void useItem(int index) {
        if (index >= 0 && index < inventory.size()) {
            Item item = inventory.get(index);
            item.use(this);
            if (item.isEmpty()) {
                inventory.remove(index); // ถ้ากระสุนหมด/ยาหมด ลบทิ้ง
            }
        }
    }

    public void addStatus(Status newStatus) {
        if (!hasStatus(newStatus.getName())) {
            newStatus.apply(this);
            statusList.add(newStatus);
        }
    }
    private boolean hasStatus(String statusName) {
        return statusList.stream().anyMatch(s -> s.getName().equals(statusName));
    }
    public void removeStatus(String statusName) {
        statusList.removeIf(s -> {
            if (s.getName().equals(statusName)) {
                s.undo(this);
                return true;
            }
            return false;
        });
    }
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    public int getHp() {return hp;}
    public void setHp(int hp) {
        this.hp = hp;
        if (this.hp > maxHp) this.hp = maxHp;
        if (this.hp < 0) this.hp = 0;
    }
    public int getSpeed() {
        return speed;
    }
    public int getDefaultSpeed() {
        return defaultSpeed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public int getX(){return x;}
    public int getY(){return y;}
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getRecoil() { return recoil; }
    public char getLastFacing() {return lastFacing;}
    public List<Item> getInventory() { return inventory; }
    public List<Status> getStatusList() { return statusList; }

    public void selectPrevItem() {
        if (!inventory.isEmpty()) {
            selectedItemIndex = (selectedItemIndex - 1 + inventory.size()) % inventory.size();
        }
    }

    public void useSelectedConsumable() {
        if (!inventory.isEmpty() && selectedItemIndex < inventory.size()) {
            Item item = inventory.get(selectedItemIndex);
            // only use if it's not a weapon (consumables)
            if (!(item instanceof Weapon)) {
                item.use(this);
                if (item.isEmpty()) {
                    inventory.remove(selectedItemIndex);
                    if (selectedItemIndex >= inventory.size())
                        selectedItemIndex = Math.max(0, inventory.size() - 1);
                }
            }
        }
    }

    public int getSelectedItemIndex() { return selectedItemIndex; }


    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public void setMousePos(float x, float y) { this.mouseX = x; this.mouseY = y; }
    public double getMaxRecoil() { return maxRecoil; }
}
