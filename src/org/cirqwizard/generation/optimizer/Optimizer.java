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

package org.cirqwizard.generation.optimizer;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.cirqwizard.toolpath.Toolpath;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Optimizer
{
    private final static int POPULATION_SIZE = 350;
    private final static int TOURNAMENT_SIZE = 7;
    private final static double MUTATION_PROBABILITY = 0.025;
    private final static int SEED_COUNT = 1;
    private final static double SEED_PROBABILITY = 0;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

    private List<Path> paths;
    private Environment environment;

    private Generation currentGeneration;

    private DoubleProperty progressProperty;
    private StringProperty estimatedMachiningTimeProperty;

    public Optimizer(List<Path> paths, Environment environment, DoubleProperty progressProperty, StringProperty estimatedMachiningTimeProperty)
    {
        this.paths = paths;
        this.environment = environment;
        this.progressProperty = progressProperty;
        this.estimatedMachiningTimeProperty = estimatedMachiningTimeProperty;
    }

    public List<Path> optimize()
    {
        init();

        double lastEvaluation = Double.MAX_VALUE;
        for (int i = 0; i < 10000; i++)
        {
            progressProperty.setValue((double) i / 10000);
            breed();
            if (i % 200 == 0)
            {
                Phenotype mostFit = currentGeneration.getBestFitness(environment);
                List<Toolpath> l = new ArrayList<>();
                for (int j : mostFit.getGenes())
                    l.addAll(environment.getPaths().get(j).getSegments());
                final double bestResult = TimeEstimator.calculateTotalDuration(l, 1000.0 / 60, 200.0 / 60, 2.0, 0.3, false);
                final long totalDuration = (long)TimeEstimator.calculateTotalDuration(l, 1000.0 / 60, 200.0 / 60, 2.0, 0.3, true) * 1000;
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        estimatedMachiningTimeProperty.setValue("Estimated milling time: " + timeFormat.format(new Date(totalDuration)));
                    }
                });
                if (Math.abs(lastEvaluation - bestResult) < 0.2)
                    break;
                lastEvaluation = bestResult;
            }
        }

        Phenotype mostFit = currentGeneration.getBestFitness(environment);
        ArrayList<Path> result = new ArrayList<>();
        for (int i : mostFit.getGenes())
            result.add(paths.get(i));
        return result;
    }

    private void init()
    {
        int[] originalGenes = new int[paths.size()];
        for (int i = 0; i < originalGenes.length; i++)
            originalGenes[i] = i;
        currentGeneration = new Generation();
        currentGeneration.populate(paths.size(), POPULATION_SIZE);
    }

    public void breed()
    {
        final Vector<Phenotype> newGeneration = new Vector<>();

        if (Math.random() < SEED_PROBABILITY)
        {
            int[] originalPhenotype = new int[paths.size()];
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
