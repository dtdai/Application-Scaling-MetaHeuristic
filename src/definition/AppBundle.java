package definition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author TrongDai
 */
public class AppBundle {

    private void LoadApp(int numApp) throws IOException {
        ArrayList<Application> app = new ArrayList<>();
        for (int i = 0; i < numApp; i++) {
            Application a = new Application();
            app.add(a);
        }
        ImportFile(app, numApp);
        
    }

    private void InsertVM(ArrayList<Application> app, ArrayList<PhysicalMachine> pms, ArrayList<VirtualMachine> vms) {
        int pmindex = 0, vmindex = 0;
        ArrayList<Integer> tour = new ArrayList<>();
        for (int i = 0; i < app.size(); i++) {
            for (int t = 0; t < 3; t++) {
                for (int j = 0; j < 3; j++) {
                    int n = app.get(i).getTier(t).get(j);
                    while (n > 0) {
                        if (pms.get(pmindex).CheckAvailable(vms.get(vmindex))) {
                            pms.get(pmindex).Allocation(vms.get(vmindex));
                            tour.add(pmindex + 1);
                            vmindex++;
                            n--;
                        } else {
                            pmindex++;
                        }
                    }
                }
            }
        }
    }

    private void ImportFile(ArrayList<Application> app, int num) throws IOException {
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

            for (int i = 1; i <= num; i++) {
                String[] values = lines.get(i).trim().split("\\s+");
                int c = 1;
                for (String value : values) {
                    ArrayList<Integer> val = new ArrayList<>();
                    for (int j = 0; j < Integer.parseInt(value); j++) {
                        val.add(c);
                        c = c + 1;
                    }
                    app.get(i).setTier(val);
                }
                app.get(i).setNumVm(c - 1);
            }

        } catch (IOException e) {
            System.err.println("An error occured: " + e.getMessage());
        }
    }
}
