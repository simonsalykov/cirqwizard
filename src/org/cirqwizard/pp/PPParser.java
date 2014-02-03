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

package org.cirqwizard.pp;

import org.cirqwizard.geom.Point;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.toolpath.PPPoint;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PPParser
{
    private Reader reader;
    private Pattern pattern;

    public PPParser(Reader reader, String pattern)
    {
        this.reader = reader;
        this.pattern = Pattern.compile(pattern);
    }

    public List<PPPoint> parse() throws IOException
    {
        List<PPPoint> components = new ArrayList<>();
        try
        {
            LineNumberReader reader = new LineNumberReader(this.reader);
            String str;
            while ((str = reader.readLine()) != null)
            {
                Matcher matcher = pattern.matcher(str);
                if (!matcher.find())
                    continue;

                String name = matcher.group("name");
                String x = matcher.group("x");
                String y = matcher.group("y");
                String angle = matcher.group("angle");
                String value = matcher.group("value");
                String packaging = matcher.group("package");
                if (packaging == null)
                {
                    packaging = value;
                    value = "";
                }

                components.add(new PPPoint(new ComponentId(packaging, value),
                        new Point(new RealNumber(x), new RealNumber(y)), new RealNumber(angle), name));
            }
        }
        catch (FileNotFoundException e)
        {
            LoggerFactory.logException("Could not open PP file", e);
        }
        catch (NoSuchElementException e)
        {
            LoggerFactory.logException("Error parsing PP file", e);
        }
        return components;
    }

}
