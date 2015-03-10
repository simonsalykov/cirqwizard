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

import org.cirqwizard.settings.ApplicationConstants;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


public class RTPostprocessor implements Postprocessor
{
    private Integer x;
    private Integer y;
    private Integer z;
    private Integer feed;

    private DecimalFormat format;

    public RTPostprocessor()
    {
        format = new DecimalFormat("0.###");
        DecimalFormatSymbols customPoint = new DecimalFormatSymbols();
        customPoint.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(customPoint);
    }

    @Override
    public void home(StringBuilder str, Integer yDiff)
    {
        str.append("G28");
        if (yDiff != null)
            str.append(" Y").append(formatCoordinate(yDiff));
        str.append('\n');
    }

    @Override
    public void selectMachineWS(StringBuilder str)
    {
        str.append("G53\n");
    }

    public void header(StringBuilder str)
    {
    }

    @Override
    public void setupG54(StringBuilder str, int x, int y, int z)
    {
        str.append("G92 X").append(formatCoordinate(x)).append(" Y").append(formatCoordinate(y)).append(" Z").append(formatCoordinate(z)).append('\n');
    }

    @Override
    public void selectWCS(StringBuilder str)
    {
        str.append("G54\n");
    }

    private String formatCoordinate(int number)
    {
        return format.format((double)number / ApplicationConstants.RESOLUTION);
    }

    public void rapid(StringBuilder str, Integer x, Integer y, Integer z)
    {
        str.append("G0 ");
        if (x != null && (this.x == null || !this.x.equals(x)))
            str.append('X').append(formatCoordinate(x)).append(' ');
        if (y != null && (this.y == null || !this.y.equals(y)))
            str.append('Y').append(formatCoordinate(y)).append(' ');
        if (z != null && (this.z == null || !this.z.equals(z)))
            str.append('Z').append(formatCoordinate(z));
        str.append('\n');
        if (x != null)
            this.x = x;
        if (y != null)
            this.y = y;
        if (z != null)
            this.z = z;
    }

    public void linearInterpolation(StringBuilder str, Integer x, Integer y, Integer z, Integer feed)
    {
        str.append("G1 ");
        if (this.x == null || !this.x.equals(x))
            str.append('X').append(formatCoordinate(x)).append(' ');
        if (this.y == null || !this.y.equals(y))
            str.append('Y').append(formatCoordinate(y)).append(' ');
        if (this.z == null || !this.z.equals(z))
            str.append('Z').append(formatCoordinate(z)).append(' ');
        if (this.feed == null || !this.feed.equals(feed))
            str.append("F").append(formatCoordinate(feed));
        str.append('\n');
        this.x = x;
        this.y = y;
        this.z = z;
        this.feed = feed;
    }

    public void circularInterpolation(StringBuilder str, boolean clockwise, Integer x, Integer y, Integer z, Integer i, Integer j, Integer feed)
    {
        str.append('G');
        str.append(clockwise ? '2' : '3');
        str.append(' ');
        str.append('X').append(formatCoordinate(x)).append(' ');
        str.append('Y').append(formatCoordinate(y)).append(' ');
        if (this.z == null || !this.z.equals(z))
            str.append('Z').append(formatCoordinate(z)).append(' ');
        str.append('I').append(formatCoordinate(i)).append(' ');
        str.append('J').append(formatCoordinate(j)).append(' ');
        if (this.feed == null || !this.feed.equals(feed))
            str.append("F").append(formatCoordinate(feed));
        str.append('\n');
        this.x = x;
        this.y = y;
        this.z = z;
        this.feed = feed;
    }

    public void spindleOn(StringBuilder str, int speed)
    {
        str.append("S").append(speed).append(" M3\n");
        str.append("G4 P2.5\n");
    }

    public void spindleOff(StringBuilder str)
    {
        str.append("M5\n");
    }

    public void footer(StringBuilder str)
    {
    }

    public void pause(StringBuilder str, int duration)
    {
        str.append("G4 P").append(formatCoordinate(duration)).append("\n");
    }

    public void comment(StringBuilder str, String comment)
    {
        str.append("( ").append(comment).append(" )\n");
    }

    public void syringeOn(StringBuilder str)
    {
        str.append("M8\n");
    }

    public void syringeOff(StringBuilder str)
    {
        str.append("M9\n");
    }

    @Override
    public void rotatePP(StringBuilder str, int angle, int feed)
    {
        str.append("G1 A").append(formatCoordinate(angle / 100));
        str.append(" F").append(formatCoordinate(feed)).append("\n");
    }

    @Override
    public void rotatePP(StringBuilder str, int angle)
    {
        str.append("G0 A").append(formatCoordinate(angle / 100)).append("\n");
    }

    @Override
    public void vacuumOn(StringBuilder str)
    {
        str.append("M7\n");
    }

    @Override
    public void vacuumOff(StringBuilder str)
    {
        str.append("M9\n");
    }

    @Override
    public void getFirmwareVersion(StringBuilder str)
    {
        str.append("$$$info\n");
    }
}
