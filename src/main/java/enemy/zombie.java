package enemy;

public class zombie extends basenemy {
    public zombie(){
        super(1,1,1);
    }

    @Override
    public int Attack(int player) {
        player= player-getSTRENGTH();
        return player;
    }
}
