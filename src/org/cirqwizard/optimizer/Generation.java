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
import java.util.Random;

public class Generation
{
    private ArrayList<Phenotype> population = new ArrayList<>();
    private Double fitness;

    public void populate(List<Toolpath> toolpaths, int size)
    {
        Random rnd = new Random();
        for (int i = 0; i < size; i++)
        {
            ArrayList<Toolpath> toAdd = new ArrayList<>(toolpaths);
            ArrayList<Toolpath> newList = new ArrayList<>();
            while (!toAdd.isEmpty())
            {
                Toolpath t = toAdd.remove(rnd.nextInt(toAdd.size()));
                newList.add(t);
            }
            population.add(new Phenotype(newList));
        }
    }

    public double getBestFitness(Environment environment)
    {
        if (fitness != null)
            return fitness;

        fitness = Double.MAX_VALUE;
        for (Phenotype p : population)
        {
            double f = p.calculateFitness(environment);
            if (f < fitness)
                fitness = f;
        }
        return fitness;
    }

}
