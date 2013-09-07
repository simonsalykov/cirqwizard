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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;


public class ExcellonParser
{
    private final static RealNumber INCHES_MM_RATIO = new RealNumber("25.4");
    private final static int DECIMAL_PLACES = 4;

    private HashMap<Integer, RealNumber> tools = new HashMap<Integer, RealNumber>();
    private RealNumber currentDiameter;
    private ArrayList<DrillPoint> drillPoints = new ArrayList<DrillPoint>();


    public void parse(String filename)
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(filename);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(inputStream));
            String str = reader.readLine();
            if (str == null || !str.equals("%"))
            {
                LoggerFactory.getApplicationLogger().log(Level.INFO, "Unsupported excellon format");
                return;

            }
            boolean header = true;
            while ((str = reader.readLine()) != null)
            {
                if (str.equals("%"))
                    header = false;
                else if (header)
                    parseHeaderLine(str);
                else
                    parseBodyLine(str);
            }
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error reading excellon file", e);
        }
    }

    private void parseHeaderLine(String line)
    {
        if (line.equals("M48"))
            return; // Parsing header, not huge news
        if (line.equals("M72"))
            return; // Well, inches - what else could it be?
        if (line.startsWith("T") && line.charAt(3) == 'C')  // Tool definition
        {
            int toolNumber = Integer.parseInt(line.substring(1, 3));
            RealNumber diameter = new RealNumber(line.substring(5)).multiply(INCHES_MM_RATIO);
            tools.put(toolNumber, diameter);
        }
    }

    private void parseBodyLine(String line)
    {
        if (line.equals("M30"))
            return; // End of program
        if (line.startsWith("T"))
            currentDiameter = tools.get(Integer.parseInt(line.substring(1)));
        else
        {
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

    public ArrayList<DrillPoint> getDrillPoints()
    {
        return drillPoints;
    }

}
