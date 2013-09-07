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

package org.cirqwizard.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;


public class RealNumber
{
    public final static RealNumber PRECISION_THRESHOLD = new RealNumber("0.00000001");

    private final static DecimalFormat format = new DecimalFormat("0.000000");

    private BigDecimal value;
    private Double doubleValue = null;

    private final static BigDecimal ZERO = new BigDecimal(0);
    private final static BigDecimal TWO = new BigDecimal(2);

    public RealNumber(String str)
    {
        value = new BigDecimal(str);
    }

    public RealNumber(int i)
    {
        if (i == 0)
            value = ZERO;
        else if (i == 2)
            value = TWO;
        else
            value = new BigDecimal(i);
    }

    public RealNumber(BigDecimal bd)
    {
        value = bd;
    }

    public RealNumber(double d)
    {
        value = new BigDecimal(d);
    }

    public double doubleValue()
    {
        if (doubleValue == null)
            doubleValue = value.doubleValue();
        return doubleValue;
    }

    public RealNumber add(RealNumber number)
    {
        return new RealNumber(value.add(number.value));
    }

    public RealNumber subtract(RealNumber number)
    {
        return new RealNumber(value.subtract(number.value));
    }

    public RealNumber subtract(int number)
    {
        return new RealNumber(value.subtract(new BigDecimal(number)));
    }

    public RealNumber multiply(RealNumber number)
    {
        return new RealNumber(value.multiply(number.value));
    }

    public RealNumber multiply(int number)
    {
        return new RealNumber(value.multiply(new BigDecimal(number)));
    }

    public RealNumber divide(RealNumber number)
    {
        return new RealNumber(value.divide(number.value, new MathContext(100, RoundingMode.HALF_EVEN)));
    }

    public RealNumber divide(int number)
    {
        return new RealNumber(value.divide(new BigDecimal(number), new MathContext(100, RoundingMode.HALF_EVEN)));
    }

    public int compareTo(RealNumber number)
    {
        return value.compareTo(number.value);
    }

    public RealNumber negate()
    {
        return new RealNumber(value.negate());
    }

    public String toString()
    {
        return format.format(value);
    }

    public BigDecimal getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        return this.value.subtract(((RealNumber)obj).value).abs().compareTo(PRECISION_THRESHOLD.value) < 0;
    }

    public boolean equals(int number)
    {
        return equals(new RealNumber(number));
    }

    public boolean lessThan(RealNumber number)
    {
        if (equals(number))
            return false;
        return compareTo(number.add(PRECISION_THRESHOLD)) < 0;
    }

    public boolean lessThan(int number)
    {
        return lessThan(new RealNumber(number));
    }

    public boolean lessOrEqualTo(RealNumber number)
    {
        return compareTo(number.add(PRECISION_THRESHOLD)) <= 0;
    }

    public boolean lessOrEqualTo(int number)
    {
        return lessOrEqualTo(new RealNumber(number));
    }

    public boolean greaterThan(RealNumber number)
    {
        if (equals(number))
            return false;
        return add(PRECISION_THRESHOLD).compareTo(number) > 0;
    }

    public boolean greaterThan(int number)
    {
        return greaterThan(new RealNumber(number));
    }

    public boolean greaterOrEqualTo(RealNumber number)
    {
        return add(PRECISION_THRESHOLD).compareTo(number) >= 0;
    }

    public boolean greaterOrEqualTo(int number)
    {
        return greaterOrEqualTo(new RealNumber(number));
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
