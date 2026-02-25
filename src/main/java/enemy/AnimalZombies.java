package enemy;

public class AnimalZombies extends basenemy{
    public AnimalZombies(){
        super();
    }

    @Override
    public int Attack(int player) {
        player = player - getSTRENGTH();

        return player;
    }
}
