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

    @Test
    public void testKiCADFile() throws IOException
    {
        String fileContent = "M48\n" +
                "INCH,TZ\n" +
                "T1C0.013\n" +
                "T2C0.025\n" +
                "T3C0.032\n" +
                "%\n" +
                "G90\n" +
                "G05\n" +
                "T1\n" +
                "X027638Y-059016\n" +
                "T2\n" +
                "X036811Y-048819\n" +
                "T3\n" +
                "X029724Y-049303\n" +
                "T0\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(new RealNumber("70.20052"), new RealNumber("-149.90064")), points.get(0).getPoint());
        assertEquals(new RealNumber("0.3"), points.get(0).getToolDiameter());

        assertEquals(new Point(new RealNumber("93.49994"), new RealNumber("-124.00026")), points.get(1).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(1).getToolDiameter());

        assertEquals(new Point(new RealNumber("75.49896"), new RealNumber("-125.22962")), points.get(2).getPoint());
        assertEquals(new RealNumber("0.8"), points.get(2).getToolDiameter());
    }

    @Test
    public void testOrCADFile() throws IOException
    {
        String fileContent = "%\n" +
                "T2C0.0236F200S100\n" +
                "X009000Y005250\n" +
                "T3C0.0354F200S100\n" +
                "X007000Y001000\n" +
                "T1C0.0400F200S100\n" +
                "X004500Y001000\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(new RealNumber("22.86"), new RealNumber("13.335")), points.get(0).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(0).getToolDiameter());

        assertEquals(new Point(new RealNumber("17.78"), new RealNumber("2.54")), points.get(1).getPoint());
        assertEquals(new RealNumber("0.9"), points.get(1).getToolDiameter());

        assertEquals(new Point(new RealNumber("11.43"), new RealNumber("2.54")), points.get(2).getPoint());
        assertEquals(new RealNumber("1"), points.get(2).getToolDiameter());
    }

    @Test
    public void testDesignSparkFile() throws IOException
    {
        String fileContent = "G81\n" +
                "M48\n" +
                "INCH,LZ,00.000\n" +
                "T1C00.025\n" +
                "%\n" +
                "T001\n" +
                "G00X00050Y05159\n" +
                "M15\n" +
                "G01X30498Y05159\n" +
                "X30498Y35607\n" +
                "M17\n" +
                "G00X02066Y00393\n" +
                "M15\n" +
                "G01X04031Y00393\n" +
                "X04031Y02357\n" +
                "M17\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(4, points.size());

        assertEquals(new Point(new RealNumber("77.46492"), new RealNumber("13.10386")), points.get(0).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(0).getToolDiameter());

        assertEquals(new Point(new RealNumber("77.46492"), new RealNumber("90.44178")), points.get(1).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(1).getToolDiameter());

        assertEquals(new Point(new RealNumber("10.23874"), new RealNumber("0.99822")), points.get(2).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(2).getToolDiameter());

        assertEquals(new Point(new RealNumber("10.23874"), new RealNumber("5.98678")), points.get(3).getPoint());
        assertEquals(new RealNumber("0.6"), points.get(3).getToolDiameter());
    }

    @Test
    public void testMetricCoordinates() throws IOException
    {
        String fileContent = "M48\n" +
                "METRIC,TZ\n" +
                "FMAT,1\n" +
                "ICI,OFF\n" +
                "T01C1.00076F085S1\n" +
                "%\n" +
                "T01\n" +
                "G81\n" +
                "X0123209Y0373930\n" +
                "X0123209Y0399330\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(2, points.size());

        assertEquals(new Point(new RealNumber("12.3209"), new RealNumber("37.393")), points.get(0).getPoint());
        assertEquals(new RealNumber("1"), points.get(0).getToolDiameter());

        assertEquals(new Point(new RealNumber("12.3209"), new RealNumber("39.933")), points.get(1).getPoint());
        assertEquals(new RealNumber("1"), points.get(0).getToolDiameter());
    }

    @Test
    public void testCQ49() throws IOException
    {
        String fileContent = "M48\n" +
                ";Layer_Color=9474304\n" +
                ";FILE_FORMAT=2:4\n" +
                "INCH\n" +
                ";TYPE=PLATED\n" +
                "T1F00S00C0.03543\n" +
                ";TYPE=NON_PLATED\n" +
                "%\n" +
                "T01\n" +
                "X023400Y014450\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        ArrayList<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(new RealNumber("59.436"), new RealNumber("36.703")), points.get(0).getPoint());
        assertEquals(new RealNumber("0.9"), points.get(0).getToolDiameter());
    }

}
