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
import org.cirqwizard.appertures.macro.*;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.math.RealNumber;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GerberMacroTest
{
    @Test
    public void testMacroCenterLine() throws IOException
    {
        String fileContent = "%FSLAX35Y35*%\n" +
                "%MOIN*%\n" +
                "%IN2=Facesoudure:cuivreL1(X.Bot)*%\n" +
                "%AMR_17*21,1,0.06102,0.02362,0,0,180.000*%\n" +
                "%ADD17R_17*%\n" +
                "G54D17*\n" +
                "X34379Y474355D03*\n" +
                "M02*";


        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroCenterLine.class, macro.getPrimitives().get(0).getClass());
        MacroCenterLine centerLine = (MacroCenterLine) macro.getPrimitives().get(0);
        assertEquals(new RealNumber("1.549908"), centerLine.getWidth());
        assertEquals(new RealNumber("0.599948"), centerLine.getHeight());
        assertEquals(new Point(new RealNumber("0"), new RealNumber("0")), centerLine.getCenter());
        assertEquals(new RealNumber("180"), centerLine.getRotationAngle());

        assertEquals(new Point(new RealNumber("8.732266"), new RealNumber("120.48617")), f.getPoint());
    }

    @Test
    public void testMacroOutline() throws IOException
    {
        String fileContent = "%FSLAX35Y35*%\n" +
                "%MOIN*%\n" +
                "%IN2=Facesoudure:cuivreL1(X.Bot)*%\n" +
                "%AMOCT_18*4,1,8,0.035433,0.017717,0.017717,0.035433,-0.017717,0.035433,-0.035433,0.017717,-0.035433,-0.017717,-0.017717,-0.035433,0.017717,-0.035433,0.035433,-0.017717,0.035433,0.017717,0.000*%\n" +
                "%ADD18OCT_18*%\n" +
                "G54D18*\n" +
                "X19375Y411230D03*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroOutline.class, macro.getPrimitives().get(0).getClass());

        MacroOutline outline = (MacroOutline)macro.getPrimitives().get(0);
        assertEquals(8, outline.getPoints().size());

        assertEquals(new Point(new RealNumber("0.8999982"), new RealNumber("0.4500118")), outline.getPoints().get(0));
        assertEquals(new Point(new RealNumber("0.4500118"), new RealNumber("0.8999982")), outline.getPoints().get(1));
        assertEquals(new Point(new RealNumber("-0.4500118"), new RealNumber("0.8999982")), outline.getPoints().get(2));
        assertEquals(new Point(new RealNumber("-0.8999982"), new RealNumber("0.4500118")), outline.getPoints().get(3));
        assertEquals(new Point(new RealNumber("-0.8999982"), new RealNumber("-0.4500118")), outline.getPoints().get(4));
        assertEquals(new Point(new RealNumber("-0.4500118"), new RealNumber("-0.8999982")), outline.getPoints().get(5));
        assertEquals(new Point(new RealNumber("0.4500118"), new RealNumber("-0.8999982")), outline.getPoints().get(6));
        assertEquals(new Point(new RealNumber("0.8999982"), new RealNumber("-0.4500118")), outline.getPoints().get(7));

        assertEquals(new RealNumber("0"), outline.getRotationAngle());

        assertEquals(new Point(new RealNumber("4.92125"), new RealNumber("104.45242")), f.getPoint());
    }

    @Test
    public void testMacroVectorLine() throws IOException
    {
        String fileContent = "%FSLAX35Y35*%\n" +
                "%MOIN*%\n" +
                "%IN2=Facesoudure:cuivreL1(X.Bot)*%\n" +
                "%AMO_20*20,1,0.02362,-0.01870,0.00000,0.01870,0.00000,0*%\n" +
                "%ADD20O_20*%\n" +
                "G54D20*\n" +
                "X34379Y469355D03*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroVectorLine.class, macro.getPrimitives().get(0).getClass());
        MacroVectorLine vectorLine = (MacroVectorLine) macro.getPrimitives().get(0);
        assertEquals(new RealNumber("0.599948"), vectorLine.getWidth());
        assertEquals(new Point(new RealNumber("-0.47498"), new RealNumber(0)), vectorLine.getStart());
        assertEquals(new Point(new RealNumber("0.47498"), new RealNumber(0)), vectorLine.getEnd());
        assertEquals(new RealNumber(0), vectorLine.getRotationAngle());

        assertEquals(new Point(new RealNumber("8.732266"), new RealNumber("119.21617")), f.getPoint());
    }

    @Test
    public void testMacroCircle() throws IOException
    {
        String fileContent = "%FSLAX35Y35*%\n" +
                "%MOIN*%\n" +
                "%IN2=Facesoudure:cuivreL1(X.Bot)*%\n" +
                "%AMO_20*1,1,0.02362,-0.01870,0.00000*%\n" +
                "%ADD20O_20*%\n" +
                "G54D20*\n" +
                "X34379Y469355D03*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        ArrayList<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroCircle.class, macro.getPrimitives().get(0).getClass());
        MacroCircle circle = (MacroCircle) macro.getPrimitives().get(0);
        assertEquals(new RealNumber("0.599948"), circle.getDiameter());
        assertEquals(new Point(new RealNumber("-0.47498"), new RealNumber(0)), circle.getCenter());

        assertEquals(new Point(new RealNumber("8.732266"), new RealNumber("119.21617")), f.getPoint());
    }

}
