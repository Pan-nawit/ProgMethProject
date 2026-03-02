package Item;
import java.util.List;
import java.util.Random;
import Player.Player;
import Status.Status;

public class Antidote extends HealingItems{
    private Random random = new Random();
    public Antidote(){
        super("Antidote",1,//,//);
    }
    @Override
    public void Healing(Player player){
        List<Status>currentStatus = player.getStatusList();
        if(!currentStatus.isEmpty()){
            int randomIndex = random.nextInt(currentStatus.size());
            Status removed = currentStatus.remove(randomIndex);
            currentStatus.remove(randomIndex);
            System.out.println("Cured: "+removed.getName());
        }
        else{
            System.out.println("No status to cure");
        }
    }
}
