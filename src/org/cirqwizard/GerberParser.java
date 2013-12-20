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

package org.cirqwizard;

import org.cirqwizard.appertures.*;
import org.cirqwizard.geom.Point;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.gerber.Flash;
import org.cirqwizard.gerber.LinearShape;
import org.cirqwizard.settings.Settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GerberParser
{
    private String filename;
    private ArrayList<GerberPrimitive> elements = new ArrayList<GerberPrimitive>();
    private ArrayList<Point> polygonPoints = new ArrayList<Point>();

    private boolean parameterMode = false;
    private boolean polygonMode = false;
    private HashMap<Integer, Aperture> apertures = new HashMap<Integer, Aperture>();

    private static final int MM_RATIO = 1 * Settings.RESOLUTION;
    private static final int INCHES_RATIO = (int)(25.4 * Settings.RESOLUTION);
    private int unitConversionRatio = MM_RATIO;

    private int integerPlaces = 2;
    private int decimalPlaces = 4;

    private enum InterpolationMode
    {
        LINEAR,
        CLOCKWISE_CIRCULAR,
        COUNTERCLOCKWISE_CIRCULAR
    }

    private InterpolationMode currentInterpolationMode = InterpolationMode.LINEAR;

    private int x = 0;
    private int y = 0;

    private enum ExposureMode
    {
        ON,
        OFF,
        FLASH
    }

    private enum PolygonStage
    {
        BEGIN,
        DRAWING,
        CLOSING,
        CLOSED
    }

    private ExposureMode exposureMode = ExposureMode.OFF;
    private PolygonStage polygonStage = PolygonStage.CLOSED;

    private Aperture aperture = null;

    public GerberParser(String filename)
    {
        this.filename = filename;
    }

    public ArrayList<GerberPrimitive> getElements()
    {
        return elements;
    }


    public void parse()
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(filename);
            String str;
            while ((str = readDataBlock(inputStream)) != null)
            {
                try
                {
                    if (parameterMode)
                        parseParameter(str);
                    else
                        processDataBlock(parseDataBlock(str));
                }
                catch (GerberParsingException e)
                {
                    LoggerFactory.getApplicationLogger().log(Level.FINE, "Unparsable gerber element", e);
                }
            }
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error reader gerber file", e);
        }
    }

    private String readDataBlock(InputStream inputStream) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        int i;
        while ((i = inputStream.read()) != -1)
        {
            if (i == '%')
                parameterMode = !parameterMode;
            else if (i == '*')
                break;
            else if (!Character.isWhitespace(i))
                sb.append((char)i);
        }
        if (sb.length() == 0)
            return null;
        return sb.toString();
    }

    private void parseParameter(String parameter) throws GerberParsingException
    {
        if (parameter.startsWith("AD"))
            parseApertureDefinition(parameter.substring(2));
        else if (parameter.startsWith("OF") || parameter.startsWith("IP"))
            LoggerFactory.getApplicationLogger().log(Level.FINE, "Ignoring obsolete gerber parameter");
        else if (parameter.startsWith("FS"))
            parseCoordinateFormatSpecification(parameter.substring(parameter.indexOf("X")));
        else
            throw new GerberParsingException("Unknown parameter: " + parameter);
    }

    private void parseCoordinateFormatSpecification(String str)
    {
        integerPlaces = Integer.parseInt(str.substring(1, 2));
        decimalPlaces = Integer.parseInt(str.substring(2, 3));
    }

    private void parseApertureDefinition(String str) throws GerberParsingException
    {
        if (!str.startsWith("D"))
            throw new GerberParsingException("Invalid aperture definition: " + str);

        str = str.substring(1);
        Pattern pattern = Pattern.compile("(\\d+)([CORP8]+)");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find())
            throw new GerberParsingException("Aperture definition incorrectly formatted: " + str);

        int apertureNumber = Integer.parseInt(matcher.group(1));
        String aperture = matcher.group(2).toString();

        if (aperture.equals("C"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d+)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of circular aperture");
            int diameter = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            apertures.put(apertureNumber, new CircularAperture(diameter));
        }
        else if (aperture.equals("R"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d+)X(\\d*.\\d+)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of rectangular aperture");
            int width = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            int height = (int)(Double.valueOf(matcher.group(2)) * unitConversionRatio);
            apertures.put(apertureNumber, new RectangularAperture(width, height));
        }
        else if (aperture.equals("OC8"))
        {
            pattern = Pattern.compile(".*,(\\d*.\\d+)");
            matcher = pattern.matcher(str);
            if (!matcher.find())
                throw new GerberParsingException("Invalid definition of circular aperture");
            int diameter = (int)(Double.valueOf(matcher.group(1)) * unitConversionRatio);
            apertures.put(apertureNumber, new OctagonalAperture(diameter));
        }
        else if (aperture.equals("O"))
        {
            System.out.println("Oval aperture");
        }
        else if (aperture.equals("P"))
        {
            System.out.println("Polygon aperture");
        }
        else
            throw new GerberParsingException("Unknown aperture");
    }

    private DataBlock parseDataBlock(String str)
    {
        DataBlock dataBlock = new DataBlock();
        Pattern pattern = Pattern.compile("([GMDXY])(\\d+)");
        Matcher matcher = pattern.matcher(str);
        int i = 0;
        while (matcher.find(i))
        {
            switch (matcher.group(1).charAt(0))
            {
                case 'G': dataBlock.setG(Integer.parseInt(matcher.group(2))); break;
                case 'M': dataBlock.setM(Integer.parseInt(matcher.group(2))); break;
                case 'D': dataBlock.setD(Integer.parseInt(matcher.group(2))); break;
                case 'X': dataBlock.setX(convertCoordinates(matcher.group(2))); break;
                case 'Y': dataBlock.setY(convertCoordinates(matcher.group(2))); break;
            }
            i = matcher.end();
        }
        return dataBlock;
    }

    private int convertCoordinates(String str)
    {
        if(str.equals("0"))
            return 0;

        int validIntPlaces = str.length() >= (integerPlaces + decimalPlaces) ? integerPlaces : (str.length() - decimalPlaces);

        int number = Integer.valueOf(str.substring(validIntPlaces)) * unitConversionRatio;
        for (int i = 0; i < decimalPlaces; i++)
            number /= 10;

        if(validIntPlaces > 0)
            number += Integer.valueOf(str.substring(0, validIntPlaces)) * unitConversionRatio;

        return number;
    }

    private void processDataBlock(DataBlock dataBlock) throws GerberParsingException
    {
        if (dataBlock.getM() != null)
        {
            switch (dataBlock.getM())
            {
                case 2: return;
                default:
                    throw new GerberParsingException("Unknown mcode: " + dataBlock.getM());
            }
        }
        if (dataBlock.getG() != null)
        {
            switch (dataBlock.getG())
            {
                case 36: polygonMode = true;
                         polygonStage = PolygonStage.BEGIN; break;
                case 37: polygonStage = PolygonStage.CLOSING; break;
                case 54: break;
                case 70: unitConversionRatio = INCHES_RATIO; break;
                case 71: unitConversionRatio = MM_RATIO; break;
                default:
                    throw new GerberParsingException("Unknown gcode: " + dataBlock.getG());
            }
        }
        if (dataBlock.getD() != null)
        {
            switch (dataBlock.getD())
            {
                case 1: exposureMode = ExposureMode.ON; break;
                case 2: exposureMode = ExposureMode.OFF; break;
                case 3: exposureMode = ExposureMode.FLASH; break;
                default:
                    aperture = apertures.get(dataBlock.getD());
                    if (aperture == null)
                        throw new GerberParsingException("Undefined aperture used: " + dataBlock.getD());
                    return;
            }
        }
        Integer newX = x;
        if (dataBlock.getX() != null)
            newX = dataBlock.getX();
        Integer newY = y;
        if (dataBlock.getY() != null)
            newY = dataBlock.getY();

        if (polygonMode)
        {
            switch (polygonStage)
            {
                case BEGIN: polygonPoints = new ArrayList<>();
                     polygonStage = PolygonStage.DRAWING; break;
                case DRAWING: polygonPoints.add(new Point(newX, newY)); break;
                case CLOSING: elements.add(new Flash(0, 0, new PolygonalAperture(polygonPoints)));
                     polygonMode = false;
                     polygonStage = PolygonStage.CLOSED; break;
            }
        }
        else if (aperture != null)
        {
            if(exposureMode == ExposureMode.FLASH)
                elements.add(new Flash(newX, newY, aperture));
            else if (exposureMode == ExposureMode.ON && (!newX.equals(x) || !newY.equals(y)))
                elements.add(new LinearShape(x, y, newX, newY, aperture));
        }
        x = newX;
        y = newY;
    }

}
