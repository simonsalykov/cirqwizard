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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final static int MAX_GENERATIONS_COUNT = 10_000;
    private final static int REEVALUATION_FREQUENCY = 200;
    private final static double MIN_IMPROVEMENT = 0.2;

    private Environment environment;
    private Generation currentGeneration;

    private double feed;
    private double arcFeed;
    private double zFeed;
    private double clearance;
    private double safetyHeight;
    private int mergeTolerance;

    private DoubleProperty progressProperty = new SimpleDoubleProperty();
    private DoubleProperty bestSolutionDuration = new SimpleDoubleProperty();
    private BooleanProperty cancelledProperty;

    public Optimizer(List<Chain> chains, double feed, double zFeed, double arcFeed, double clearance, double safetyHeight, int mergeTolerance,
                     BooleanProperty cancelledProperty)
    {
        this.environment = new Environment(chains);
        this.feed = feed;
        this.arcFeed = arcFeed;
        this.zFeed = zFeed;
        this.clearance = clearance;
        this.safetyHeight = safetyHeight;
        this.mergeTolerance = mergeTolerance;
        this.cancelledProperty = cancelledProperty;
    }

    public List<Chain> optimize()
    {
        init();

        double lastEvaluation = Double.MAX_VALUE;
        for (int i = 0; i < MAX_GENERATIONS_COUNT; i++)
        {
            if (cancelledProperty.get())
                break;

            progressProperty.setValue((double) i / MAX_GENERATIONS_COUNT);
            breed();
            if (i % REEVALUATION_FREQUENCY == 0)
            {
                Phenotype mostFit = currentGeneration.getBestFitness(environment);
                final List<Toolpath> l = new ArrayList<>();
                for (int j : mostFit.getGenes())
                    l.addAll(environment.getChains().get(j).getSegments());
                final double bestResult = TimeEstimator.calculateTotalDuration(l, feed, zFeed, arcFeed, clearance, safetyHeight, true, mergeTolerance);
                Platform.runLater(() -> bestSolutionDuration.setValue(bestResult));
                if (Math.abs(lastEvaluation - bestResult) < MIN_IMPROVEMENT)
                    break;
                lastEvaluation = bestResult;
            }
        }

        Phenotype mostFit = currentGeneration.getBestFitness(environment);

        int[] originalGenes = new int[environment.getChains().size()];
        Arrays.setAll(originalGenes, i -> i);
        Phenotype original = new Phenotype(originalGenes);
        System.out.println("@@ mostFit: " + mostFit.calculateFitness(environment) + ", original: " + original.calculateFitness(environment));

        ArrayList<Chain> result = new ArrayList<>();
        for (int i : mostFit.getGenes())
            result.add(environment.getChains().get(i));
        return result;
    }

    public DoubleProperty progressProperty()
    {
        return progressProperty;
    }

    public double getBestSolutionDuration()
    {
        return bestSolutionDuration.get();
    }

    public DoubleProperty bestSolutionDurationProperty()
    {
        return bestSolutionDuration;
    }

    private void init()
    {
        int[] originalGenes = new int[environment.getChains().size()];
        for (int i = 0; i < originalGenes.length; i++)
            originalGenes[i] = i;
        currentGeneration = new Generation();
        currentGeneration.populate(environment.getChains().size(), POPULATION_SIZE);
    }

    public void breed()
    {
        final Vector<Phenotype> newGeneration = new Vector<>();

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < POPULATION_SIZE; i++)
        {
            pool.submit(() ->
            {
                Phenotype parent1 = currentGeneration.tournamentWinner(environment, TOURNAMENT_SIZE);
                Phenotype parent2 = currentGeneration.tournamentWinner(environment, TOURNAMENT_SIZE);
                Phenotype child = parent1.crossOver(parent2);
                if (Math.random() < MUTATION_PROBABILITY)
                    child.mutate();
                child.calculateFitness(environment);
                newGeneration.add(child);
            });
        }
        try
        {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException e) {}
        currentGeneration = new Generation(new ArrayList<>(newGeneration));
    }
}
