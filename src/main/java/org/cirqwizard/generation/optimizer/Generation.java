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

import java.util.ArrayList;
import java.util.Random;

public class Generation
{
    private ArrayList<Phenotype> population = new ArrayList<>();

    public Generation()
    {
    }

    public Generation(ArrayList<Phenotype> population)
    {
        this.population = population;
    }

    public void populate(int genomeSize, int size)
    {
        Random rnd = new Random();
        for (int i = 0; i < size; i++)
        {
            ArrayList<Integer> toAdd = new ArrayList<>();
            for (int j = 0; j < genomeSize; j++)
                toAdd.add(j);
            int[] newGenes = new int[toAdd.size()];
            for (int j = 0; j < newGenes.length; j++)
                newGenes[j] = toAdd.remove(rnd.nextInt(toAdd.size()));
            population.add(new Phenotype(newGenes));
        }
    }

    public Phenotype getBestFitness(Environment environment)
    {
        Phenotype mostFit = null;
        for (Phenotype p : population)
            if (mostFit == null || p.calculateFitness(environment) < mostFit.calculateFitness(environment))
                mostFit = p;

        return mostFit;
    }

    public Phenotype tournamentWinner(Environment environment, int tournamentSize)
    {
        Phenotype winner = null;
        Random random = new Random();
        for (int i = 0; i < tournamentSize; i++)
        {
            Phenotype p = population.get(random.nextInt(population.size()));
            if (winner == null || p.calculateFitness(environment) < winner.calculateFitness(environment))
                winner = p;
        }
        return winner;
    }

}
