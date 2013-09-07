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

import org.cirqwizard.math.RealNumber;


public enum Feeder
{
    SMALL
            {
                @Override
                public RealNumber getYForRow(RealNumber baseY, int row)
                {
                    switch (row)
                    {
                        case 0: return baseY.subtract(new RealNumber("3"));
                        case 1: return baseY.subtract(new RealNumber("13.5"));
                        case 2: return baseY.subtract(new RealNumber("21"));
                        case 3: return baseY.subtract(new RealNumber("29.5"));
                        case 4: return baseY.subtract(new RealNumber("39"));
                        case 5: return baseY.subtract(new RealNumber("49.5"));
                    }
                    return null;
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
                public RealNumber getYForRow(RealNumber baseY, int row)
                {
                    switch (row)
                    {
                        case 0: return baseY.subtract(new RealNumber("6"));
                        case 1: return baseY.subtract(new RealNumber("18.5"));
                        case 2: return baseY.subtract(new RealNumber("32"));
                        case 3: return baseY.subtract(new RealNumber("46.5"));
                    }
                    return null;
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
                public RealNumber getYForRow(RealNumber baseY, int row)
                {
                    switch (row)
                    {
                        case 0: return baseY.subtract(new RealNumber("8"));
                        case 1: return baseY.subtract(new RealNumber("24.5"));
                        case 2: return baseY.subtract(new RealNumber("42"));

                    }
                    return null;
                }

                @Override
                public int getRowCount()
                {
                    return 3;
                }
            };

    public abstract RealNumber getYForRow(RealNumber baseY, int row);
    public abstract int getRowCount();
}
