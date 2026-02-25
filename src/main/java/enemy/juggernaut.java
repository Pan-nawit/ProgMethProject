package enemy;

public class juggernaut extends basenemy {
    public juggernaut(){
        super(5,1,1);
    }

    @Override
    public int Attack(int player) {
        player = player - getSTRENGTH();
        return player;
    }
}
