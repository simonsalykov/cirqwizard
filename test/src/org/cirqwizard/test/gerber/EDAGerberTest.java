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
import org.cirqwizard.geom.Arc;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.*;
import org.cirqwizard.math.RealNumber;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EDAGerberTest
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

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.254"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("14.1986"), new RealNumber("38.5064")), l.getFrom());
        assertEquals(new Point(new RealNumber("14.1986"), new RealNumber("36.4236")), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.254"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("16.2814"), new RealNumber("36.4236")), l.getFrom());
        assertEquals(new Point(new RealNumber("14.1986"), new RealNumber("36.4236")), l.getTo());

        p = elements.get(2);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.27"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("1.524"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("49.784"), new RealNumber("59.055")), f.getPoint());

        p = elements.get(3);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.27"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("1.524"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("46.736"), new RealNumber("59.055")), f.getPoint());

        p = elements.get(4);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.524"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("88.265"), new RealNumber("59.055")), f.getPoint());

        p = elements.get(5);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.524"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("88.265"), new RealNumber("46.355")), f.getPoint());

    }

    @Test
    public void testKiCAD() throws IOException
    {
        String fileContent = "G04 (created by PCBNEW (2013-07-07 BZR 4022)-stable) date 23/01/2014 11:32:09*\n" +
                "%MOIN*%\n" +
                "G04 Gerber Fmt 3.4, Leading zero omitted, Abs format*\n" +
                "%FSLAX34Y34*%\n" +
                "G01*\n" +
                "G70*\n" +
                "G90*\n" +
                "G04 APERTURE LIST*\n" +
                "%ADD12C,0.055*%\n" +
                "%ADD13R,0.144X0.08*%\n" +
                "%ADD39C,0.012*%\n" +
                "G04 APERTURE END LIST*\n" +
                "G54D12*\n" +
                "X29724Y-52649D03*\n" +
                "G54D13*\n" +
                "X34842Y-57796D03*\n" +
                "G54D39*\n" +
                "X30905Y-49428D02*\n" +
                "X30905Y-49094D01*\n" +
                "X31023Y-47755D02*\n" +
                "X30433Y-47755D01*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(4, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(CircularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.397"), ((CircularAperture)f.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("75.49896"), new RealNumber("-133.72846")), f.getPoint());

        p = elements.get(1);
        assertEquals(Flash.class, p.getClass());
        f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("3.6576"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("2.032"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("88.49868"), new RealNumber("-146.80184")), f.getPoint());

        p = elements.get(2);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.3048"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("78.4987"), new RealNumber("-125.54712")), l.getFrom());
        assertEquals(new Point(new RealNumber("78.4987"), new RealNumber("-124.69876")), l.getTo());

        p = elements.get(3);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.3048"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("78.79842"), new RealNumber("-121.2977")), l.getFrom());
        assertEquals(new Point(new RealNumber("77.29982"), new RealNumber("-121.2977")), l.getTo());

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
        List<GerberPrimitive> s = r.getSegments();
        assertEquals(4, r.getSegments().size());
        assertEquals(new RealNumber("5.34"), r.getMin().getX());
        assertEquals(new RealNumber("8.22"), r.getMin().getY());
        LinearShape l = (LinearShape) s.get(0);
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.52")), l.getFrom());
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.22")), l.getTo());
        l = (LinearShape) s.get(1);
        assertEquals(new Point(new RealNumber("6.54"), new RealNumber("8.22")), l.getFrom());
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.22")), l.getTo());
        l = (LinearShape) s.get(2);
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.22")), l.getFrom());
        assertEquals(new Point(new RealNumber("5.34"), new RealNumber("8.52")), l.getTo());
        l = (LinearShape) s.get(3);
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

    @Test
    public void testProteusFile() throws IOException
    {
        String fileContent = "G04 PROTEUS RS274X GERBER FILE*\n" +
                "%FSLAX24Y24*%\n" +
                "%MOIN*%\n" +
                "%ADD11C,0.0080*%\n" +
                "G54D11*\n" +
                "X+3077Y-16191D02*\n" +
                "X+8457Y-16191D01*\n" +
                "X-44265Y+11501D01*\n" +
                "M00*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(2, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.2032"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("7.81558"), new RealNumber("-41.12514")), l.getFrom());
        assertEquals(new Point(new RealNumber("21.48078"), new RealNumber("-41.12514")), l.getTo());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.2032"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("21.48078"), new RealNumber("-41.12514")), l.getFrom());
        assertEquals(new Point(new RealNumber("-112.4331"), new RealNumber("29.21254")), l.getTo());

    }

    @Test
    public void testEasyPCFile() throws IOException
    {
        String fileContent = "%FSLAX23Y23*%\n" +
                "%MOIN*%\n" +
                "G04 EasyPC Gerber Version 17.0 Build 3379 *\n" +
                "%ADD102R,0.06000X0.06000*%\n" +
                "%ADD27C,0.00800*%\n" +
                "%ADD17R,0.07800X0.02200*%\n" +
                "%ADD114R,0.12598X0.03937*%\n" +
                "%ADD115R,0.06890X0.05906*%\n" +
                "%ADD103R,0.07090X0.06300*%\n" +
                "%ADD113C,0.01654*%\n" +
                "%ADD112C,0.05906*%\n" +
                "%ADD116C,0.06299*%\n" +
                "X0Y0D02*\n" +
                "D02*\n" +
                "D17*\n" +
                "X353Y228D03*\n" +
                "D113*\n" +
                "X203Y186D02*\n" +
                "G75*\n" +
                "G02X224Y224I81J-22D01*\n" +
                "G01*\n" +
                "X181*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(3, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(RectangularAperture.class, f.getAperture().getClass());
        assertEquals(new RealNumber("1.9812"), ((RectangularAperture)f.getAperture()).getDimensions()[0]);
        assertEquals(new RealNumber("0.5588"), ((RectangularAperture)f.getAperture()).getDimensions()[1]);
        assertEquals(new Point(new RealNumber("8.9662"), new RealNumber("5.7912")), f.getPoint());

        p = elements.get(1);
        assertEquals(CircularShape.class, p.getClass());
        CircularShape c = (CircularShape) p;
        Arc arc = c.getArc();
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("5.1562"), new RealNumber("4.7244")), arc.getFrom());
        assertEquals(new Point(new RealNumber("5.6896"), new RealNumber("5.6896")), arc.getTo());
        assertEquals(new Point(new RealNumber("7.2136"), new RealNumber("4.1656")), arc.getCenter());
        assertEquals(true, arc.isClockwise());

        p = elements.get(2);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("5.6896"), new RealNumber("5.6896")), l.getFrom());
        assertEquals(new Point(new RealNumber("4.5974"), new RealNumber("5.6896")), l.getTo());
    }

    @Test
    public void testEasyPC2File() throws IOException
    {
        String fileContent = "%FSLAX23Y23*%\n" +
                "%MOIN*%\n" +
                "G04 EasyPC Gerber Version 17.0 Build 3379 *\n" +
                "%ADD102R,0.06000X0.06000*%\n" +
                "%ADD27C,0.00800*%\n" +
                "%ADD17R,0.07800X0.02200*%\n" +
                "%ADD114R,0.12598X0.03937*%\n" +
                "%ADD115R,0.06890X0.05906*%\n" +
                "%ADD103R,0.07090X0.06300*%\n" +
                "%ADD113C,0.01654*%\n" +
                "%ADD112C,0.05906*%\n" +
                "%ADD116C,0.06299*%\n" +
                "D113*\n" +
                "X203Y186D02*\n" +
                "G75*\n" +
                "G02X224Y224I81J-22D01*\n" +
                "G01*\n" +
                "X181*\n" +
                "G75*\n" +
                "G02X203Y186I-59J-59*\n" +
                "G01*\n" +
                "G36*\n" +
                "G75*\n" +
                "G02X224Y224I81J-22*\n" +
                "G01*\n" +
                "X181*\n" +
                "G75*\n" +
                "G02X203Y186I-59J-59*\n" +
                "G01*\n" +
                "G37*\n" +
                "Y654D02*\n" +
                "X254Y602D01*\n" +
                "M02*\n";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(5, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(CircularShape.class, p.getClass());
        CircularShape c = (CircularShape) p;
        Arc arc = c.getArc();
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("5.1562"), new RealNumber("4.7244")), arc.getFrom());
        assertEquals(new Point(new RealNumber("5.6896"), new RealNumber("5.6896")), arc.getTo());
        assertEquals(new Point(new RealNumber("7.2136"), new RealNumber("4.1656")), arc.getCenter());
        assertEquals(true, arc.isClockwise());

        p = elements.get(1);
        assertEquals(LinearShape.class, p.getClass());
        LinearShape l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("5.6896"), new RealNumber("5.6896")), l.getFrom());
        assertEquals(new Point(new RealNumber("4.5974"), new RealNumber("5.6896")), l.getTo());

        p = elements.get(2);
        assertEquals(CircularShape.class, p.getClass());
        c = (CircularShape) p;
        arc = c.getArc();
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("4.5974"), new RealNumber("5.6896")), arc.getFrom());
        assertEquals(new Point(new RealNumber("5.1562"), new RealNumber("4.7244")), arc.getTo());
        assertEquals(new Point(new RealNumber("3.0988"), new RealNumber("4.191")), arc.getCenter());
        assertEquals(true, arc.isClockwise());

        p = elements.get(3);
        assertEquals(Region.class, p.getClass());

        Region region = (Region) p;
        assertEquals(3, region.getSegments().size());

        p = elements.get(4);
        assertEquals(LinearShape.class, p.getClass());
        l = (LinearShape) p;
        assertEquals(CircularAperture.class, p.getAperture().getClass());
        assertEquals(new RealNumber("0.420116"), ((CircularAperture)p.getAperture()).getDiameter());
        assertEquals(new Point(new RealNumber("5.1562"), new RealNumber("16.6116")), l.getFrom());
        assertEquals(new Point(new RealNumber("6.4516"), new RealNumber("15.2908")), l.getTo());


    }
}
