package Interface;

public interface Cooldownable {
    boolean isReady();
    void startCooldown();        // เริ่มนับคูลดาวน์
    long getCooldownMillis();
}
