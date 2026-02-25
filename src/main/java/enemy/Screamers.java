package enemy;

public class Screamers extends basenemy{
    public Screamers(){
        super();
    }

    @Override
    public int Attack(int player) {
        player = player - getSTRENGTH();

        return player;
    }
}
