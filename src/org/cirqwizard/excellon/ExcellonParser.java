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
import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;
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
    private final static RealNumber INCHES_MM_RATIO = new RealNumber("25.4");
    private final static int DECIMAL_PLACES = 4;

    private final static Pattern TC_COMMAND_PATTERN = Pattern.compile("T(\\d+)C(\\d+.\\d+).*");
    private final static Pattern T_COMMAND_PATTERN = Pattern.compile("T(\\d+)");
    private final static Pattern COORDINATES_PATTERN = Pattern.compile("X(\\d+)Y(\\d+)");

    private HashMap<Integer, RealNumber> tools = new HashMap<Integer, RealNumber>();
    private RealNumber currentDiameter;
    private ArrayList<DrillPoint> drillPoints = new ArrayList<DrillPoint>();
    private boolean header = false;

    private Reader reader;

    public ExcellonParser(Reader reader)
    {
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

    private boolean parseToolDefinition(String line)
    {
        Matcher matcher = TC_COMMAND_PATTERN.matcher(line);
        if (matcher.matches())
        {
            int toolNumber = Integer.parseInt(matcher.group(1));
            RealNumber diameter = new RealNumber(matcher.group(2)).multiply(INCHES_MM_RATIO);
            tools.put(toolNumber, diameter);
            return true;
        }

        return false;
    }

    private void parseHeaderLine(String line)
    {
        if (parseHeaderCommands(line))
            return;
        if (parseToolDefinition(line))
            return;
    }

    private void parseBodyLine(String line)
    {
        if (parseHeaderCommands(line))
            return;

        if (parseToolDefinition(line))
            return;

        Matcher matcher = T_COMMAND_PATTERN.matcher(line);
        if (matcher.matches())
        {
            currentDiameter = tools.get(Integer.parseInt(matcher.group(1)));
            return;
        }

        matcher = COORDINATES_PATTERN.matcher(line);
        if (matcher.matches())
        {
            Point point = new Point(convertCoordinate(matcher.group(1)), convertCoordinate(matcher.group(2)));
            drillPoints.add(new DrillPoint(point, currentDiameter));
        }
    }

    private static RealNumber convertCoordinate(String str)
    {
        int decimalPartStart = str.length() - DECIMAL_PLACES;
        decimalPartStart = Math.max(decimalPartStart, 0);
        RealNumber number = new RealNumber(str.substring(decimalPartStart)).divide(MathUtil.pow(new RealNumber(10), DECIMAL_PLACES));
        if (str.length() > DECIMAL_PLACES)
            number = number.add(new RealNumber(str.substring(0, decimalPartStart)));
        return number.multiply(INCHES_MM_RATIO);
    }

}
