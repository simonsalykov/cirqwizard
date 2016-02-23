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
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.gerber.Region;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class RasterWindow
{
    private BufferedImage window;
    private Point windowLowerLeftCorner;
    private Graphics2D g;

    public RasterWindow(Point windowLowerLeftCorner, int width, int height)
    {
        this(windowLowerLeftCorner, width, height, 1);
    }

    /**
     *
     * @param windowLowerLeftCorner - window offset (in pre-scaled coordinates)
     * @param width width of window before scaling
     * @param height height of window before scaling
     * @param scale multiplier used to speed up processing time (1.0 is 1:1 scale, 0.5 is 1:2 scale, etc.)
     */
    public RasterWindow(Point windowLowerLeftCorner, int width, int height, double scale)
    {
        this.windowLowerLeftCorner = windowLowerLeftCorner;
        this.window = new BufferedImage((int)(scale * width), (int)(scale * height), BufferedImage.TYPE_BYTE_BINARY);
        g = window.createGraphics();
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, window.getWidth(), window.getHeight());
        g = window.createGraphics();
        g.transform(AffineTransform.getScaleInstance(scale, scale));
        g.transform(AffineTransform.getTranslateInstance(-windowLowerLeftCorner.getX(), -windowLowerLeftCorner.getY()));
    }

    public void render(java.util.List<GerberPrimitive> primitives, int inflation)
    {
        for (GerberPrimitive primitive : primitives)
            renderPrimitive(primitive, primitive.getPolarity() == GerberPrimitive.Polarity.DARK ? inflation : -inflation);
    }

    private void renderPrimitive(GerberPrimitive primitive, double inflation)
    {
        if (!(primitive instanceof Region) && !primitive.getAperture().isVisible())
            return;

        g.setColor(primitive.getPolarity() == GerberPrimitive.Polarity.DARK ? Color.WHITE : Color.BLACK);
        primitive.render(g, inflation);
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
