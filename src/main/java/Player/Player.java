package Player;
import Item.Item;
import Item.Weapon.Weapon;
import Sound.SoundManager;
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
    private int width = 32;
    private int height = 32;
    private char lastFacing = 'w';
    private double recoil = 0.0;
    private double maxRecoil = 10.0;
    private float mouseX = -1;
    private float mouseY = -1;

    private List<Status> statusList = new ArrayList<>();
    private List<Item> inventory = new ArrayList<>();
    private int selectedItemIndex = 0;

    public Player(){
        this.x = 384;
        this.y = 234;
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
                if (y < 0) y = 0;
            }
            case 's' -> {
                y += currentSpeed;
                if (y > 500 - height) y = 500 - height;
            }
            case 'a' -> {
                x -= currentSpeed;
                if (x < 0) x = 0;
            }
            case 'd' -> {
                x += currentSpeed;
                if (x > 800 - width) x = 800 - width;
            }
        }
    }
    public Weapon getEquippedWeapon() {
        if (!inventory.isEmpty() && selectedItemIndex < inventory.size()) {
            Item item = inventory.get(selectedItemIndex);
            if (item instanceof Weapon) {
                return (Weapon) item;
            }
        }
        return null;
    }

    public void removeItem(Item item) {
        inventory.remove(item);
        if (selectedItemIndex >= inventory.size()) {
            selectedItemIndex = Math.max(0, inventory.size() - 1);
        }
    }

    public void onAttacked(int damage, Status incomingStatus) {
        setHp(getHp() - damage);
        SoundManager.getInstance().playSFX("/Sound/Player/classic_hurt.wav");
        if (incomingStatus != null && !hasStatus(incomingStatus.getName()))
            addStatus(incomingStatus);
    }
    public void applyKnockback(int force) {
        switch (lastFacing) {
            case 'w' -> y += force;
            case 's' -> y -= force;
            case 'a' -> x += force;
            case 'd' -> x -= force;
        }
    }

    public void addRecoil(double amount) {
        recoil += amount;
        if (recoil > maxRecoil) recoil = maxRecoil;
    }
    public void recoverRecoil() {
        if (recoil > 0) {
            recoil -= 0.5;
            if (recoil < 0) recoil = 0;
        }
    }

    public void addItem(Item newItem) {
        for (Item existingItem : inventory) {
            if (existingItem.getName().equals(newItem.getName())) {
                existingItem.addAmount(newItem.getAmount());
                return;
            }
        }
        inventory.add(newItem);
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
    public List<Item> getInventory() { return inventory; }
    public List<Status> getStatusList() { return statusList; }
    public void useSelectedConsumable() {
        if (!inventory.isEmpty() && selectedItemIndex < inventory.size()) {
            Item item = inventory.get(selectedItemIndex);
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
    public void setSelectedItemIndex(int index) {
        if (index >= 0) {
            this.selectedItemIndex = index;
        }
    }
    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
    public void setMousePos(float x, float y) { this.mouseX = x; this.mouseY = y; }
    public double getMaxRecoil() { return maxRecoil; }

}