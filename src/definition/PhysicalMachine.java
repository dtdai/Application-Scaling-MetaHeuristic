package definition;

/**
 *
 * @author TrongDai
 */
public class PhysicalMachine extends Machine {
    private int available_core;
    private int available_ram;
    private int available_disk;
    private int max_core = 512;
    private int max_ram = 1024;
    private int max_disk = 16384;
    private int slot;

    public int getMax_core() {
        return max_core;
    }

    public int getMax_ram() {
        return max_ram;
    }

    public int getMax_disk() {
        return max_disk;
    }
    
    public PhysicalMachine(int core, int ram, int disk) {
        super(core, ram, disk);
        this.available_core = core;
        this.available_ram = ram;
        this.available_disk = disk;
        this.slot = 0;
    }
    
    public PhysicalMachine(PhysicalMachine pm) {
        super(pm.getCore(), pm.getRam(), pm.getDisk());
        this.available_core = pm.getCore();
        this.available_ram = pm.getRam();
        this.available_disk = pm.getDisk();
    }

    public int getAvailable_core() {
        return available_core;
    }

    public int getAvailable_ram() {
        return available_ram;
    }

    public int getAvailable_disk() {
        return available_disk;
    }
    
    public void Allocation(VirtualMachine vm) {
        available_core -= vm.getCore();
        available_ram -= vm.getRam();
        available_disk -= vm.getDisk();
        slot += 1;
    }
    
    public Boolean CheckAvailable(VirtualMachine vm) {
        return (available_core - vm.getCore() >= 0) && 
                (available_ram - vm.getRam() >= 0) &&
                (available_disk - vm.getDisk() >= 0) && (slot < 3);
    }
    
    public Double performance() {
        return ((1.0 * available_core) / (1.0 * this.getCore()) 
                + (1.0 * available_ram) / (1.0 * this.getRam()) 
                + (1.0 * available_disk) / (1.0 * this.getDisk())) / 3;
    }
    
    public Double Utilization() {
        return ((1.0 * (this.getCore() - available_core)) / (1.0 * this.getCore()) 
                + (1.0 * (this.getRam() - available_ram)) / (1.0 * this.getRam()) 
                + (1.0 * (this.getDisk() - available_disk)) / (1.0 * this.getDisk())) / 3;
    }
    
    public Double CoreUtilize() {
        return 1 - (1.0 * available_core) / (1.0 * this.getCore());
    }
    
    public Double RamUtilize() {
        return 1 - (1.0 * available_ram) / (1.0 * this.getRam());
    }
    
    public Double DiskUtilize() {
        return 1 - (1.0 * available_disk) / (1.0 * this.getDisk());
    }
}
