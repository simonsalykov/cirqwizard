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

package org.cirqwizard.test.fx;

import org.cirqwizard.fx.PCBSize;

import org.junit.Test;
import static org.junit.Assert.*;


public class PCBSizeTest
{
    private PCBSize largePcb = PCBSize.Large;

    @Test
    public void testPCBSmallerThanLaminate()
    {
        assertTrue(largePcb.checkFit(10, 10));
    }

    @Test
    public void testPCBLargerThanLaminate()
    {
        assertFalse(largePcb.checkFit(200, 200));
    }

    @Test
    public void testPCBSizeEqualToLaminate()
    {
        assertTrue(largePcb.checkFit(100, 160));
    }

    @Test
    public void testPCBWidthEqualToLaminate()
    {
        assertTrue(largePcb.checkFit(100, 50));
    }

    @Test
    public void testPCBHeightEqualToLaminate()
    {
        assertTrue(largePcb.checkFit(50, 160));
    }

    @Test
    public void testPCBSizeCheckTolerance()
    {
        assertTrue(largePcb.checkFit(100.1, 160.1));
    }
}