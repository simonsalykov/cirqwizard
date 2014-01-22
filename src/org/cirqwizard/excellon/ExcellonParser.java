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
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.MathUtil;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.DrillPoint;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExcellonParser
{
    private final static RealNumber INCHES_MM_RATIO = new RealNumber("25.4");
    private final static int DECIMAL_PLACES = 4;

    private HashMap<Integer, RealNumber> tools = new HashMap<Integer, RealNumber>();
    private RealNumber currentDiameter;
    private ArrayList<DrillPoint> drillPoints = new ArrayList<DrillPoint>();

    private Reader reader;

    public ExcellonParser(Reader reader)
    {
        this.reader = reader;
    }

    public ArrayList<DrillPoint> parse() throws IOException
    {
        LineNumberReader r = new LineNumberReader(reader);
        String str;

        boolean header = false;
        while ((str = r.readLine()) != null)
        {
            if (str.equals("M48"))
                header = true;
            if (str.equals("%"))
                header = false;
            else if (header)
                parseHeaderLine(str);
            else
                parseBodyLine(str);
        }

        return drillPoints;
    }

    private void parseHeaderLine(String line)
    {
        Matcher matcher = Pattern.compile("T(\\d+)C(\\d+.\\d+).*").matcher(line);
        if (matcher.matches())
        {
            int toolNumber = Integer.parseInt(matcher.group(1));
            RealNumber diameter = new RealNumber(matcher.group(2)).multiply(INCHES_MM_RATIO);
            tools.put(toolNumber, diameter);
        }
    }

    private void parseBodyLine(String line)
    {
        Matcher matcher = Pattern.compile("T(\\d+)C(\\d+.\\d+).*").matcher(line);

        if (line.equals("M30"))
            return; // End of program
        if (line.equals("G90"))
            return; // Absolute Mode
        if (line.equals("G05"))
            return; // Drill Mode

        if (matcher.matches())
        {
            int toolNumber = Integer.parseInt(matcher.group(1));
            RealNumber diameter = currentDiameter = new RealNumber(matcher.group(2)).multiply(INCHES_MM_RATIO);
            tools.put(toolNumber, diameter);
            return; // C# command
        }
        if (line.startsWith("T"))
        {
            currentDiameter = tools.get(Integer.parseInt(line.substring(1)));
        }
        else
        {
            if (currentDiameter == null)
                return;

            String x = line.substring(1, line.indexOf('Y'));
            String y = line.substring(line.indexOf('Y') + 1);
            Point point = new Point(convertCoordinate(x), convertCoordinate(y));
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
