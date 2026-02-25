package enemy;

public abstract class basenemy implements Attackable{
    private int HP;
    private int SPEED;
    private int STRENGTH;
    public basenemy(int HP, int SPEED, int STRENGTH) {
        setHP(HP);
        setSPEED(SPEED);
        setSTRENGTH(STRENGTH);
    }
    public basenemy(){
        setHP(1);
        setSPEED(1);
        setSTRENGTH(1);
    }
    public boolean isdead(){
        if(getHP() <= 0){
            return true;
        }else {
            return false;
        }
    }
    public int getHP() {
        return HP;
    }
    public void setHP(int HP) {
        this.HP = HP;
    }
    public int getSPEED() {
        return SPEED;
    }
    public void setSPEED(int SPEED) {
        this.SPEED = SPEED;
    }
    public int getSTRENGTH() {
        return STRENGTH;
    }
    public void setSTRENGTH(int STRENGTH) {
        this.STRENGTH = STRENGTH;
    }
}
