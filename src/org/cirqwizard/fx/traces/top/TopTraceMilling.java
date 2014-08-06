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

package org.cirqwizard.fx.traces.top;

import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.traces.TraceMilling;
import org.cirqwizard.layers.Layer;

public class TopTraceMilling extends TraceMilling
{
    @Override
    public void refresh()
    {
        super.refresh();
        pcbPane.setGerberColor(PCBPaneFX.TOP_TRACE_COLOR);
    }

    @Override
    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getTopTracesLayer();
    }

    @Override
    protected boolean mirror()
    {
        return false;
    }

    @Override
    protected int getCacheId()
    {
        return 0;
    }
}
