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

import org.cirqwizard.fx.Context;

public class PCBPlacement extends org.cirqwizard.fx.common.PCBPlacement
{

    @Override
    public void refresh()
    {
        super.refresh();
        text.setText("Put the board FACE UP right on the machine bed. Make sure both the bed and PCB are clean. It is also worth checking if PCB edges are smooth.");
    }

    @Override
    protected Context.PcbPlacement getExpectedPlacement()
    {
        return Context.PcbPlacement.FACE_UP;
    }

}
