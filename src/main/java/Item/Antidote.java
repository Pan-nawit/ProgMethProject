package Item;
import java.util.List;
import java.util.Random;
import Player.Player;
import Status.Status;

public class Antidote extends HealingItems{
    private Random random = new Random();
    public Antidote(){
        super("Antidote",1);
    }
    @Override
    public void Healing(){
        List<Status>currentStatus = Player.getStatusList();
        if(!currentStatus.isEmpty()){
            int randomIndex = random.nextInt(currentStatus.size());
            String removed = currentStatus.remove(randomIndex);
            currentStatus.remove(randomIndex);
            System.out.println("Cured: "+removed);
        }
        else{
            System.out.println("No status to cure");
        }
    }
}
