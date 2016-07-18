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
import org.cirqwizard.generation.toolpath.DrillPoint;
import org.cirqwizard.geom.Point;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;


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
        List<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(10454, 11854), points.get(0).getPoint());
        assertEquals(600, points.get(0).getToolDiameter());

        assertEquals(new Point(23809, 6733), points.get(1).getPoint());
        assertEquals(900, points.get(1).getToolDiameter());

        assertEquals(new Point(19364, 9273), points.get(2).getPoint());
        assertEquals(1000, points.get(2).getToolDiameter());
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
        List<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(70200, -149900), points.get(0).getPoint());
        assertEquals(300, points.get(0).getToolDiameter());

        assertEquals(new Point(93499, -124000), points.get(1).getPoint());
        assertEquals(600, points.get(1).getToolDiameter());

        assertEquals(new Point(75498, -125229), points.get(2).getPoint());
        assertEquals(800, points.get(2).getToolDiameter());
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
        List<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(22860, 13335), points.get(0).getPoint());
        assertEquals(600, points.get(0).getToolDiameter());

        assertEquals(new Point(17780, 2540), points.get(1).getPoint());
        assertEquals(900, points.get(1).getToolDiameter());

        assertEquals(new Point(11430, 2540), points.get(2).getPoint());
        assertEquals(1000, points.get(2).getToolDiameter());
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
        List<DrillPoint> points = parser.parse();
        assertEquals(4, points.size());

        assertEquals(new Point(77464, 13103), points.get(0).getPoint());
        assertEquals(600, points.get(0).getToolDiameter());

        assertEquals(new Point(77464, 90441), points.get(1).getPoint());
        assertEquals(600, points.get(1).getToolDiameter());

        assertEquals(new Point(10238, 998), points.get(2).getPoint());
        assertEquals(600, points.get(2).getToolDiameter());

        assertEquals(new Point(10238, 5986), points.get(3).getPoint());
        assertEquals(600, points.get(3).getToolDiameter());
    }

    @Test
    public void testUltiboardFile() throws IOException
    {
        String fileContent = "M48\n" +
                "INCH,TZ\n" +
                "VER,1\n" +
                "FMAT,2\n" +
                "DETECT,ON\n" +
                "ATC,ON\n" +
                "T1C0.02362\n" +
                "T2C0.05118\n" +
                "T3C0.03500\n" +
                "T4C0.03917\n" +
                "T5C0.15748\n" +
                "T6C0.04331\n" +
                "T7C0.03937\n" +
                "T8C0.03150\n" +
                "%\n" +
                "T1\n" +
                "X5.05000Y2.69000\n";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(128270, 68326), points.get(0).getPoint());
        assertEquals(600, points.get(0).getToolDiameter());
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
        List<DrillPoint> points = parser.parse();
        assertEquals(2, points.size());

        assertEquals(new Point(12320, 37393), points.get(0).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());

        assertEquals(new Point(12320, 39933), points.get(1).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());
    }

    @Test
    public void testMetricCoordinatesNoZeroInfo() throws IOException
    {
        String fileContent = "M48\n" +
                "METRIC\n" +
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
        List<DrillPoint> points = parser.parse();
        assertEquals(2, points.size());

        assertEquals(new Point(12320, 37393), points.get(0).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());

        assertEquals(new Point(12320, 39933), points.get(1).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());
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
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(59436, 36703), points.get(0).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());
    }

    @Test
    public void testModalCoordinates() throws IOException
    {
        String fileContent = "M48\n" +
                ";Layer_Color=9474304\n" +
                ";FILE_FORMAT=2:5\n" +
                "INCH\n" +
                ";TYPE=PLATED\n" +
                "T1F00S00C0.03543\n" +
                ";TYPE=NON_PLATED\n" +
                "%\n" +
                "T01\n" +
                "X023400Y014450\n" +
                "X024400\n" +
                "Y017450\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(3, points.size());

        assertEquals(new Point(59436, 36703), points.get(0).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());

        assertEquals(new Point(61976, 36703), points.get(1).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());

        assertEquals(new Point(61976, 44323), points.get(2).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());
    }

    @Test
    public void testUndefinedTool() throws IOException
    {
        String fileContent = "%\n" +
                "T3\n" +
                "X00250Y05025\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(635, 12763), points.get(0).getPoint());
        assertEquals(1300, points.get(0).getToolDiameter());
    }

    @Test
    public void testEmptyLines() throws IOException
    {
        String fileContent = "\n%\n" +
                "T3\n" +
                "X00250Y05025\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(635, 12763), points.get(0).getPoint());
        assertEquals(1300, points.get(0).getToolDiameter());
    }

    @Test
    public void testAllegroFile() throws IOException
    {
        String fileContent = "M48\n" +
                "INCH,TZ\n" +
                "T01C.013\n" +
                "T02C.036\n" +
                "T03C.042\n" +
                "T04C.043\n" +
                ";LEADER: 12 \n" +
                ";HEADER: \n" +
                ";CODE  : ASCII \n" +
                "%\n" +
                "G90\n" +
                "T02\n" +
                "X530000Y510000\n" +
                "R04Y10000\n" +
                "X500000\n" +
                "R04Y-10000";

        ExcellonParser parser = new ExcellonParser(2, 5, ExcellonParser.INCHES_MM_RATIO, new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(10, points.size());

        assertEquals(new Point(134620, 129540), points.get(0).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());
        assertEquals(new Point(134620, 132080), points.get(1).getPoint());
        assertEquals(900, points.get(1).getToolDiameter());
        assertEquals(new Point(134620, 134620), points.get(2).getPoint());
        assertEquals(900, points.get(2).getToolDiameter());
        assertEquals(new Point(134620, 137160), points.get(3).getPoint());
        assertEquals(900, points.get(3).getToolDiameter());
        assertEquals(new Point(134620, 139700), points.get(4).getPoint());
        assertEquals(900, points.get(4).getToolDiameter());
        assertEquals(new Point(127000, 139700), points.get(5).getPoint());
        assertEquals(900, points.get(5).getToolDiameter());
        assertEquals(new Point(127000, 137160), points.get(6).getPoint());
        assertEquals(900, points.get(6).getToolDiameter());
        assertEquals(new Point(127000, 134620), points.get(7).getPoint());
        assertEquals(900, points.get(7).getToolDiameter());
        assertEquals(new Point(127000, 132080), points.get(8).getPoint());
        assertEquals(900, points.get(8).getToolDiameter());
        assertEquals(new Point(127000, 129540), points.get(9).getPoint());
        assertEquals(900, points.get(9).getToolDiameter());
    }

    @Test
    public void testNoToolSelected() throws IOException
    {
        String fileContent = "%\n" +
                "G90\n" +
                "X0Y0\n";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(0, 0), points.get(0).getPoint());
        assertEquals(0, points.get(0).getToolDiameter());
    }

    @Test
    public void testDipTrace() throws IOException
    {
        String fileContent = "M48\n" +
                "INCH\n" +
                "T01C0.0191\n" +
                "%\n" +
                "T01\n" +
                "X+009070Y+030709\n" +
                "T00\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(23037, 78000), points.get(0).getPoint());
        assertEquals(500, points.get(0).getToolDiameter());
    }

    @Test
    public void testLeadingZeroes() throws IOException
    {
        String fileContent = "M48\n" +
                "INCH,LZ\n" +
                "VER,1\n" +
                "FMAT,2\n" +
                "T01C0.0354F5S14\n" +
                "T02C0.0370F5S14\n" +
                "DETECT,ON\n" +
                "ATC,ON\n" +
                "%\n" +
                "G90\n" +
                "T01\n" +
                "X014491Y02111";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(36807, 53619), points.get(0).getPoint());
        assertEquals(900, points.get(0).getToolDiameter());
    }

    @Test
    public void testNoDecimalPart() throws IOException
    {
        String fileContent = "M48\n" +
                ";DRILL file {KiCad 4.0.2+e4-6225~38~ubuntu16.04.1-stable} date dom 10 jul 2016 23:16:58 CEST\n" +
                ";FORMAT={-:-/ absolute / inch / decimal}\n" +
                "FMAT,2\n" +
                "INCH,TZ\n" +
                "T6C0.039\n" +
                "%\n" +
                "G90\n" +
                "G05\n" +
                "M72\n" +
                "T6\n" +
                "X2.Y-2.85\n";

        ExcellonParser parser = new ExcellonParser(new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(50800, -72390), points.get(0).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());
    }

    @Test
    public void testTrailingZeros() throws IOException
    {
        String fileContent = "%\n" +
                "M48\n" +
                "M72\n" +
                "T01C0.0394\n" +
                "%\n" +
                "T01\n" +
                "X947Y20239\n" +
                "M30";

        ExcellonParser parser = new ExcellonParser(2, 4, new BigDecimal(25400), new StringReader(fileContent));
        List<DrillPoint> points = parser.parse();
        assertEquals(1, points.size());

        assertEquals(new Point(2405, 51407), points.get(0).getPoint());
        assertEquals(1000, points.get(0).getToolDiameter());
    }

}
