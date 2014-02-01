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

import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.geom.Line;
import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.Random;

public class Phenotype
{
    private int[] genes;
    private Double fitness = null;

    public Phenotype(int[] genes)
    {
        this.genes = genes;
    }

    public double calculateFitness(Environment env)
    {
        if (fitness != null)
            return fitness;

        Point currentLocation = new Point(0, 0);
        fitness = 0.0;

        for (int i : genes)
        {
            Path path = env.getPaths().get(i);
            fitness += currentLocation.distanceTo(path.getStart());
            currentLocation = path.getEnd();
        }

        return fitness;
    }

    public int[] getGenes()
    {
        return genes;
    }

    public Phenotype crossOver(Phenotype partner)
    {
        Random random = new Random();
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
        Random random = new Random();
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
