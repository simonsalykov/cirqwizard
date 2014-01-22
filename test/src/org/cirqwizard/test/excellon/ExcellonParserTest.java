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

package org.cirqwizard.test.excellon;

import org.cirqwizard.excellon.ExcellonParser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.DrillPoint;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;


public class ExcellonParserTest
{

    @Test
    public void testEagleFile() throws IOException
    {
        String fileContent = "%\n" +
                "M48\n" +
                "M72\n" +
                "T01C0.0236\n" +
                "T02C0.0354\n" +
                "T03C0.0400\n" +
                "%\n" +
                "T01\n" +
                "X4116Y4667\n" +
                "T02\n" +
                "X9374Y2651\n" +
                "T03\n" +
                "X7624Y3651\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(new RealNumber("10.45464"), new RealNumber("11.85418")), points.get(0).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(0).getToolDiameter());

        assertEquals(new Point(new RealNumber("23.80996"), new RealNumber("6.73354")), points.get(1).getPoint());
        assertEquals(new RealNumber("0.9"), points.get(1).getToolDiameter());

        assertEquals(new Point(new RealNumber("19.36496"), new RealNumber("9.27354")), points.get(2).getPoint());
        assertEquals(new RealNumber("1"), points.get(2).getToolDiameter());
    }

}
