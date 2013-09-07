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

package org.cirqwizard.post;

import org.cirqwizard.math.RealNumber;


public interface Postprocessor
{
    public void home(StringBuilder str, RealNumber yDiff);

    public void setupG54(StringBuilder str, RealNumber x, RealNumber y, RealNumber z);
    public void selectWCS(StringBuilder str);
    public void selectMachineWS(StringBuilder str);

    public void header(StringBuilder str);
    public void footer(StringBuilder str);
    public void comment(StringBuilder str, String comment);

    public void rapid(StringBuilder str, RealNumber x, RealNumber y, RealNumber z);
    public void linearInterpolation(StringBuilder str, RealNumber x, RealNumber y, RealNumber z, RealNumber feed);
    public void circularInterpolation(StringBuilder str, boolean clockwise, RealNumber x, RealNumber y, RealNumber z, RealNumber i, RealNumber j, RealNumber feed);

    public void spindleOn(StringBuilder str, String speed);
    public void spindleOff(StringBuilder str);

    public void syringeOn(StringBuilder str);
    public void syringeOff(StringBuilder str);

    public void pause(StringBuilder str, RealNumber duration);

    public void rotatePP(StringBuilder str, RealNumber angle, RealNumber feed);
    public void rotatePP(StringBuilder str, RealNumber angle);
    public void vacuumOn(StringBuilder str);
    public void vacuumOff(StringBuilder str);

    public void getFirmwareVersion(StringBuilder str);
}
