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

import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.gerber.appertures.macro.*;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.GerberPrimitive;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroCenterLine.class, macro.getPrimitives().get(0).getClass());
        MacroCenterLine centerLine = (MacroCenterLine) macro.getPrimitives().get(0);
        assertEquals(1549, centerLine.getWidth());
        assertEquals(599, centerLine.getHeight());
        assertEquals(new Point(0, 0), centerLine.getCenter());
        assertEquals(180, centerLine.getRotationAngle());

        assertEquals(new Point(8732, 120486), f.getPoint());
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
        List<GerberPrimitive> elements = parser.parse();

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

        assertEquals(new Point(899, 450), outline.getPoints().get(0));
        assertEquals(new Point(450, 899), outline.getPoints().get(1));
        assertEquals(new Point(-450, 899), outline.getPoints().get(2));
        assertEquals(new Point(-899, 450), outline.getPoints().get(3));
        assertEquals(new Point(-899, -450), outline.getPoints().get(4));
        assertEquals(new Point(-450, -899), outline.getPoints().get(5));
        assertEquals(new Point(450, -899), outline.getPoints().get(6));
        assertEquals(new Point(899, -450), outline.getPoints().get(7));

        assertEquals(0, outline.getRotationAngle());

        assertEquals(new Point(4921, 104452), f.getPoint());
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
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroVectorLine.class, macro.getPrimitives().get(0).getClass());
        MacroVectorLine vectorLine = (MacroVectorLine) macro.getPrimitives().get(0);
        assertEquals(599, vectorLine.getWidth());
        assertEquals(new Point(-474, 0), vectorLine.getStart());
        assertEquals(new Point(474, 0), vectorLine.getEnd());
        assertEquals(0, vectorLine.getRotationAngle());

        assertEquals(new Point(8732, 119216), f.getPoint());
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
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroCircle.class, macro.getPrimitives().get(0).getClass());
        MacroCircle circle = (MacroCircle) macro.getPrimitives().get(0);
        assertEquals(599, circle.getDiameter());
        assertEquals(new Point(-474, 0), circle.getCenter());

        assertEquals(new Point(8732, 119216), f.getPoint());
    }

    @Test
    public void testOrcadMacroOutline() throws IOException
    {
        String fileContent = "%FSLAX25Y25*MOMM*%\n" +
                "%IR0*IPPOS*OFA0.00000B0.00000*MIA0B0*SFA1.00000B1.00000*%\n" +
                "%AMMACRO24*\n" +
                "4,1,8,-2.0574,.254,\n" +
                "-1.2446,.254,\n" +
                "-1.2446,.8636,\n" +
                "2.0574,.8636,\n" +
                "2.0574,-.8636,\n" +
                "-1.2446,-.8636,\n" +
                "-1.2446,-.254,\n" +
                "-2.0574,-.254,\n" +
                "-2.0574,.254,\n" +
                "0.0*\n" +
                "%\n" +
                "%ADD24MACRO24*%\n" +
                "G54D24*\n" +
                "X16673260Y13984100D03*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

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

        assertEquals(new Point(-2057, 254), outline.getPoints().get(0));
        assertEquals(new Point(-1244, 254), outline.getPoints().get(1));
        assertEquals(new Point(-1244, 863), outline.getPoints().get(2));
        assertEquals(new Point(2057, 863), outline.getPoints().get(3));
        assertEquals(new Point(2057, -863), outline.getPoints().get(4));
        assertEquals(new Point(-1244, -863), outline.getPoints().get(5));
        assertEquals(new Point(-1244, -254), outline.getPoints().get(6));
        assertEquals(new Point(-2057, -254), outline.getPoints().get(7));

        assertEquals(0, outline.getRotationAngle());

        assertEquals(new Point(166732, 139841), f.getPoint());
    }

    @Test
    public void testOracdMacroCenterLine() throws IOException
    {
        String fileContent = "%FSLAX25Y25*MOMM*%\n" +
                "%IR0*IPPOS*OFA0.00000B0.00000*MIA0B0*SFA1.00000B1.00000*%\n" +
                "%AMMACRO12*\n" +
                "21,1,2.,1.,0.0,0.0,90.172*%\n" +
                "%ADD12MACRO12*%\n" +
                "G54D12*\n" +
                "X15145520Y13085760D03*\n" +
                "M02*";


        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroCenterLine.class, macro.getPrimitives().get(0).getClass());
        MacroCenterLine centerLine = (MacroCenterLine) macro.getPrimitives().get(0);
        assertEquals(2000, centerLine.getWidth());
        assertEquals(1000, centerLine.getHeight());
        assertEquals(new Point(0, 0), centerLine.getCenter());
        assertEquals(90, centerLine.getRotationAngle());

        assertEquals(new Point(151455, 130857), f.getPoint());
    }

    @Test
    public void testMacroPolygon() throws IOException
    {
        String fileContent = "%MOIN*%\n" +
                "%AMOUTLINE1*5,1,4,0,0,0.121622,-225.0*%\n" +
                "%ADD87OUTLINE1*%\n" +
                "%FSLAX26Y26*%\n" +
                "D87*\n" +
                "X256277Y2481510D3*\n" +
                "M02*";

        GerberParser parser = new GerberParser(new StringReader(fileContent));
        List<GerberPrimitive> elements = parser.parse();

        assertEquals(1, elements.size());

        GerberPrimitive p = elements.get(0);
        assertEquals(Flash.class, p.getClass());
        Flash f = (Flash) p;
        assertEquals(6509, f.getX());
        assertEquals(63030, f.getY());

        assertEquals(ApertureMacro.class, f.getAperture().getClass());
        ApertureMacro macro = (ApertureMacro) f.getAperture();
        assertEquals(1, macro.getPrimitives().size());
        assertEquals(MacroPolygon.class, macro.getPrimitives().get(0).getClass());
        MacroPolygon m = (MacroPolygon)macro.getPrimitives().get(0);
        assertEquals(4, m.getVerticesCount());
        assertEquals(0, m.getCenter().getX());
        assertEquals(0, m.getCenter().getY());
        assertEquals(3089, m.getDiameter());
        assertEquals(-225, m.getRotationAngle());
    }


}
