package org.cirqwizard.render;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: simon
 * Date: 08/11/13
 * Time: 02:33
 * To change this template use File | Settings | File Templates.
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
//        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int y = 1; y < height - 1; y++)
        {
            final int _y = y;
//            pool.submit(new Runnable()
//            {
//                @Override
//                public void run()
//                {
                    for (int x = 1; x < width - 1; x++)
                    {
                        int index = _y * width + x;
                        if (sourceData[index] == 0 &&
                                (sourceData[index - 1] != 0 || sourceData[index + 1] != 0 || sourceData[index - width] != 0 || sourceData[index + width] != 0 ||
                                        sourceData[index - 1 - width] != 0 || sourceData[index + 1 - width] != 0 || sourceData[index - 1 + width] != 0 || sourceData[index + 1 + width] != 0))
                            output[index] = 1;
                    }
//                }
//            });
        }
//        try
//        {
//            pool.shutdown();
//            pool.awaitTermination(10, TimeUnit.DAYS);
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
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
