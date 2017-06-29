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

import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.ApplicationConstants;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Phenotype
{
    private int[] genes;
    private double fitness = -1;

    private static double MOTION_PENALTY = 2000;

    public Phenotype(int[] genes)
    {
        this.genes = genes;
    }

    public double calculateFitness(Environment env)
    {
        if (fitness >= 0)
            return fitness;

        Point currentLocation = env.getChains().get(0).getStart();
        fitness = 0.0;

        for (int i : genes)
        {
            Chain chain = env.getChains().get(i);
            double distance = currentLocation.distanceTo(chain.getStart());
            if (distance > ApplicationConstants.ROUNDING)
            {
                fitness += distance;
                fitness += MOTION_PENALTY;
            }
            currentLocation = chain.getEnd();
        }

        return fitness;
    }

    public int[] getGenes()
    {
        return genes;
    }

    public Phenotype crossOver(Phenotype partner)
    {
        Random random = ThreadLocalRandom.current();
        int firstIndex = random.nextInt(genes.length);
        int lastIndex = random.nextInt(genes.length);

        int[] childGenes = new int[genes.length];
        if (lastIndex < firstIndex)
            lastIndex += genes.length;
        int counter = 0;

        boolean[] copied = new boolean[genes.length];
        for (int i = firstIndex; i < lastIndex; i++)
        {
            int g = genes[i % genes.length];
            childGenes[counter++] = g;
            copied[g] = true;
        }
        for (int i = 0; i < partner.genes.length; i++)
        {
            if (!copied[partner.genes[i]])
                childGenes[counter++] = partner.genes[i];
        }

        return new Phenotype(childGenes);
    }

    public void mutate()
    {
        Random random = ThreadLocalRandom.current();
        int genesCount = random.nextInt(genes.length / 2);
        for (int i = 0; i < genesCount; i++)
        {
            int gene1 = random.nextInt(genes.length);
            int gene2 = random.nextInt(genes.length);
            int g = genes[gene1];
            genes[gene1] = genes[gene2];
            genes[gene2] = g;
        }
    }

}
