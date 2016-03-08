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
package org.cirqwizard.layers;

import javafx.scene.canvas.GraphicsContext;
import org.cirqwizard.geom.Point;

public interface LayerElement extends Cloneable
{
    Point getMin();
    Point getMax();
    void move(Point point);
    void rotate(boolean clockwise);
    Object clone() throws CloneNotSupportedException;
    void render(GraphicsContext g);
    boolean isVisible();
}
