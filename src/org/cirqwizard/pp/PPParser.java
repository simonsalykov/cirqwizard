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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public class PPParser
{
    private String filename;
    private int resolution;

    private List<PPPoint> components;

    public PPParser(String filename, int resolution)
    {
        this.filename = filename;
        this.resolution = resolution;
    }

    public void parse()
    {
        components = new ArrayList<>();
        try
        {
            LineNumberReader reader = new LineNumberReader(new FileReader(filename));
            String str;
            while ((str = reader.readLine()) != null)
            {
                StringTokenizer tokenizer = new StringTokenizer(str, " ");
                String name = tokenizer.nextToken();
                String x = tokenizer.nextToken();
                String y = tokenizer.nextToken();
                String angle = tokenizer.nextToken();
                String value = tokenizer.nextToken();
                String packaging;
                if (tokenizer.hasMoreElements())
                    packaging = tokenizer.nextToken();
                else
                {
                    packaging = value;
                    value = "";
                }

                components.add(new PPPoint(new ComponentId(packaging, value),
                        new Point(Integer.valueOf(x) * resolution, Integer.valueOf(y) * resolution), Integer.valueOf(angle), name));
            }
        }
        catch (FileNotFoundException e)
        {
            LoggerFactory.logException("Could not open PP file", e);
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error reading PP file", e);
        }
        catch (NoSuchElementException e)
        {
            LoggerFactory.logException("Error parsing PP file", e);
        }
    }

    public List<PPPoint> getComponents()
    {
        return components;
    }

}
