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
package org.cirqwizard.generation;

import java.awt.image.BufferedImage;

public class SimpleEdgeDetector
{
    private byte[] sourceData;
    private byte[] output;
    private int width;
    private int height;

    public SimpleEdgeDetector(BufferedImage image)
    {
        sourceData = (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void process()
    {
        output = new byte[sourceData.length];
        for (int y = 1; y < height - 1; y++)
        {
            final int _y = y;
            for (int x = 1; x < width - 1; x++)
            {
                int index = _y * width + x;
                if (sourceData[index] == 0 &&
                        (sourceData[index - 1] != 0 || sourceData[index + 1] != 0 || sourceData[index - width] != 0 || sourceData[index + width] != 0))
                    output[index] = 1;
            }
        }
    }

    public byte[] getOutput()
    {
        return output;
    }

    public BufferedImage getOutputImage()
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        image.getWritableTile(0, 0).setDataElements(0, 0, width, height, output);
        return image;
    }
}
