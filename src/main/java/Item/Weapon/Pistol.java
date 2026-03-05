package Item.Weapon;

public class Pistol extends Gun {
    public Pistol(){
        super("Pistol",5,1,"Images/Gun/Pistol.png","",1000);
    }
    @Override
    public double getRecoilAmount() {
        // ปืนพกยิงแล้วเป้าบานน้อยมาก (เช่น 0.3) ทำให้ยิงต่อเนื่องได้แม่นยำ
        return 0.3;
    }
    @Override
    public void playGunSound() {
        playSound();
    }
}
