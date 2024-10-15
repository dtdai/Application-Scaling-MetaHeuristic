package algorithm;

import definition.Machine;
import definition.VirtualMachine;
import definition.PhysicalMachine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class SA {

    private final ArrayList<PhysicalMachine> pms;
    private final ArrayList<VirtualMachine> vms;
    private final int numPM;
    private final int numVM;
    private double temperature;
    private final double coolingRate;
    private ArrayList<Integer> tour, besttour;
    private double bestFairUtil = 1e9;
    private GameModel gamemodel;

    public SA(ArrayList<Machine> machine, double temp, double coolRate) {
        this.pms = new ArrayList<>();
        this.vms = new ArrayList<>();
        for (Machine i : machine) {
            if (i instanceof PhysicalMachine) {
                this.pms.add((PhysicalMachine) i);
            } else if (i instanceof VirtualMachine) {
                this.vms.add((VirtualMachine) i);
            }
        }
        this.numPM = pms.size();
        this.numVM = vms.size();
        this.temperature = temp;
        this.coolingRate = coolRate;
    }

    public void solve() {
        besttour = new ArrayList<>();
        while (temperature > 20.0) {
            tour = new ArrayList<>();
            ArrayList<PhysicalMachine> hosts = CloneHost();

            // Generate tour
            for (int i = 0; i < numVM; i++) {
                int randPM = RandomIntMinMax(0, numPM - 1);
                randPM = CheckAvailable(hosts, randPM, i, 1);
                if (randPM == -1) {
                    break;
                }
                Allocation(hosts, randPM, i);
                tour.add(randPM + 1);
            }

//            System.out.println(tour.toString());
            if (tour.size() == numVM) {
                GameModel gameModel = new GameModel(pms, vms, tour);
                double F = gameModel.BenefitFunction();
//                System.out.println(F);
                if (Double.compare(F, bestFairUtil) < 0) {
                    besttour = tour;
                    bestFairUtil = F;
                    gamemodel = gameModel;
                }
            }
            temperature = temperature * (1 - coolingRate);
        }
        System.out.println(bestFairUtil);
        System.out.println("Best Solution using SA is: " + besttour.toString());
        System.out.println("Best value is " + bestFairUtil);
    }

    private ArrayList<PhysicalMachine> CloneHost() {
        ArrayList<PhysicalMachine> host = new ArrayList<>();
        for (Iterator<PhysicalMachine> it = pms.iterator(); it.hasNext();) {
            PhysicalMachine pm = new PhysicalMachine(it.next());
            host.add(pm);
        }
        return host;
    }

    private int RandomIntMinMax(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    private int CheckAvailable(ArrayList<PhysicalMachine> host, int indexPM, int indexVM, int runtime) {
        while (!host.get(indexPM).CheckAvailable(vms.get(indexVM))) {
            indexPM = RandomIntMinMax(0, numPM - 1);
            if (runtime > numPM * 10) {
                return -1;
            }
            runtime++;
        }
        return indexPM;
    }

    private void Allocation(ArrayList<PhysicalMachine> host, int indexPM, int indexVM) {
        host.get(indexPM).Allocation(vms.get(indexVM));
    }
}
