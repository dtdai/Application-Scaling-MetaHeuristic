package execute;

import algorithm.GameModel;
import definition.AppBundle;
import definition.Machine;
import definition.PhysicalMachine;
import definition.VirtualMachine;
import java.util.ArrayList;

/**
 *
 * @author TrongDai
 */
public class EvaluateResult {
    public EvaluateResult(ArrayList<Machine> machine, int numApp, ArrayList<Integer> tour) {
        ArrayList<PhysicalMachine> pms = new ArrayList<>();
        ArrayList<VirtualMachine> vms = new ArrayList<>();
        for (Machine i : machine) {
            switch (i) {
                case PhysicalMachine p -> pms.add(p);
                case VirtualMachine v -> vms.add(v);
                default -> {
                }
            }
        }
        
        AppBundle appBundle = new AppBundle(numApp, pms, vms);
        
        for (int i = 0; i < tour.size(); i++) {
            pms.get(tour.get(i) - 1).Allocation(vms.get(i));
        }
        
        ArrayList<Integer> t = new ArrayList<>();
        t.addAll(appBundle.getTour());
        t.addAll(tour);
        GameModel model = new GameModel(appBundle.getApp(), pms, vms, t);
        
        model.Evaluate();        
    }
}
