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

package org.cirqwizard.excellon;

import org.cirqwizard.geom.Point;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.generation.toolpath.DrillPoint;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExcellonParser
{
    public final static BigDecimal INCHES_MM_RATIO = new BigDecimal((int)(25.4 * ApplicationConstants.RESOLUTION));
    public final static BigDecimal MM_MM_RATIO = new BigDecimal(ApplicationConstants.RESOLUTION);

    private final static Pattern TC_COMMAND_PATTERN = Pattern.compile("T(\\d+).*C(\\d*.\\d+).*");
    private final static Pattern T_COMMAND_PATTERN = Pattern.compile("T(\\d+)");
    private final static Pattern COORDINATES_PATTERN = Pattern.compile("(?:G01)?(X\\+?-?[0123456789\\.]+)?(Y\\+?-?[0123456789\\.]+)?");
    private final static Pattern R_COMMAND_PATTERN = Pattern.compile("R(\\d+)(X-?[0123456789\\.]+)?(Y-?[0123456789\\.]+)?");
    private final static Pattern MEASUREMENT_SYSTEM_PATTERN = Pattern.compile("(INCH|METRIC),?(LZ|TZ)?");

    private HashMap<Integer, Integer> tools = new HashMap<>();
    private Integer currentDiameter = 0;
    private ArrayList<DrillPoint> drillPoints = new ArrayList<>();
    private boolean header = false;

    private BigDecimal coordinatesConversionRatio;
    private int integerPlaces;
    private int decimalPlaces;
    private boolean leadingZeros = false;

    private Integer x = null;
    private Integer y = null;

    private Reader reader;

    public ExcellonParser(Reader reader)
    {
        this(2, 4, INCHES_MM_RATIO, reader);
    }

    public ExcellonParser(int integerPlaces, int decimalPlaces, BigDecimal coordinatesConversionRatio, Reader reader)
    {
        this.integerPlaces = integerPlaces;
        this.decimalPlaces = decimalPlaces;
        this.coordinatesConversionRatio = coordinatesConversionRatio;
        this.reader = reader;
    }

    public List<DrillPoint> parse() throws IOException
    {
        LineNumberReader r = new LineNumberReader(reader);
        String str;

        while ((str = r.readLine()) != null)
        {
            if (header)
                parseHeaderLine(str);
            else
                parseBodyLine(str);
        }

        return drillPoints;
    }

    private boolean parseHeaderCommands(String line)
    {
        if (line.equals("%"))
        {
            header = false;
            return true;
        }
        if (line.equals("M48"))
        {
            header = true;
            return true;
        }

        return false;
    }

    private boolean parseToolDefinition(String line, boolean updateCurrentTool)
    {
        Matcher matcher = TC_COMMAND_PATTERN.matcher(line);
        if (matcher.matches())
        {
            int toolNumber = Integer.parseInt(matcher.group(1));
            int diameter = coordinatesConversionRatio.multiply(new BigDecimal(matcher.group(2))).intValue();
            tools.put(toolNumber, diameter);
            if (updateCurrentTool)
                currentDiameter = diameter;
            return true;
        }

        return false;
    }

    private void parseHeaderLine(String line)
    {
        if (parseHeaderCommands(line))
            return;
        if (parseToolDefinition(line, false))
            return;

        Matcher matcher = MEASUREMENT_SYSTEM_PATTERN.matcher(line);
        if (matcher.matches())
        {
            coordinatesConversionRatio = matcher.group(1).equals("METRIC") ? MM_MM_RATIO : INCHES_MM_RATIO;
            if (matcher.group(2) != null)
                leadingZeros = matcher.group(2).equals("LZ");
            return;
        }
    }

    private void parseBodyLine(String line)
    {
        if (parseHeaderCommands(line))
            return;

        if (parseToolDefinition(line, true))
            return;

        Matcher matcher = T_COMMAND_PATTERN.matcher(line);
        if (matcher.matches())
        {
            currentDiameter = tools.get(Integer.parseInt(matcher.group(1)));
            if (currentDiameter == null)
                currentDiameter = Integer.valueOf(matcher.group(1)) * ApplicationConstants.RESOLUTION / 10 + ApplicationConstants.RESOLUTION;
            return;
        }

        matcher = R_COMMAND_PATTERN.matcher(line);
        if (matcher.matches())
        {
            int repetitions = Integer.valueOf(matcher.group(1));
            Integer deltaX = null, deltaY = null;
            if (matcher.group(2) != null)
                deltaX = convertCoordinate(matcher.group(2).substring(1));
            if (matcher.group(3) != null)
                deltaY = convertCoordinate(matcher.group(3).substring(1));
            for (int i = 0; i < repetitions; i++)
            {
                if (deltaX != null)
                    x += deltaX;
                if (deltaY != null)
                    y += deltaY;
                drillPoints.add(new DrillPoint(new Point(x, y), currentDiameter));
            }
        }

        matcher = COORDINATES_PATTERN.matcher(line);
        if (matcher.matches())
        {
            if (matcher.group(1) != null)
                x = convertCoordinate(matcher.group(1).substring(1));
            if (matcher.group(2) != null)
                y = convertCoordinate(matcher.group(2).substring(1));
            if (x != null && y != null)
            {
                Point point = new Point(x, y);
                drillPoints.add(new DrillPoint(point, currentDiameter));
            }
        }
    }

    private int convertCoordinate(String str)
    {
        boolean negative = str.startsWith("-");
        if (negative)
            str = str.substring(1);

        if (str.indexOf('.') < 0) // Decimal point location needs to be deduced
        {
            while (str.length() < integerPlaces + decimalPlaces)
            {
                if (leadingZeros)
                    str = str + "0";
                else
                    str = "0" + str;
            }
        }
        else
            return (new BigDecimal(str).multiply(coordinatesConversionRatio)).intValue() * (negative ? -1 : 1);

        long number = coordinatesConversionRatio.multiply(new BigDecimal(Long.valueOf(str.substring(integerPlaces)))).longValue();
        for (int i = 0; i < decimalPlaces; i++)
            number /= 10;
        number += coordinatesConversionRatio.multiply(new BigDecimal(Long.valueOf(str.substring(0, integerPlaces)))).longValue();
        return (int)(number * (negative ? -1 : 1));
    }

}
