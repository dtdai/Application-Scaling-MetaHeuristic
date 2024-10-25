package algorithm;

import definition.Application;
import definition.VirtualMachine;
import definition.PhysicalMachine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class GameModel {

    private final ArrayList<PhysicalMachine> pms;
    private final ArrayList<VirtualMachine> vms;
    private final ArrayList<Application> app;
    private final ArrayList<Integer> tour;
    private final int u; // Amount of vm
    private final int a; // Amount of Application
    private final int m; // Amount of pm
    private final int tier = 3;
    private final double q = 1000.0;
    private final double Z = 120.0;
    private double[][] R;
    private double[] h;
    private double[] ome;
    private double h_t;
    private double om;
    private double psi;
    private double[] nguy;
    private double[] Lamb;
    private int gamma;
    private double Beta;
    private double[] L;
    private ArrayList<ArrayList<Integer>> PA = new ArrayList<>();
    private ArrayList<Double> Pa;
    private ArrayList<Double> Wt;
    private ArrayList<Double> AC;
    private final double alpha = 0.5;
    private final double beta = 0.5;

    public GameModel(ArrayList<Application> app, ArrayList<PhysicalMachine> pms, ArrayList<VirtualMachine> vms, ArrayList<Integer> tour) {
        this.app = app;
        this.pms = pms;
        this.vms = vms;
        this.tour = tour;
        this.m = pms.size();
        this.u = vms.size();
        this.a = app.size();
        PA = new ArrayList<>();
        Pa = new ArrayList<>();
        Wt = new ArrayList<>();
        AC = new ArrayList<>();

        IntialValues();
        Efficiency();
        LoadWaste();
        Processing();
        CPUCosts();
    }

    private int RandInteger(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    private void IntialValues() {
        double[] Nguy = new double[u];
        for (int i = 0; i < u; i++) {
            Nguy[i] = q * vms.get(i).getCore() / Z;
        }
        nguy = Nguy;

        for (int i = 0; i < a; i++) {
            for (int j = 0; j < app.get(i).getNumVm(); j++) {
                app.get(i).setLamb(RandInteger(5, 10)); // speed from outside to nodes
//                app.get(i).setLamb(8);
            }
        }

        for (int i = 0; i <= m; i++) {
            PA.add(new ArrayList<>(Collections.nCopies(a, 0)));
            Pa.add(0.0);
        }

        for (int i = 0; i < a; i++) {
            for (int j = 0; j < tier; j++) {
                ArrayList<Integer> v = app.get(i).getTier(j);
                for (Integer value : v) {
                    int c = PA.get(tour.get(value)).get(i) + 1;
                    PA.get(tour.get(value)).set(i, c);
                }
            }
        }
    }

    private void Efficiency() {
        h = new double[m];
        ome = new double[m];
        for (int i = 0; i < m; i++) {
            h[i] = 0.3 * ((pms.get(i).getUcore() * 1.0) / (pms.get(i).getCore() * 1.0))
                    + 0.3 * ((pms.get(i).getUram() * 1.0) / (pms.get(i).getRam() * 1.0))
                    + 0.4 * ((pms.get(i).getUdisk() * 1.0) / (pms.get(i).getDisk() * 1.0));
            h_t = h_t + h[i];
            ome[i] = 1 - h[i];
        }
        h_t = h_t / m;
    }

    private void LoadWaste() {
        double sum1 = 0.0, sum2 = 0.0;
        for (int i = 0; i < m; i++) {
            sum1 = sum1 + Math.pow(h[i] - h_t, 2);
            sum2 = sum2 + ome[i];
        }
        psi = sum1 / (1.0 * m);
        om = sum2 / (1.0 * m);
    }

    private void RChangeMatrix(int k, int s) {
        R = new double[s][s];
        ArrayList<Integer> lb, rb;
        lb = app.get(k).getTier(0);
        for (int i = 0; i < lb.size(); i++) {
            R[0][lb.get(i)] = 1.0 / lb.size();
        }

        for (int t = 1; t < tier; t++) {
            rb = app.get(k).getTier(t);
            int ac = lb.size() * rb.size();
            for (int i = 0; i < lb.size(); i++) {
                for (int j = 0; j < rb.size(); j++) {
                    R[lb.get(i)][rb.get(j)] = 1.0 / ac;
                }
            }
            lb = rb;
        }
    }

    private double Kahan_Summation(double[] nums) {
        double sum = 0.0;
        double c = 0.0;
        for (double x : nums) {
            double y = x - c;
            double t = sum + y;
            c = (t - sum) - y;
            sum = t;
        }
        return sum;
    }

    private void Processing() {
        for (int k = 0; k < a; k++) {
            gamma = RandInteger(20, 30); // Num request per sec
//            gamma = 25;
            Beta = gamma;
            ArrayList<Integer> lamb = app.get(k).getLamb();
            RChangeMatrix(k, tour.size() + 1);
            L = new double[app.get(k).getNumVm()];
            Lamb = new double[app.get(k).getNumVm() + 1];
            Lamb[0] = gamma;

            for (int i = 0; i < app.get(k).getNumVm(); i++) {
                double sum = 0.0;
                for (int r = 0; r < app.get(k).getNumVm() + 1; r++) {
                    sum = sum + R[i + 1][r] * Lamb[i];
                }
                Lamb[i + 1] = lamb.get(i) + sum;
                double p = Lamb[i + 1] / nguy[i];
                L[i] = p / (1 - p) == Double.POSITIVE_INFINITY ? 0 : p / (1 - p);
                Beta += lamb.get(i);
            }

            Wt.add(Kahan_Summation(L) / Beta);
        }
    }

    private void CPUCosts() {
        double result;
        for (int j = 0; j <= m; j++) {
            result = 0;
            for (int i = 0; i < tour.size(); i++) {
                if (tour.get(i) == j) {
                    result = result + vms.get(i).getCore();
                }
            }
            result = alpha * result + beta;
            Pa.add(j, result);
        }

        for (int k = 0; k < a; k++) {
            result = 0.0;
            for (int i = 1; i <= m; i++) {
                result = result + Pa.get(i) * PA.get(i).get(k);
            }
            AC.add(result);
        }
    }

    private ArrayList<Double> Normalize(ArrayList<Double> arr) {
        ArrayList<Double> Res = new ArrayList<>();
        double max = Collections.max(arr);
        double min = Collections.min(arr);

        for (int i = 0; i < arr.size(); i++) {
            Res.add((arr.get(i) - min) / (max - min));
        }
        return Res;
    }

    public double BenefitFunction() {
        double val1 = 0.0, val2 = 0.0, result;
        ArrayList<Double> waittime = Normalize(Wt);
        ArrayList<Double> appcost = Normalize(AC);
        for (Double i : waittime) {
            val1 = val1 + i;
        }
        for (Double i : appcost) {
            val2 = val2 + i;
        }
        result = 1 / (val1 + val2);
        return result;
    }
    
    public void Evaluate() {
        ArrayList<Double> waittime = Normalize(Wt);
        ArrayList<Double> appcost = Normalize(AC);
        System.out.println("");
        for (int i = 0; i < a; i++) {
            System.out.print("App " + (i + 1) + ":\t");
            System.out.print("Response Time: " + waittime.get(i));
            System.out.println("\t, App Cost:" + appcost.get(i));
        }
        
    }
}
