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


package org.cirqwizard.test.gerber;

import org.cirqwizard.GerberParser;
import org.cirqwizard.appertures.CircularAperture;
import org.cirqwizard.appertures.RectangularAperture;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.gerber.Region;
import org.cirqwizard.math.RealNumber;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GerberParserTest
{

    @Test
    public void testEagleFile() throws IOException
    {
        String fileContent = "G75*\n" +
                "G70*\n" +
                "%OFA0B0*%\n" +
                "%FSLAX24Y24*%\n" +
                "%IPPOS*%\n" +
                "%LPD*%\n" +
                "%AMOC8*\n" +
                "5,1,8,0,0,1.08239X$1,22.5*\n" +
                "%\n" +
                "%ADD10C,0.0000*%\n" +
                "%ADD11R,0.0591X0.0197*%\n" +
                "%ADD16C,0.0740*%\n" +
                "%ADD22C,0.0236*%\n" +
                "D10*\n" +
                "X000100Y000100D02*\n" +
                "X000100Y012305D01*\n" +
                "X012108Y012305D01*\n" +
                "D11*\n" +
                "X006181Y005549D03*\n" +
                "X006181Y006179D03*\n" +
                "D16*\n" +
                "X003624Y010901D03*\n" +
                "X002624Y010901D03*\n" +
                "D22*\n" +
                "X004594Y008561D02*\n" +
                "X003214Y008561D01*\n" +
                "X002874Y008901D01*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(8, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber(0), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("0.254"), new RealNumber("0.254")), l.getFrom());
        assertEquals(new Point(new RealNumber("0.254"), new RealNumber("31.2547")), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber(0), p.getAperture().getWidth(new RealNumber(0)));
        assertEquals(new Point(new RealNumber("0.254"), new RealNumber("31.2547")), l.getFrom());
        assertEquals(new Point(new RealNumber("30.75432"), new RealNumber("31.2547")), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.50114"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("0.50038"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("15.69974"), new RealNumber("14.09446")), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.50114"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("0.50038"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("15.69974"), new RealNumber("15.69466")), f.getPoint());

        p = elements.get(4);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.8796"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("9.20496"), new RealNumber("27.68854")), f.getPoint());

        p = elements.get(5);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.8796"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("6.66496"), new RealNumber("27.68854")), f.getPoint());

        p = elements.get(6);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.59944"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("11.66876"), new RealNumber("21.74494")), l.getFrom());
        assertEquals(new Point(new RealNumber("8.16356"), new RealNumber("21.74494")), l.getTo());

        p = elements.get(7);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.59944"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("8.16356"), new RealNumber("21.74494")), l.getFrom());
        assertEquals(new Point(new RealNumber("7.29996"), new RealNumber("22.60854")), l.getTo());
    }

    @Test
    public void testOrCADFile() throws IOException
    {
        String fileContent = "*\n" +
                "G04 Mass Parameters ***\n" +
                "*\n" +
                "G04 Image ***\n" +
                "*\n" +
                "%IND:\\FILENAME*%\n" +
                "%ICAS*%\n" +
                "%MOIN*%\n" +
                "%IPPOS*%\n" +
                "%ASAXBY*%\n" +
                "G74*%FSLAN2X34Y34*%\n" +
                "*\n" +
                "G04 Aperture Definitions ***\n" +
                "*\n" +
                "%ADD10R,0.0500X0.0600*%\n" +
                "%ADD16C,0.0600*%\n" +
                "%ADD25C,0.0100*%\n" +
                "%ADD26C,0.0140*%\n" +
                "*\n" +
                "G04 Plot Data ***\n" +
                "*\n" +
                "G54D25*\n" +
                 "G01X0005590Y0015160D02*\n" +
                "Y0014340D01*\n" +
                "X0006410D02*\n" +
                "X0005590D01*\n" +
                "G54D10*\n" +
                "X0019600Y0023250D03*\n" +
                "X0018400D03*\n" +
                "G54D16*\n" +
                "X0034750Y0023250D03*\n" +
                "Y0018250D03*\n" +
                "%LPD*%\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(6, elements.size());
    }

    @Test
    public void testSprintLayoutFile() throws IOException
    {
        String fileContent = "%FSLAX32Y32*%\n" +
                "%MOMM*%\n" +
                "%LNKUPFERSEITE2*%\n" +
                "G71*\n" +
                "G01*\n" +
                "%ADD10C, 0.25*%\n" +
                "%ADD11C, 1.80*%\n" +
                "%ADD12C, 2.00*%\n" +
                "%LPD*%\n" +
                "G36*\n" +
                "X654Y852D02*\n" +
                "X654Y822D01*\n" +
                "X534Y822D01*\n" +
                "X534Y852D01*\n" +
                "X654Y852D01*\n" +
                "G37*\n" +
                "G54D10*\n" +
                "X1474Y1163D02*\n" +
                "X1474Y1263D01*\n" +
                "G54D11*\n" +
                "D03*\n" +
                "X1103Y438D02*\n" +
                "G54D12*\n" +
                "D03*\n" +
                "X2126Y1233D02*\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Region.class, p.getClass());
        Region r = (Region) p;
        List<LinearShape> s = r.getSegments();
        assertEquals(4, r.getSegments().size());
        assertEquals(new RealNumber("5.34"), r.getMin().getX());
        assertEquals(new RealNumber("8.22"), r.getMin().getY());
        LinearShape l = s.get(0);
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.52")), l.getFrom());
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.22")), l.getTo());
        l = s.get(1);
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.22")), l.getFrom());
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.22")), l.getTo());
        l = s.get(2);
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.22")), l.getFrom());
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.52")), l.getTo());
        l = s.get(3);
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.52")), l.getFrom());
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.52")), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.25"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("14.74"), new RealNumber("11.63")), l.getFrom());
        assertEquals(new Point(new RealNumber("14.74"), new RealNumber("12.63")), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.80"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("14.74"), new RealNumber("12.63")), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("2.00"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("11.03"), new RealNumber("4.38")), f.getPoint());
    }

    @Test
    public void testDesignSparkFile() throws IOException
    {
        String fileContent = "%FSLAX23Y23*%\n" +
                "%MOMM*%\n" +
                "G04 EasyPC Gerber Version 16.0.6 Build 3249 *\n" +
                "%ADD23R,1.52400X1.52400*%\n" +
                "%ADD13R,1.87960X1.87960*%\n" +
                "%ADD14C,1.87960*%\n" +
                "X0Y0D02*\n" +
                "D02*\n" +
                "D13*\n" +
                "X17844Y25718D03*\n" +
                "D02*\n" +
                "D14*\n" +
                "X20384D02*\n" +
                "X22924D01*\n" +
                "D02*\n" +
                "D23*\n" +
                "X17844Y7049D03*\n" +
                "Y25464D03*\n" +
                "X0Y0D02*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.87960"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("1.87960"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("17.844"), new RealNumber("25.718")), f.getPoint());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("1.87960"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("20.384"), new RealNumber("25.718")), l.getFrom());
        assertEquals(new Point(new RealNumber("22.924"), new RealNumber("25.718")), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.52400"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("1.52400"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("17.844"), new RealNumber("7.049")), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.52400"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("1.52400"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("17.844"), new RealNumber("25.464")), f.getPoint());

    }
}
