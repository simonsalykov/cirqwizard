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

package org.cirqwizard.fx.traces.bottom;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.traces.TraceMilling;
import org.cirqwizard.layers.Board;

public class BottomTraceMilling extends TraceMilling
{
    @Override
    public void refresh()
    {
        pcbPane.setGerberColor(PCBPane.BOTTOM_TRACE_COLOR);
        super.refresh();
    }

    @Override
    protected boolean isEnabled()
    {
        return super.isEnabled() && Context.PcbPlacement.FACE_DOWN.equals(getMainApplication().getContext().getPcbPlacement());
    }

    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.BOTTOM;
    }

    @Override
    protected boolean mirror()
    {
        return true;
    }

}
