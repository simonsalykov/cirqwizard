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
        assertEquals(new Point(7680, 2670), p.getPoint());
        assertEquals(0, p.getAngle());
        assertEquals("C1", p.getName());

        p = points.get(1);
        assertEquals(new ComponentId("SMD_0603", "100nF"), p.getId());
        assertEquals(new Point(26100, 16640), p.getPoint());
        assertEquals(270000, p.getAngle());
        assertEquals("C5", p.getName());

        p = points.get(2);
        assertEquals(new ComponentId("SMD_0805", ""), p.getId());
        assertEquals(new Point(26100, 16640), p.getPoint());
        assertEquals(270000, p.getAngle());
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
        assertEquals(new Point(29972, 56388), p.getPoint());
        assertEquals(180000, p.getAngle());
        assertEquals("R1", p.getName());
    }

    @Test
    public void testAltium() throws IOException
    {
        String fileContent = "Designator Footprint               Mid X         Mid Y         Ref X         Ref Y         Pad X         Pad Y TB      Rotation Comment        \n" +
                "\n" +
                "C11        CAP-0805           124.0028mm     65.4342mm    124.0028mm     65.4342mm    122.9028mm     65.4342mm  B        360.00 100nF          \n" +
                "C11        CAP-0805           -124.0028mm     -65.4342mm    -124.0028mm     -65.4342mm    -122.9028mm     -65.4342mm  B        -360.00 100nF          ";
        String regex = "(?<name>\\S+)\\s+(?<package>\\S+)\\s+(?<x>-?\\d+.?\\d*)mm\\s+(?<y>-?\\d+.?\\d*)mm\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(?<angle>-?\\d+.\\d*)\\s+(?<value>\\S+)\\s*";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(2, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("CAP-0805", "100nF"), p.getId());
        assertEquals(new Point(124002, 65434), p.getPoint());
        assertEquals(360000, p.getAngle());
        assertEquals("C11", p.getName());

        p = points.get(1);
        assertEquals(new ComponentId("CAP-0805", "100nF"), p.getId());
        assertEquals(new Point(-124002, -65434), p.getPoint());
        assertEquals(-360000, p.getAngle());
        assertEquals("C11", p.getName());

    }

    @Test
    public void testDesignSpark() throws IOException
    {
        String fileContent = "\"Name\",\"Component\",\"Side\",\"Centre X\",\"Centre Y\",\"Rotation\"\n" +
                "\"U3\",\"BSS84\",\"Top\",\"220.7\",\"271.0\",\"90.00\"";
        String regex = "\"(?<name>\\S+)\",\"(?<package>\\S+)\",\"Top\",\"(?<x>\\d+.?\\d*)\",\"(?<y>\\d+.?\\d*)\",\"(?<angle>\\d+.?\\d*)\"(?<value>.*)";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(1, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("BSS84", ""), p.getId());
        assertEquals(new Point(220700, 271000), p.getPoint());
        assertEquals(90000, p.getAngle());
        assertEquals("U3", p.getName());

    }

    @Test
    public void testKiCAD() throws IOException
    {
        String fileContent = "Ref,Side,Val,Package,PosX,PosY,Rot\n" +
                "C1,F.Cu,100 nF,R_0805,104.673,-81.432,90\n";
        String regex = "(?<name>\\S+),.*,(?<value>.+?),(?<package>\\S+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*)";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(1, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("R_0805", "100 nF"), p.getId());
        assertEquals(new Point(104673, -81432), p.getPoint());
        assertEquals(90000, p.getAngle());
        assertEquals("C1", p.getName());
    }

    @Test
    public void testDipTrace() throws IOException
    {
        String fileContent = "RefDes,Name,X (mm),Y (mm),Side,Rotate,Value\n" +
                "C1,CAP_0603,47.72,18.42,Top,270,2.2uF";
        String regex= "(?<name>\\S+),(?<package>.+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),Top,(?<angle>\\d+.?\\d*),(?<value>\\S*)";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(1, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("CAP_0603", "2.2uF"), p.getId());
        assertEquals(new Point(47720, 18420), p.getPoint());
        assertEquals(270000, p.getAngle());
        assertEquals("C1", p.getName());
    }


    @Test
    public void testEasyPC() throws IOException
    {
        String fileContent = "name,x,y,angle,value,package\n" +
                "C1,40.147,19.883,270.00,100nF,CAP-0805-100nF-10%-X7R\n";
        String regex= "(?<name>\\S+),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*),(?<value>\\S*),(?<package>.*)";

        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(1, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("CAP-0805-100nF-10%-X7R", "100nF"), p.getId());
        assertEquals(new Point(40147, 19883), p.getPoint());
        assertEquals(270000, p.getAngle());
        assertEquals("C1", p.getName());
    }

    @Test
    public void testProteus() throws IOException
    {
        String fileContent = "LABCENTER PROTEUS PICK AND PLACE FILE\n" +
                "=====================================\n" +
                "\n" +
                "\"R1\",\"15\",\"1206\",TOP,180,1548,90\n" +
                "\"R3\",\"15.4\",\"1206\",TOP,180,2190,90\n";
        String regex = "\"(?<name>\\S+)\",\"(?<value>\\S*)\",\"(?<package>.*)\",TOP,(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*)";
        PPParser parser = new PPParser(new StringReader(fileContent), regex);
        List<PPPoint> points = parser.parse();

        assertEquals(2, points.size());
        PPPoint p = points.get(0);
        assertEquals(new ComponentId("1206", "15"), p.getId());
        assertEquals(new Point(180000, 1548000), p.getPoint());
        assertEquals(90000, p.getAngle());
        assertEquals("R1", p.getName());
    }


}
