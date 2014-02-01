/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.optimizer;

import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Optimizer
{
    private final static int POPULATION_SIZE = 350;
    private final static int TOURNAMENT_SIZE = 7;
    private final static double MUTATION_PROBABILITY = 0.05;
    private final static int SEED_COUNT = 1;
    private final static double SEED_PROBABILITY = 0.01;

    private List<Toolpath> toolpaths;
    private Environment environment;

    private Generation currentGeneration;

    public Optimizer(List<Toolpath> toolpaths, Environment environment)
    {
        this.toolpaths = toolpaths;
        this.environment = environment;
    }

    public void optimize()
    {
        init();

        long t = 0;
        for (int i = 0; i < 10000000; i++)
        {
            boolean debug = i % 100 == 0;
            if (debug)
            {
                System.out.println("Generation #" + i);
                t = System.currentTimeMillis();
            }
            breed();
            if (debug)
            {
                t = System.currentTimeMillis() - t;
                System.out.println("Breeding time: " + t);
                t = System.currentTimeMillis();
            }
            Phenotype mostFit = currentGeneration.getBestFitness(environment);
            if (debug)
            {
                t = System.currentTimeMillis() - t ;
                System.out.println("Best generation: " + mostFit.calculateFitness(environment) + " / " +
                        mostFit.calculateTotalDuration(environment, false) + " / " + mostFit.calculateTotalDuration(environment, true) + " @ " +
                        mostFit.calculateRapidsCount(environment, 1));
            }
        }
    }

    private void init()
    {
        int[] originalGenes = new int[toolpaths.size()];
        for (int i = 0; i < originalGenes.length; i++)
            originalGenes[i] = i;
        Phenotype original = new Phenotype(originalGenes);
        System.out.println("Original phenotype fitness: " + original.calculateFitness(environment) + " / " +
                original.calculateTotalDuration(environment, false) + " / " + original.calculateTotalDuration(environment, true) + " @ " +
                original.calculateRapidsCount(environment, 1));
        currentGeneration = new Generation();
        long t = System.currentTimeMillis();
        currentGeneration.populate(toolpaths.size(), POPULATION_SIZE);
        t = System.currentTimeMillis() - t;
        System.out.println("Population generation: " + t);
        t = System.currentTimeMillis();

        Phenotype mostFit = currentGeneration.getBestFitness(environment);
        t = System.currentTimeMillis() - t ;
        System.out.println("Best random calculation: " + t);
        System.out.println("Best random: " + mostFit.calculateFitness(environment) + " / " +
                mostFit.calculateTotalDuration(environment, false) + " / " + mostFit.calculateTotalDuration(environment, true) + " @ " +
                mostFit.calculateRapidsCount(environment, 1));

    }

    public void breed()
    {
        final Vector<Phenotype> newGeneration = new Vector<>();

        if (Math.random() < SEED_PROBABILITY)
        {
            int[] originalPhenotype = new int[toolpaths.size()];
            for (int i = 0; i < originalPhenotype.length; i++)
                originalPhenotype[i] = i;
            for (int i = 0; i < SEED_COUNT; i++)
                newGeneration.add(new Phenotype(originalPhenotype));
        }

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = SEED_COUNT; i < POPULATION_SIZE; i++)
        {
            pool.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    Phenotype parent1 = currentGeneration.tournamentWinner(environment, TOURNAMENT_SIZE);
                    Phenotype parent2 = currentGeneration.tournamentWinner(environment, TOURNAMENT_SIZE);
                    Phenotype child = parent1.crossOver(parent2);
                    if (Math.random() < MUTATION_PROBABILITY)
                        child.mutate();
                    child.calculateFitness(environment);
                    newGeneration.add(child);
                }
            });
        }
        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
        }
        currentGeneration = new Generation(new ArrayList<>(newGeneration));
    }
}
