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


public interface Postprocessor
{
    public void home(StringBuilder str, Integer yDiff);

    public void setupG54(StringBuilder str, int x, int y, int z);
    public void selectWCS(StringBuilder str);
    public void selectMachineWS(StringBuilder str);

    public void header(StringBuilder str);
    public void footer(StringBuilder str);
    public void comment(StringBuilder str, String comment);

    public void rapid(StringBuilder str, Integer x, Integer y, Integer z);
    public void linearInterpolation(StringBuilder str, Integer x, Integer y, Integer z, Integer feed);
    public void circularInterpolation(StringBuilder str, boolean clockwise, Integer x, Integer y, Integer z, Integer i, Integer j, Integer feed);

    public void spindleOn(StringBuilder str, int speed);
    public void spindleOff(StringBuilder str);

    public void syringeOn(StringBuilder str);
    public void syringeOff(StringBuilder str);

    public void pause(StringBuilder str, int duration);

    public void rotatePP(StringBuilder str, int angle, int feed);
    public void rotatePP(StringBuilder str, int angle);
    public void vacuumOn(StringBuilder str);
    public void vacuumOff(StringBuilder str);

    public void getFirmwareVersion(StringBuilder str);
}
