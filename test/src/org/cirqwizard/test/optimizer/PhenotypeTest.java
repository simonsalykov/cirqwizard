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

package org.cirqwizard.test.optimizer;

import org.cirqwizard.geom.Point;
import org.cirqwizard.optimizer.Environment;
import org.cirqwizard.optimizer.Phenotype;
import org.cirqwizard.toolpath.CircularToolpath;
import org.cirqwizard.toolpath.LinearToolpath;
import org.cirqwizard.toolpath.Toolpath;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PhenotypeTest
{

    @Test
    public void testStraigthLine()
    {
        Toolpath t = new LinearToolpath(0, new Point(0, 0), new Point(100_000, 0));
        assertEquals(20.1, new Phenotype(Arrays.asList(t)).calculateFitness(new Environment(5.0, 200.0 / 60, 5.0, 2.0)), 0.01);
    }

    @Test
    public void testDisjointLines()
    {
        Toolpath t1 = new LinearToolpath(0, new Point(0, 0), new Point(100_000, 0));
        Toolpath t2 = new LinearToolpath(0, new Point(0, 0), new Point(100_000, 0));
        assertEquals(46.095, new Phenotype(Arrays.asList(t1, t2)).calculateFitness(new Environment(5.0, 200.0 / 60, 5.0, 2.0)), 0.001);
    }

    @Test
    public void testArc()
    {
        Toolpath t = new CircularToolpath(0, new Point(0, 0), new Point(0, 10000), new Point(0, 5000), 5000, true);
        assertEquals(2.38, new Phenotype(Arrays.asList(t)).calculateFitness(new Environment(1000.0 / 60, 200.0 / 60, 5.0, 2.0)), 0.01);
    }


}
