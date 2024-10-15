package algorithm;

import definition.Machine;
import definition.PhysicalMachine;
import definition.VirtualMachine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class PSO {

    private ArrayList<PhysicalMachine> pms;
    private ArrayList<VirtualMachine> vms;
    private int numPM;
    private int numVM;
    private final int Particle;
    private final int Iteration;
    private final double W;
    private final double C1;
    private final double C2;
    private ArrayList<ArrayList<PhysicalMachine>> loc = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> locPos = new ArrayList<>();
    private ArrayList<Double> pbest = new ArrayList<>();
    private ArrayList<PhysicalMachine> gbest = new ArrayList<>();
    private ArrayList<Integer> gbestPos = new ArrayList<>();
    private double gbestfitness;
    private ArrayList<Double> vel = new ArrayList<>();

    public PSO(ArrayList<Machine> machine, int numParticle, int Iteration, double W, double C1, double C2) {
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
        this.Particle = numParticle;
        this.Iteration = Iteration;
        this.W = W;
        this.C1 = C1;
        this.C2 = C2;
    }

    public void solve() {
        IntialParticles();
        for (int it = 0; it < Iteration; it++) {
            VelocityUpdate();
            PositionUpdate();
            LocalBestUpdate();
            GlobalBestUpdate();
        }

        System.out.println("Best Solution using PSO is: " + gbestPos.toString());
        System.out.println("Best value is " + gbestfitness);
    }

    private void IntialParticles() {
        RandPosition();
        RandVelocity();
        int gpos = 0;
        for (int i = 0; i < Particle; i++) {
            if (pbest.get(i) < pbest.get(gpos)) {
                gpos = i;
            }
        }
        gbest = loc.get(gpos);
        gbestPos = locPos.get(gpos);
        gbestfitness = pbest.get(gpos);
    }

    private void RandPosition() {
        for (int it = 0; it < Particle; it++) {
            ArrayList<PhysicalMachine> hosts = CloneHost();
            ArrayList<PhysicalMachine> sol = new ArrayList<>();
            ArrayList<Integer> solPos = new ArrayList<>();

            // Generate tour
            for (int i = 0; i < numVM; i++) {
                int randPM = randInt(0, numPM - 1);
                randPM = CheckAvailable(hosts, randPM, i, 1);
                if (randPM == -1) {
                    break;
                }
                sol.add(hosts.get(randPM));
                solPos.add(randPM + 1);
                Allocation(hosts, randPM, i);
            }

            if (sol.size() == numVM) {
                loc.add(sol);
                locPos.add(solPos);
                GameModel gameModel = new GameModel(pms, vms, solPos);
                pbest.add(gameModel.BenefitFunction());
            } else {
                it--;
            }
        }
    }

    private void RandVelocity() {
        for (int i = 0; i < Particle; i++) {
            Double v = randDouble(-2.0, 2.0);
            vel.add(v);
        }
    }

    private void VelocityUpdate() {
        for (int i = 0; i < Particle; i++) {
            double veloc = vel.get(i);
            GameModel gameModel = new GameModel(pms, vms, locPos.get(i));
            double pos = gameModel.BenefitFunction();
            veloc = W * veloc + this.C1 * randDouble(0, 1) * (pbest.get(i) - pos) + this.C2 * randDouble(0, 1) * (gbestfitness - pos);
            vel.set(i, veloc);
        }
    }

    private void PositionUpdate() {
        for (int i = 0; i < Particle; i++) {
            GameModel gameModel = new GameModel(pms, vms, locPos.get(i));
            double pos = gameModel.BenefitFunction();
            pos = Math.abs(pos + vel.get(i));
            if (Double.isInfinite(pos) || Double.isNaN(pos)) {
                continue;
            }
            while (pos > 1000) {
                pos = Math.sqrt(pos);
            }
//            System.out.println(pos);
            SwapPosition(i, (int) pos);
        }
    }

    private void LocalBestUpdate() {
        for (int i = 0; i < Particle; i++) {
            GameModel gameModel = new GameModel(pms, vms, locPos.get(i));
            double fitness_value = gameModel.BenefitFunction();
            if (fitness_value > pbest.get(i)) {
                pbest.set(i, fitness_value);
            }
        }
    }

    private void GlobalBestUpdate() {
        for (int i = 0; i < Particle; i++) {
            if (pbest.get(i) > gbestfitness) {
                gbest = loc.get(i);
                gbestPos = locPos.get(i);
            }
        }
    }

    private double randDouble(double min, double max) {
        double result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextDouble(((max - min) + 1)) + min;
        }
        return result;
    }

    private int randInt(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    private ArrayList<PhysicalMachine> CloneHost() {
        ArrayList<PhysicalMachine> host = new ArrayList<>();
        for (Iterator<PhysicalMachine> it = pms.iterator(); it.hasNext();) {
            PhysicalMachine pm = new PhysicalMachine(it.next());
            host.add(pm);
        }
        return host;
    }

    private int CheckAvailable(ArrayList<PhysicalMachine> host, int indexPM, int indexVM, int runtime) {
        while (!host.get(indexPM).CheckAvailable(vms.get(indexVM))) {
            indexPM = randInt(0, numPM - 1);
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

    private void SwapPosition(int index, int coeff) {
        for (int i = 0; i < coeff; i++) {
            int random1 = 0;
            int random2 = 0;

            while (random1 == random2) {
                random1 = randInt(0, numVM - 1);
                random2 = randInt(0, numVM - 1);
            }
            PhysicalMachine pm1 = loc.get(index).get(random1);
            PhysicalMachine pm2 = loc.get(index).get(random2);
            int Pos1 = locPos.get(index).get(random1);
            int Pos2 = locPos.get(index).get(random2);
            
            loc.get(index).set(random2, pm1);
            loc.get(index).set(random1, pm2);
            locPos.get(index).set(random2, Pos1);
            locPos.get(index).set(random1, Pos2);
        }
    }
}
