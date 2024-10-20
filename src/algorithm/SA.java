package algorithm;

import definition.AppBundle;
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
    private ArrayList<Integer> bestTour;
    private double bestValue = Double.MIN_EXPONENT;
    private Object bestIndex;
    private AppBundle appBundle;

    public SA(int numApps, ArrayList<Machine> machine, double temp, double coolRate) {
        this.pms = new ArrayList<>();
        this.vms = new ArrayList<>();
        for (Machine i : machine) {
            switch (i) {
                case PhysicalMachine p ->
                    this.pms.add(p);
                case VirtualMachine v ->
                    this.vms.add(v);
                default -> {
                }
            }
        }
        this.numPM = pms.size();
        this.numVM = vms.size();
        this.temperature = temp;
        this.coolingRate = coolRate;

        appBundle = new AppBundle(numApps, pms, vms);
    }

    public void solve() {
        ArrayList<Annealing> anealings = new ArrayList<>();
        while (temperature > 20.0) {
            Annealing a = new Annealing();
            ArrayList<Integer> tour = new ArrayList<>();

            while (tour.size() != appBundle.getCount() - appBundle.getTour().size()) {
                tour = GenerateTour();
            }

            a.tour.addAll(appBundle.getTour());
            a.tour.addAll(tour);
            a.model = GetModel(a, tour);
            a.value = a.model.BenefitFunction();

            if (Double.compare(a.value, bestValue) > 0) {
                bestTour = a.tour;
                bestValue = a.value;
                bestIndex = a.model;
            }
            
            anealings.add(a);
            temperature = temperature * (1 - coolingRate);
        }
        
        System.out.println("Best Solution using SA is: " + bestTour.toString());
        System.out.println("Best value is " + bestValue);
    }

    private ArrayList<Integer> GenerateTour() {
        ArrayList<Integer> tour = new ArrayList<>();
        ArrayList<PhysicalMachine> hosts = CloneHost();

        for (int i = appBundle.getTour().size(); i < appBundle.getCount(); i++) {

            int randPM = RandInteger(0, numPM - 1);
            randPM = CheckAvailable(hosts, randPM, i, 1);
            if (randPM == -1) {
                break;
            }

            Allocation(hosts, randPM, i);
            tour.add(randPM + 1);
        }

        return tour;
    }

    private ArrayList<PhysicalMachine> CloneHost() {
        ArrayList<PhysicalMachine> host = new ArrayList<>();
        for (Iterator<PhysicalMachine> it = pms.iterator(); it.hasNext();) {
            PhysicalMachine pm = new PhysicalMachine(it.next());
            host.add(pm);
        }
        return host;
    }
    
        private GameModel GetModel(Annealing a, ArrayList<Integer> tour) {
        ArrayList<PhysicalMachine> hosts = CloneHost();
        for (int i = 0; i < tour.size(); i++) {
            Allocation(hosts, tour.get(i) - 1, i);
        }
        return new GameModel(appBundle.getApp(), hosts, vms, a.tour);
    }

    private int RandInteger(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    private int CheckAvailable(ArrayList<PhysicalMachine> host, int indexPM, int indexVM, int runtime) {
        while (!host.get(indexPM).CheckAvailable(vms.get(indexVM))) {
            indexPM = RandInteger(0, numPM - 1);
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

    private class Annealing {

        ArrayList<Integer> tour;
        double value;
        GameModel model;

        Annealing() {
            tour = new ArrayList<>();
            value = 0.0;
            model = null;
        }
    }
}
