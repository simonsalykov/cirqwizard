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

package org.cirqwizard.test.pp;

import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.PPParser;
import org.cirqwizard.toolpath.PPPoint;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PPParserTest
{

    @Test
    public void testEagleFile() throws IOException
    {
        String fileContent = "C1  7.68  2.67   0 1µF SMD_0603\n" +
                "C5 26.10 16.64 270 100nF SMD_0603\n" +
                "C8 26.10 16.64 270 SMD_0805";
        String regex = "(?<name>\\S+)\\s+(?<x>\\d+.?\\d*)\\s+(?<y>\\d+.?\\d*)\\s+(?<angle>\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(3, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("SMD_0603", "1µF"), p.getId());
        assertEquals(new Point(new RealNumber("7.68"), new RealNumber("2.67")), p.getPoint());
        assertEquals(new RealNumber(0), p.getAngle());
        assertEquals("C1", p.getName());

        p = points.get(1);
        assertEquals(new ComponentId("SMD_0603", "100nF"), p.getId());
        assertEquals(new Point(new RealNumber("26.10"), new RealNumber("16.64")), p.getPoint());
        assertEquals(new RealNumber(270), p.getAngle());
        assertEquals("C5", p.getName());

        p = points.get(2);
        assertEquals(new ComponentId("SMD_0805", ""), p.getId());
        assertEquals(new Point(new RealNumber("26.10"), new RealNumber("16.64")), p.getPoint());
        assertEquals(new RealNumber(270), p.getAngle());
        assertEquals("C8", p.getName());
    }

    @Test
    public void testUltiBoardFile() throws IOException
    {
        String fileContent = "\"C1\",\"100nF\",\"KERKO5X4R5\",\"23.876000\",\"14.732000\",\"90\",\"TOP\",\"THD\"\n" +
                "\"J16\",\"JUMPER\",\"JUMPER\",\"\",\"\",\"0\",\"TOP\",\"THD\"\n" +
                "\"R1\",\"10kOhm\",\"RESC6432X70N\",\"29.972000\",\"56.388000\",\"180\",\"TOP\",\"SMD\"";
        String regex = "\"(?<name>\\S+)\",\"(?<value>\\S+)\",\"(?<package>\\S+)\",\"(?<x>\\d+.?\\d*)\",\"(?<y>\\d+.?\\d*)\",\"(?<angle>\\d+)\",\"TOP\",\"SMD\"";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(1, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("RESC6432X70N", "10kOhm"), p.getId());
        assertEquals(new Point(new RealNumber("29.972"), new RealNumber("56.388")), p.getPoint());
        assertEquals(new RealNumber(180), p.getAngle());
        assertEquals("R1", p.getName());
    }


}
