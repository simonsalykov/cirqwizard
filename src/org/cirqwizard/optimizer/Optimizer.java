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

import java.util.List;

public class Optimizer
{
    private final static int POPULATION_SIZE = 5000;
    private final static int TOURNAMENT_SIZE = 4;

    private List<Toolpath> toolpaths;

    private Generation currentGeneration;

    public Optimizer(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

    public void optimize()
    {
        init();
    }

    private void init()
    {
        Environment environment = new Environment(1000.0 / 60, 200.0 / 60, 5.0, 2.0);
        System.out.println("Original phenotype fitness: " + new Phenotype(toolpaths).calculateFitness(environment));
        currentGeneration = new Generation();
        long t = System.currentTimeMillis();
        currentGeneration.populate(toolpaths, 5000);
        t = System.currentTimeMillis() - t;
        System.out.println("Population generation: " + t);
        t = System.currentTimeMillis();
        double bestRandom = currentGeneration.getBestFitness(environment);
        t = System.currentTimeMillis() - t ;
        System.out.println("Best random calculation: " + t);
        System.out.println("Best random: " + bestRandom);

    }
}
