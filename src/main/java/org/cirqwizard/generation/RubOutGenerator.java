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

package org.cirqwizard.generation;

import org.cirqwizard.geom.Point;
import org.cirqwizard.generation.toolpath.LinearToolpath;
import org.cirqwizard.generation.toolpath.Toolpath;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RubOutGenerator
{
    private byte[] sourceData;
    private int width;
    private int height;
    private int diameter;
    private int overlap;

    /**
     *
     * @param image Source raster window
     * @param diameter Tool diameter
     * @param overlap Tool paths overlap in mm/RESOLUTION units
     */
    public RubOutGenerator(BufferedImage image, int diameter, int overlap)
    {
        sourceData = (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.diameter = diameter;
        this.overlap = overlap;
    }

    private byte getPoint(int x, int y)
    {
        return sourceData[y * width + x];
    }

    private boolean checkVertical(int x, int y)
    {
        for (int yy = y - diameter / 2; yy < y + diameter / 2; yy++)
        {
            if (getPoint(x, yy) != 0)
                return false;
        }
        return true;
    }

    private Toolpath generateToolpath(int start, int end, int y)
    {
        for (int yy = y - diameter / 2; yy < y + diameter / 2 - overlap; yy++)
        {
            int endIndex = yy * width + end;
            for (int index = yy * width + start; index < endIndex; index++)
                sourceData[index] = 1;
        }
        return new LinearToolpath(diameter, new Point(start, y), new Point(end, y));
    }

    public List<Toolpath> process()
    {
        List<Toolpath> result = new ArrayList<>();
        int radius = diameter / 2;
        for (int y = radius; y < height - radius; y++)
        {
            Integer start = null;
            for (int x = 0; x < width - 1; x++)
            {
                if (start == null && getPoint(x, y) == 0 && checkVertical(x, y))
                    start = x;
                else if (start != null && !checkVertical(x, y))
                {
                    if (x - start > diameter / 2)
                        result.add(generateToolpath(start, x, y));
                    start = null;
                }
            }
            if (start != null)
                result.add(generateToolpath(start, width - 1, y));
        }

        return result;
    }
}
