package Status;

import Player.Player;

public class Bleeding extends Status {
    private long lastTickTime = 0;
    private static final long TICK_INTERVAL_MS = 10000;

    public Bleeding() {
        super("Bleeding");
    }

    @Override
    public void apply(Player player) {
        lastTickTime = System.currentTimeMillis();
    }

    @Override
    public void tick(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastTickTime >= TICK_INTERVAL_MS) {
            if (player.getHp() > 0) {
                player.setHp(player.getHp() - 1);
                System.out.println("[Status] Bleeding: HP -1. Current HP: " + player.getHp());
            }
            lastTickTime = now;
        }
    }

    @Override
    public void undo(Player player) {
        System.out.println("[Status] Bleeding stopped.");
    }
}