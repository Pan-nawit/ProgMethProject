package Status;

import Player.Player;
import java.util.Timer;
import java.util.TimerTask;

public class Bleeding extends Status {
    private Timer timer;
    public Bleeding(){
        super("Bleeding");
    }
    @Override
    public void apply(Player player) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (player.getHp() > 0) {
                    player.setHp(player.getHp() - 1);
                    System.out.println("[Status] Bleeding: Player HP reduced by 1. Current HP: " + player.getHp());
                } else {
                    stopEffect(); // ถ้าตายแล้วให้หยุดทำงาน
                }
            }
        }, 10000, 10000); // (เริ่มทำงานหลังผ่านไป 10วินาที, ทำซ้ำทุก 10วินาที)
    }

    @Override
    public void undo(Player player) {
        stopEffect();
        System.out.println("[Status] Bleeding stopped.");
    }

    private void stopEffect() {
        if (timer != null) {
            timer.cancel(); // สั่งยกเลิกการทำงานของ Timer
            timer.purge();
        }
    }
}
