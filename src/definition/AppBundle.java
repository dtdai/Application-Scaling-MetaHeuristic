package definition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class AppBundle {

    private ArrayList<Application> app;
    private ArrayList<Integer> tour;
    private int count;

    public AppBundle(int numApp, ArrayList<PhysicalMachine> pms, ArrayList<VirtualMachine> vms) {
        count = 0;
        app = new ArrayList<>();
        LoadApp(numApp);
        tour = InsertVM(app, pms, vms);
        AutoScaling();
    }

    private void LoadApp(int numApp) {
        for (int i = 0; i < numApp; i++) {
            Application a = new Application();
            app.add(a);
        }
        ImportFile(app, numApp);
    }

    private ArrayList<Integer> InsertVM(ArrayList<Application> app, ArrayList<PhysicalMachine> pms, ArrayList<VirtualMachine> vms) {
        int pmindex = 0, vmindex = 0;
        ArrayList<Integer> tour = new ArrayList<>();
        for (int i = 0; i < app.size(); i++) {
            for (int t = 0; t < 3; t++) {
                for (Integer integer : app.get(i).getTier(t)) {
                    Boolean flag = false;
                    while (!flag) {
                        if (pms.get(pmindex).CheckAvailable(vms.get(vmindex))) {
                            pms.get(pmindex).Allocation(vms.get(vmindex));
                            tour.add(pmindex + 1);
                            vmindex++;
                            flag = true;
                        } else {
                            pmindex++;
                        }
                    }
                }
            }
        }
        return tour;
    }
    /**
     * Auto-Scaling Method
     * Rate Adding:
     *    5% -> +3
     *   15% -> +2
     *   30% -> +1
     * Rate Subtract: (not available now)
     */
    private void AutoScaling() {
        for (int i = 0; i < app.size(); i++) {
            for (int t = 0; t < 3; t++) {
                double r = RandDouble(0.0, 1.0);
                
                if (r >= 0.95) {
                    addApp(i, t, 3);
                }
                else if (r >= 0.8) {
                    addApp(i, t, 2);
                }
                else if (r >= 0.5) {
                    addApp(i, t, 1);
                }
            }
        }
    }
       
    private void addApp(int index, int tier, int num) {
        app.get(index).addNumVm(num);
        ArrayList<Integer> t = app.get(index).getTier(tier);
        for (int i = 0; i < num; i++) {
            t.add(count);
            count = count + 1;
        }
        app.get(index).setTier(tier, t);
    }

    private double RandDouble(double min, double max) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextDouble() * ((max - min) + min);
        }
        return result;
    }

    private void ImportFile(ArrayList<Application> app, int num) {
        try {
            FileReader fileReader = new FileReader("app.txt");

            ArrayList<String> lines;
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            for (int i = 0; i < num; i++) {
                int c = 0;
                String[] values = lines.get(i + 1).trim().split("\\s+");
                for (String value : values) {
                    ArrayList<Integer> val = new ArrayList<>();
                    for (int j = 0; j < Integer.parseInt(value); j++) {
                        val.add(count);
                        count = count + 1;
                        c = c + 1;
                    }
                    app.get(i).setTier(val);
                }
                app.get(i).setNumVm(c);
            }

        } catch (IOException e) {
            System.err.println("An error occured: " + e.getMessage());
        }
    }

    public ArrayList<Application> getApp() {
        return app;
    }

    public void setApp(ArrayList<Application> app) {
        this.app = app;
    }

    public ArrayList<Integer> getTour() {
        return tour;
    }

    public void setTour(ArrayList<Integer> tour) {
        this.tour = tour;
    }

    public int getCount() {
        return count;
    }
}
