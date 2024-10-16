package execute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class Generate {

    public static void main(String[] args) {
        String pmpath = "pm.txt";       // Gen pm
        String vmpath = "vm.txt";       // Gen vm
        String apppath = "app.txt";     // Gen app
        int pm = 500, vm = 2000, app = 100;
        int[] gpcpu = {64, 128, 256, 512};
        int[] gpram = {128, 256, 512, 1024};
        int[] gpdisk = {2048, 4096, 8192, 16384};
        int[] gvcpu = {2, 4, 8, 16};
        int[] gvram = {4, 8, 16, 32};
        int[] gvdisk = {100, 200, 400, 1000};
        int[] gapt = {1, 2, 2, 2, 3, 3, 3, 4, 4, 5};
        ArrayList<String> pmarray = new ArrayList<>();  // Store gen pm
        ArrayList<String> vmarray = new ArrayList<>();  // Store gen vm
        ArrayList<String> apparray = new ArrayList<>(); // Store gen app
        String sp = " ";
        
        pmarray.add("Pm: (cpu - ram - disk)");
        vmarray.add("Vm: (cpu - ram - disk)");
        apparray.add("App: (tier1 - tier2 - tier3)");
        
        for (int i = 0; i < pm; i++) {
            String m = "";
            m = m + gpcpu[RandInteger(0, 3)] + sp 
                    + gpram[RandInteger(0, 3)] + sp 
                    + gpdisk[RandInteger(0, 3)];
            pmarray.add(m);
        }
        
        for (int i = 0; i < vm; i++) {
            String m = "";
            int rd = RandInteger(0, 3);
            m = m + gvcpu[rd] + sp + gvram[rd] + sp + gvdisk[rd];
            vmarray.add(m);
        }
        
        for (int i = 0; i < app; i++) {
            String m = "";
            int t1 = RandInteger(0, 9);
            int t2 = RandInteger(0, 9);
            int t3 = RandInteger(0, 9);
            m = m + gapt[t1] + sp + gapt[t2] + sp + gapt[t3];
            apparray.add(m);
        }
        
        WriteFile(pmarray, pmpath);
        WriteFile(vmarray, vmpath);
        WriteFile(apparray, apppath);
        
//        System.out.println(pmarray.toString());
//        System.out.println(vmarray.toString());
//        System.out.println(apparray.toString());
    }

    private static int RandInteger(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }
    
    private static void WriteFile(ArrayList<String> array, String path) {
        try {
            FileWriter writer = new FileWriter(path);

            try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                for (String value : array) {
                    bufferedWriter.write(value);
                    bufferedWriter.newLine();
                }
            }

            System.out.println("Successfully.");
        } catch (IOException e) {
            System.err.println("An error occured: " + e.getMessage());
        }
    }
}
