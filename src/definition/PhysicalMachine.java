package definition;

/**
 *
 * @author TrongDai
 */
public class PhysicalMachine extends Machine {
    private int acore;
    private int aram;
    private int adisk;
    private int ucore;
    private int uram;
    private int udisk;
    
    public PhysicalMachine(int core, int ram, int disk) {
        super(core, ram, disk);
        this.acore = core;
        this.aram = ram;
        this.adisk = disk;
        this.ucore = 0;
        this.uram = 0;
        this.udisk = 0;
    }
    
    public PhysicalMachine(PhysicalMachine pm) {
        super(pm.getCore(), pm.getRam(), pm.getDisk());
        this.acore = pm.getCore();
        this.aram = pm.getRam();
        this.adisk = pm.getDisk();
    }

    public int getAvailable_core() {
        return acore;
    }

    public int getAvailable_ram() {
        return aram;
    }

    public int getAvailable_disk() {
        return adisk;
    }

    public int getUcore() {
        return ucore;
    }

    public int getUram() {
        return uram;
    }

    public int getUdisk() {
        return udisk;
    }
    
    public void Allocation(VirtualMachine vm) {
        acore = acore - vm.getCore();
        aram = aram - vm.getRam();
        adisk = adisk - vm.getDisk();
        ucore = ucore + vm.getCore();
        uram = uram + vm.getRam();
        udisk = udisk + vm.getDisk();
    }
    
    public Boolean CheckAvailable(VirtualMachine vm) {
        return (acore - vm.getCore() >= 0) && 
                (aram - vm.getRam() >= 0) &&
                (adisk - vm.getDisk() >= 0);
    }
    
    public Double performance() {
        return ((1.0 * acore) / (1.0 * this.getCore()) 
                + (1.0 * aram) / (1.0 * this.getRam()) 
                + (1.0 * adisk) / (1.0 * this.getDisk())) / 3;
    }
    
    public Double Utilization() {
        return ((1.0 * ucore) / (1.0 * this.getCore()) 
                + (1.0 * uram) / (1.0 * this.getRam()) 
                + (1.0 * udisk) / (1.0 * this.getDisk())) / 3;
    }
    
    public Double CoreUtilize() {
        return 1 - (1.0 * acore) / (1.0 * this.getCore());
    }
    
    public Double RamUtilize() {
        return 1 - (1.0 * aram) / (1.0 * this.getRam());
    }
    
    public Double DiskUtilize() {
        return 1 - (1.0 * adisk) / (1.0 * this.getDisk());
    }
}
