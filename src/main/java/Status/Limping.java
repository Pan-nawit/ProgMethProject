package Status;

import Player.Player;

public class Limping extends Status{
    public Limping() {
        super("Limping");
    }
    @Override
    public void apply(Player player) {
        player.setSpeed(player.getSpeed()/2); // ลดสปีดทันที
    }
    @Override
    public void undo(Player player) {
        player.setSpeed(player.getDefaultSpeed());
    }
}