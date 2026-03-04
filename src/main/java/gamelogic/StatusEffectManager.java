package gamelogic;

public class StatusEffectManager {

    public void applyEffect(Player player, String effect) {

        if (effect == null) return;

        switch (effect.toLowerCase()) {

            case "bleed":
                player.applyBleed(3);
                break;

            case "slow":
                player.applySlow(3);
                break;

            default:
                break;
        }
    }
}