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
    private final ArrayList<Application> App = new ArrayList<>();
    private final int u; // Amount of vm
    private final int a = 10; //Amount of Application
    private final int m;
    private final int tier = 3;
    private final double q = 1000.0;
    private final double Z = 120.0;
    private double[][] R;
    private double[] h;
    private double[] ome;
    private double om;
    private double h_t;
    private double psi;
    private double[] nguy;
    private double[] Lamb;
    private int gamma;
    private double Beta;
    private double[] L;
    private double L_s = 0.0;
    private ArrayList<ArrayList<Integer>> Sol = new ArrayList<>();
    private ArrayList<Double> Wt = new ArrayList<>();
    private ArrayList<Double> Pa = new ArrayList<>();
    private ArrayList<Double> AC = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> PA = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> Soln = new ArrayList<>();
    private final double alpha = 0.5;
    private final double beta = 0.5;

    public GameModel(ArrayList<PhysicalMachine> pms, ArrayList<VirtualMachine> vms, ArrayList<Integer> tour) {
        this.pms = pms;
        this.vms = vms;
        this.m = pms.size();
        this.u = vms.size();

        ArrayList<Integer> cTour = new ArrayList<>();
        for (Integer tour1 : tour) {
            cTour.add(tour1);
        }
        for (int i = 0; i < tier; i++) {
            ArrayList<Integer> s = new ArrayList<>();
            for (int j = 0; j < m; j++) {
                s.add(cTour.getFirst());
                cTour.removeFirst();
            }
            Soln.add(s);
        }

        genApp();
        IntialValues();
        Efficiency();
        LoadWaste();
        Processing();
        CPUCosts();
    }

    private void genApp() {
        for (int i = 0; i < a; i++) {
            Application app = new Application();
            App.add(app);
        }

        int nv = u / a;
        int rv = u % a;
        int mv = u;

        for (int i = 0; i < a; i++) {
            if (mv <= nv) {
                App.get(i).setNumVm(mv);
                mv = mv - App.get(i).getNumVm();
            } else {
                App.get(i).setNumVm(nv);
                for (int j = 0; j < tier; j++) {
                    double rand = RandDouble(0.0, 1.0);
                    if (rand <= 0.25 && rv > -1) {
                        App.get(i).setNumVm(App.get(i).getNumVm() + 1);
                        rv -= 1;
                    }

                    if (rand >= 0.75 && rv < 1) {
                        App.get(i).setNumVm(App.get(i).getNumVm() - 1);
                        rv += 1;
                    }
                }
                mv = mv - App.get(i).getNumVm();
            }
        }
        if (mv > 0) {
            App.get(App.size() - 1).setNumVm(App.get(App.size() - 1).getNumVm() + mv);
        }

        ArrayList<Integer> tempArr = new ArrayList<>();
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < App.get(i).getNumVm() - tier; j++) {
                tempArr.add(i);
            }
        }

        for (int i = 0; i < tier; i++) {
            ArrayList<Integer> S = new ArrayList<>();
            for (int j = 0; j < u / tier; j++) {
                if (j < a) {
                    S.add(j);
                } else {
                    int rand = RandInteger(0, tempArr.size() - 1);
                    S.add(tempArr.get(rand));
                    tempArr.remove(rand);
                }
            }
            Collections.shuffle(S);

            for (int k = 0; k < a; k++) {
                ArrayList<Integer> v = new ArrayList<>();
                int c;
                if (App.get(k).getLastTier() == null) {
                    c = 0;
                } else {
                    c = App.get(k).getLastTier().getLast();
                }
                for (int value : S) {
                    if (value == k) {
                        v.add(c + 1);
                        c++;
                    }
                }
                App.get(k).setTier(v);
            }

            Sol.add(S);
        }
    }

    private double RandDouble(double min, double max) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextDouble() * ((max - min) + min);
        }
        return result;
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
            for (int j = 0; j < App.get(i).getNumVm(); j++) {
                App.get(i).setLamb(RandInteger(2, 10));
            }
        }

        for (int i = 0; i < m; i++) {
            ArrayList<Integer> p = new ArrayList<>();
            for (int j = 0; j < a; j++) {
                p.add(0);
            }

            for (int j = 0; j < tier; j++) {
                p.set(Sol.get(j).get(i), p.get(Sol.get(j).get(i)) + 1);
            }
            PA.add(p);
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

        lb = App.get(k).getTier(0);
        for (int i = 0; i < lb.size(); i++) {
            R[0][lb.get(i)] = 1.0 / lb.size();
        }

        for (int t = 1; t < tier; t++) {
            rb = App.get(k).getTier(t);
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
            gamma = RandInteger(20, 30);
            Beta = gamma;
            ArrayList<Integer> lamb = App.get(k).getLamb();
            RChangeMatrix(k, App.get(k).getNumVm() + 1);
            L = new double[App.get(k).getNumVm()];
            Lamb = new double[App.get(k).getNumVm() + 1];
            Lamb[0] = gamma;

            for (int i = 0; i < App.get(k).getNumVm(); i++) {
                double sum = 0.0;
                for (int r = 0; r < App.get(k).getNumVm() + 1; r++) {
                    sum = sum + R[i + 1][r] * Lamb[i];
                }
                Lamb[i + 1] = lamb.get(i) + sum;
                double p = Lamb[i + 1] / nguy[i];
                L[i] = p / (1 - p) == Double.POSITIVE_INFINITY ? 0 : p / (1 - p);
//                L_s += L[i];
                Beta += lamb.get(i);
            }
            
            L_s = Kahan_Summation(L);
            double res = L_s / Beta;
//            Math.abs(L_s / Beta);
//            if (res == Double.POSITIVE_INFINITY || res == Double.NaN) {
//                System.err.println(L_s + " " + Beta);
//            }
            Wt.add(res);
        }
    }

    private void CPUCosts() {
        for (int i = 0; i < m; i++) {
            double result = pms.get(i).getCore();
            for (int j = 0; j < tier; j++) {
                result = result + vms.get(Soln.get(j).get(i) - 1).getCore();
            }
            result = alpha * result + beta;
            Pa.add(result);
        }

        for (int k = 0; k < a; k++) {
            double result = 0.0;
            for (int i = 0; i < m; i++) {
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
//        result = val1 + val2;
        return result;
    }
}
