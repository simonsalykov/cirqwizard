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

package org.cirqwizard.pp;

import org.cirqwizard.settings.ApplicationConstants;


public enum Feeder
{
    SMALL
            {
                @Override
                public int getYForRow(int baseY, int row)
                {
                    double offset = 0;
                    switch (row)
                    {
                        case 0: offset = 3; break;
                        case 1: offset = 13.5; break;
                        case 2: offset = 21; break;
                        case 3: offset = 29.5; break;
                        case 4: offset = 39; break;
                        case 5: offset = 49.5; break;
                    }
                    return (int)(baseY - offset * ApplicationConstants.RESOLUTION);
                }

                @Override
                public int getRowCount()
                {
                    return 6;
                }
            },
    MEDIUM
            {
                @Override
                public int getYForRow(int baseY, int row)
                {
                    double offset = 0;
                    switch (row)
                    {
                        case 0: offset = 6; break;
                        case 1: offset = 18.5; break;
                        case 2: offset = 32; break;
                        case 3: offset = 46.5; break;
                    }
                    return (int)(baseY - offset * ApplicationConstants.RESOLUTION);
                }

                @Override
                public int getRowCount()
                {
                    return 4;
                }
            },
    LARGE
            {
                @Override
                public int getYForRow(int baseY, int row)
                {
                    double offset = 0;
                    switch (row)
                    {
                        case 0: offset = 8; break;
                        case 1: offset = 24.5; break;
                        case 2: offset = 42; break;

                    }
                    return (int)(baseY - offset * ApplicationConstants.RESOLUTION);
                }

                @Override
                public int getRowCount()
                {
                    return 3;
                }
            };

    public abstract int getYForRow(int baseY, int row);
    public abstract int getRowCount();
}
