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

package org.cirqwizard.fx.rubout;

import org.cirqwizard.fx.Context;
import org.cirqwizard.layers.Board;

public class TopRubout extends Rubout
{
    @Override
    protected boolean isEnabled()
    {
        return super.isEnabled() && Context.PcbPlacement.FACE_UP.equals(getMainApplication().getContext().getPcbPlacement());
    }

    @Override
    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.TOP;
    }

    @Override
    protected int getCacheId()
    {
        return 2;
    }

    @Override
    protected long getLayerModificationDate()
    {
        return 0; //getMainApplication().getContext().getPcbLayout().getTopLayerModificationDate();
    }

    @Override
    protected boolean mirror()
    {
        return false;
    }
}
