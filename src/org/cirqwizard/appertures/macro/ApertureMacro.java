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
package org.cirqwizard.appertures.macro;

import org.cirqwizard.appertures.Aperture;

import java.util.ArrayList;
import java.util.List;

public class ApertureMacro extends Aperture
{
    private ArrayList<MacroPrimitive> primitives = new ArrayList<>();

    public void addPrimitive(MacroPrimitive primitive)
    {
        primitives.add(primitive);
    }

    public List<MacroPrimitive> getPrimitives()
    {
        return primitives;
    }


    @Override
    public Aperture rotate(boolean clockwise)
    {
        ApertureMacro clone = new ApertureMacro();
        for (MacroPrimitive p : primitives)
        {
            p = p.clone();
            p.setRotationAngle(p.getRotationAngle() + 90 * (clockwise ? 1 : -1));
            clone.addPrimitive(p);
        }
        return clone;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public int getWidth()
    {
        return 0;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }

    @Override
    public int getCircumRadius()
    {
        return 2000;
    }
}
