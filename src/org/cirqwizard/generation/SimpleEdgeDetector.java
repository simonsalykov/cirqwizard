package org.cirqwizard.generation;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: simon
 * Date: 08/11/13
 * Time: 02:33
 */
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
