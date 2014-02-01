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

package org.cirqwizard.optimizer;

import org.cirqwizard.geom.Point;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;

import java.util.List;

public class Path
{
    private List<Toolpath> segments;

    public Path(List<Toolpath> segments)
    {
        this.segments = segments;
    }

    public List<Toolpath> getSegments()
    {
        return segments;
    }

    public Point getStart()
    {
        return ((CuttingToolpath)segments.get(0)).getCurve().getFrom();
    }

    public Point getEnd()
    {
        return ((CuttingToolpath)segments.get(segments.size() - 1)).getCurve().getTo();
    }
}
