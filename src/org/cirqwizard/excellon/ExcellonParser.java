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
import org.cirqwizard.toolpath.DrillPoint;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExcellonParser
{
    public final static int INCHES_MM_RATIO = (int)(25.4 * ApplicationConstants.RESOLUTION);
    public final static int MM_MM_RATIO = ApplicationConstants.RESOLUTION;

    private final static Pattern TC_COMMAND_PATTERN = Pattern.compile("T(\\d+).*C(\\d+.\\d+).*");
    private final static Pattern T_COMMAND_PATTERN = Pattern.compile("T(\\d+)");
    private final static Pattern COORDINATES_PATTERN = Pattern.compile("(?:G01)?(X-?[0123456789\\.]+)?(Y-?[0123456789\\.]+)?");
    private final static Pattern MEASUREMENT_SYSTEM_PATTERN = Pattern.compile("(INCH|METRIC),?(LZ|TZ)?");

    private HashMap<Integer, Integer> tools = new HashMap<>();
    private Integer currentDiameter = null;
    private ArrayList<DrillPoint> drillPoints = new ArrayList<>();
    private boolean header = false;

    private int coordinatesCoversionRatio;
    private int decimalPlaces;
    private boolean leadingZerosOmmited = true;

    private Integer x = null;
    private Integer y = null;

    private Reader reader;

    public ExcellonParser(Reader reader)
    {
        this(2, 4, reader);
    }

    public ExcellonParser(int decimalPlaces, int coordinatesCoversionRatio, Reader reader)
    {
        this.decimalPlaces = decimalPlaces;
        this.coordinatesCoversionRatio = coordinatesCoversionRatio;
        this.reader = reader;
    }

    public ArrayList<DrillPoint> parse() throws IOException
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
            int diameter = (int) (Double.valueOf(matcher.group(2)) * coordinatesCoversionRatio);
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
            coordinatesCoversionRatio = matcher.group(1).equals("METRIC") ? MM_MM_RATIO : INCHES_MM_RATIO;
            if (matcher.group(2) != null)
                leadingZerosOmmited = matcher.group(2).equals("LZ");
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

        int decimalPartStart = str.indexOf('.');
        if (decimalPartStart >= 0)
        {
            str = str.replace(".", "");
            decimalPlaces = str.length() - decimalPartStart;
        }
        if (decimalPartStart < 0)
            decimalPartStart = str.length() - decimalPlaces;
        decimalPartStart = Math.max(decimalPartStart, 0);
        long number = Long.valueOf(str.substring(decimalPartStart)) * coordinatesCoversionRatio;
        for (int i = 0; i < decimalPlaces; i++)
            number /= 10;
        if (str.length() > decimalPlaces)
            number += Long.valueOf(str.substring(0, decimalPartStart)) * coordinatesCoversionRatio;
        return (int)(number * (negative ? -1 : 1));
    }

}
