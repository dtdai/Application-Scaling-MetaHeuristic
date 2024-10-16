package definition;

import java.util.ArrayList;

/**
 *
 * @author TrongDai
 */
public class Application {
    private ArrayList<ArrayList<Integer>> tier;
    private int numVm;
    private ArrayList<Integer> lamb;
    
    public Application() {
        this.tier = new ArrayList<>();
        this.lamb = new ArrayList<>();
    }
    
    public void addTier(ArrayList<Integer> list) {
        this.tier.add(list);
    }

    public void setNumVm(int numVm) {
        this.numVm = numVm;
    }

    public int getNumVm() {
        return numVm;
    }

    public ArrayList<Integer> getTier(int index) {
        return tier.isEmpty() ? null : tier.get(index);
    }
    
    public ArrayList<Integer> getLastTier() {
        return tier.isEmpty() ? null : tier.getLast();
    }
    
    public void setTier(ArrayList<Integer> arr) {
        this.tier.add(arr);
    }

    public void setLamb(int lambda) {
        this.lamb.add(lambda);
    }

    public ArrayList<Integer> getLamb() {
        return lamb;
    }
}
