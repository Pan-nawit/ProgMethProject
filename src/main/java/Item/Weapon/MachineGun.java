package Item.Weapon;

public class MachineGun extends Gun {
    public MachineGun(){
        super("MachineGun",10,1, "Images/Gun/Machinegun.png", "Images/Gun/Machinegun.png",100);
    }
    @Override
    public double getRecoilAmount() {
        return 1.5; // ปืนกลยิงรัว ค่าเป้าบาน (Recoil) จะพุ่งขึ้นนัดละ 1.5
    }
    @Override
    public void playGunSound() {
        playSound();
    }
}