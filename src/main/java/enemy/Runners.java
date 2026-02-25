package enemy;

public class Runners extends basenemy{
    public Runners() {
        super(1, 2, 1);
    }

    @Override
    public int Attack(int player) {
        player= player - getSTRENGTH();
        return player;
    }
}
