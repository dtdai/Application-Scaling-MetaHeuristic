package algorithm;

import definition.Machine;
import definition.PhysicalMachine;
import definition.VirtualMachine;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class HybridACOSA {

    private final int numAnts;
    private final int numPM;
    private final int numVM;
    private final ArrayList<PhysicalMachine> pms;
    private final ArrayList<VirtualMachine> vms;
    private final double[][] trails;
    private double[] probabilities;
    private final double alpha;
    private final double beta;
    private final double evaporationRate;
    private final int Q = 1;
    private final double nguy = 0.5;
    private final double sa_temp;
    private final double sa_coolRate;

    public HybridACOSA(int numAnts, ArrayList<Machine> mc, double alpha, double beta, double evaporationRate, double sa_temp, double sa_coolRate) {
        pms = new ArrayList<>();
        vms = new ArrayList<>();
        for (Machine i : mc) {
            if (i instanceof PhysicalMachine) {
                pms.add((PhysicalMachine) i);
            } else if (i instanceof VirtualMachine) {
                vms.add((VirtualMachine) i);
            }
        }

        this.numAnts = numAnts;
        this.numVM = vms.size();
        this.numPM = pms.size();
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = evaporationRate;
        trails = new double[numVM][numPM];
        probabilities = new double[numPM];
        this.sa_temp = sa_temp;
        this.sa_coolRate = sa_coolRate;
    }

    public void solve() {
        ArrayList<Returning> Rbest = new ArrayList<>();

        for (int i = 0; i < numVM; i++) {
            for (int j = 0; j < numPM; j++) {
                trails[i][j] = 0.001;
            }
        }

        for (int index = 0; index < 100; index++) {
            ArrayList<Integer> ant = new ArrayList<>();
            double rbest = 1e9;

            for (int i = 0; i < numAnts; i++) {
                Returning antres = generateAntTour();
                if (antres.tour.size() == numVM && antres.rbest < rbest) {
                    ant = antres.tour;
                    rbest = antres.rbest;
                }
            }
            
            for (int g = 0; g < 100; g++) {
                double rand = RandomDoubleMinMax(0, 1);
                if (rand <= 0.8) {
                    Returning sares = generateSAtour(ant, sa_temp, sa_coolRate);
                    if (sares.tour.size() == numVM && sares.rbest < rbest) {
                        ant = sares.tour;
                        rbest = sares.rbest;
                    }
                }
            }

            Rbest.add(new Returning(ant, rbest));
        }

        FindBestSolution(Rbest);
    }

    /**
     * Generate ant path based on pheromones
     *
     * @return path & Benefit value
     */
    private Returning generateAntTour() {
        ArrayList<Integer> tour = new ArrayList<>();
        double rbest = 0.0;

        // clone hosts
        ArrayList<PhysicalMachine> hosts = CloneHost();

        int currentNode = 0;

        // Build the tour
        for (int i = 0; i < numVM; i++) {

            probabilities = calculateProbabilities(trails, hosts, currentNode);

            int nextNode = selectNext(currentNode, probabilities, hosts);

            if (nextNode == -1) {
                break;
            }

            Allocation(hosts, nextNode, i);
            tour.add(nextNode + 1);
            currentNode = nextNode;
        }

        // If Ant finished route
        if (tour.size() == numVM) {
            GameModel gameModel = new GameModel(pms, vms, tour);
            rbest = gameModel.BenefitFunction();
            updateTrails(hosts, trails, numVM, numPM);
        }

        return new Returning(tour, rbest);
    }
    
    /**
     * Generate ant path based on previous path
     * @param bestroute
     * @param temporature
     * @param coolRate
     * @return path & benefit value
     */
    private Returning generateSAtour(ArrayList<Integer> bestroute, double temporature, double coolRate) {
        ArrayList<Integer> intialtour = new ArrayList<>();
        ArrayList<PhysicalMachine> host = CloneHost();

        int rand = RandomIntMinMax(1, numVM);
        if (bestroute.size() == numVM && rand <= Math.round(numVM / 3)) {
            for (int index = 0; index < rand; index++) {
                try {
                    Allocation(host, bestroute.get(index), index);
                    intialtour.add(bestroute.get(index));
                } catch (Exception ex) {

                }
            }
        } else {
            rand = 0;
        }

        ArrayList<Integer> bettertour = new ArrayList<>();
        double rbetter = 1e9;
        while (temporature >= 20) {
            ArrayList<Integer> tour = new ArrayList<>();

            for (int i = 0; i < intialtour.size(); i++) {
                tour.add(intialtour.get(i));
            }

            for (int i = rand; i < numVM; i++) {
                int randPM = RandomIntMinMax(0, numPM - 1);
                randPM = CheckAvailable(host, randPM, i, 1);
                if (randPM == -1) {
                    break;
                }
                Allocation(host, randPM, i);
                tour.add(randPM + 1);
            }

            if (tour.size() == numVM) {
                GameModel gameModel = new GameModel(pms, vms, tour);
                double rbest = gameModel.BenefitFunction();

                if (rbest < rbetter) {
                    rbetter = rbest;
                    bettertour = tour;
                }
                updateTrails(host, trails, numVM, numPM);
            }

            temporature = temporature * (1 - coolRate);
        }

        return new Returning(bettertour, rbetter);
    }
    
    private ArrayList<PhysicalMachine> CloneHost() {
        ArrayList<PhysicalMachine> hosts = new ArrayList<>();
        for (PhysicalMachine machine : pms) {
            hosts.add(new PhysicalMachine(machine.getCore(), machine.getRam(), machine.getDisk()));
        }
        return hosts;
    }

    private int selectNext(int currentNode, double[] probabilities, ArrayList<PhysicalMachine> hosts) {
        int index = -1;
        double maxProb = 0.0;

        double r = RandomDoubleMinMax(0, 1);
        if (r <= 0.6) {
            for (int i = 0; i < numPM; i++) {
                index = RandomIntMinMax(0, numPM - 1);
                PhysicalMachine pm = hosts.get(index);
                if (pm.CheckAvailable(vms.get(currentNode))) {
                    return index;
                }
            }

        } else {
            for (int i = 0; i < numPM; i++) {
                PhysicalMachine pm = hosts.get(i);
                if (maxProb < probabilities[i] && pm.CheckAvailable(vms.get(currentNode))) {
                    maxProb = probabilities[i];
                    index = i;
                }
            }
        }

        return index;
    }

    private void updateTrails(ArrayList<PhysicalMachine> hosts, double[][] trails, int numVM, int numPM) {
        double r[] = UpdatePheromone(hosts);
        double contribution = Q / (1 / (nguy * r[0] + (1 - nguy) * r[1]));
        for (int i = 0; i < numVM; i++) {
            for (int j = 0; j < numPM; j++) {
                trails[i][j] = (1 - evaporationRate) * trails[i][j] + contribution;
            }
        }
    }

    private void Allocation(ArrayList<PhysicalMachine> host, int indexPM, int indexVM) {
        host.get(indexPM).Allocation(vms.get(indexVM));
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

    private int RandomIntMinMax(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    private double RandomDoubleMinMax(double min, double max) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextDouble(((max - min) + 1)) + min;
        }
        return result;
    }

    private double[] UpdatePheromone(ArrayList<PhysicalMachine> hosts) {
        double[] uload = new double[numPM];
        double[] wload = new double[numPM];
        for (int i = 0; i < hosts.size(); i++) {
            uload[i] = Math.round((0.3 * (hosts.get(i).getCore() - hosts.get(i).getAvailable_core())
                    + 0.3 * (hosts.get(i).getRam() - hosts.get(i).getAvailable_ram())
                    + 0.4 * (hosts.get(i).getDisk() - hosts.get(i).getAvailable_disk())) * 100) / 100;
            wload[i] = Math.sqrt(Math.pow(hosts.get(i).getAvailable_core() / hosts.get(i).getCore(), 2)
                    + Math.pow(hosts.get(i).getAvailable_ram() / hosts.get(i).getRam(), 2)
                    + Math.pow(hosts.get(i).getAvailable_disk() / hosts.get(i).getDisk(), 2));
        }
        double[] s = new double[2];
        double avgrload = avgrLoadPM(uload);

        double varLoad = 0.0;
        double wasteLoad = 0.0;
        for (int j = 0; j < numPM; j++) {
            varLoad += Math.pow(uload[j] - avgrload, 2);
            wasteLoad += wload[j];
        }
        s[0] = varLoad / (1.0 * (numPM - 1));
        s[1] = wasteLoad;
        return s;
    }

    private double avgrLoadPM(double[] aload) {
        double avgrload = 0.0;
        for (int i = 0; i < aload.length; i++) {
            avgrload += aload[i];
        }
        return avgrload / numPM;
    }

    private double[] calculateProbabilities(double[][] trails, ArrayList<PhysicalMachine> hosts, int currentNode) {
        double[] localProbabilities = new double[numPM];
        double pheromone = pheromones(trails, currentNode, hosts);
        for (int i = 0; i < numPM; i++) {
            double probability = Math.pow(trails[currentNode][i], alpha) * Math.pow(1.0 / hosts.get(i).performance(), beta);
            localProbabilities[i] = probability / pheromone;
        }
        return localProbabilities;
    }

    private double pheromones(double[][] trails, int currentNode, ArrayList<PhysicalMachine> hosts) {
        double pheromone = 0.0;

        for (int i = 0; i < numPM; i++) {
            pheromone += Math.pow(trails[currentNode][i], alpha) * Math.pow(1.0 / hosts.get(i).performance(), beta);
        }
        return pheromone;
    }

    private void FindBestSolution(ArrayList<Returning> Rbest) {
        Returning temp = Rbest.getFirst();
        for (int i = 1; i < Rbest.size(); i++) {
            if (temp.rbest > Rbest.get(i).rbest) {
                temp = Rbest.get(i);
            }
        }

        System.out.println("Best Solution using Hybrid ACO-SA is:" + temp.tour.toString());
        System.out.println("The Benifit Value is: " + temp.rbest);
    }

    private class Returning {

        ArrayList<Integer> tour;
        double rbest;

        Returning(ArrayList<Integer> tour, double rbest) {
            this.tour = tour;
            this.rbest = rbest;
        }
    }
    
}
