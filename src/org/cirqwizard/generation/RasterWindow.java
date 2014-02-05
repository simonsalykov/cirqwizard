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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class RasterWindow
{
    private BufferedImage window;
    private PointI windowLowerLeftCorner;

    private int[] dummyArray = new int[1];

    public RasterWindow(BufferedImage window, PointI windowLowerLeftCorner)
    {
        this.window = window;
        this.windowLowerLeftCorner = windowLowerLeftCorner;
    }

    public boolean contains(PointI pointI)
    {
        return pointI.x >= windowLowerLeftCorner.x && pointI.x < windowLowerLeftCorner.x + window.getWidth() - 1 &&
                pointI.y >= windowLowerLeftCorner.y && pointI.y < windowLowerLeftCorner.y + window.getHeight() - 1;
    }

    public int getPixel(PointI point)
    {
        return window.getRaster().getPixel(point.x - windowLowerLeftCorner.x, point.y - windowLowerLeftCorner.y, dummyArray)[0];
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RasterWindow that = (RasterWindow) o;

        return windowLowerLeftCorner.equals(that.windowLowerLeftCorner) && window.getWidth() == that.window.getWidth() && window.getHeight() == that.window.getHeight();
    }

    @Override
    public int hashCode()
    {
        return windowLowerLeftCorner != null ? windowLowerLeftCorner.hashCode() : 0;
    }

    public BufferedImage getBufferedImage()
    {
        return window;
    }

    public void save(String file)
    {
        System.out.println("windowLeftCorner: " + windowLowerLeftCorner);
        try
        {
            ImageIO.write(window, "png", new File(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
