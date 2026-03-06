package Status;

import Player.Player;

public abstract class Status {
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    private String name;
    public Status(String name){
        setName(name);
    }
    public abstract void apply(Player player); // สั่งลดค่า
    public abstract void undo(Player player);

    /** Called every frame from the game loop. Override for time-based effects. */
    public void tick(Player player) { }
}
