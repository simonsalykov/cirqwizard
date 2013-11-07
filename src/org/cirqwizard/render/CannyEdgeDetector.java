package org.cirqwizard.render;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.util.IOUtils;
import org.bridj.Pointer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/**
 * http://www.tomgibara.com/computer-vision/CannyEdgeDetector.java
 * <p/>
 * <p><em>This software has been released into the public domain.
 * <strong>Please read the notes in this source file for additional information.
 * </strong></em></p>
 * <p/>
 * <p>This class provides a configurable implementation of the Canny edge
 * detection algorithm. This classic algorithm has a number of shortcomings,
 * but remains an effective tool in many scenarios. <em>This class is designed
 * for single threaded use only.</em></p>
 * <p/>
 * <p>Sample usage:</p>
 * <p/>
 * <pre><code>
 * //create the detector
 * CannyEdgeDetector detector = new CannyEdgeDetector();
 * //adjust its parameters as desired
 * detector.setLowThreshold(0.5f);
 * detector.setHighThreshold(1f);
 * //apply it to an image
 * detector.setSourceImage(frame);
 * detector.process();
 * BufferedImage edges = detector.getEdgesImage();
 * </code></pre>
 * <p/>
 * <p>For a more complete understanding of this edge detector's parameters
 * consult an explanation of the algorithm.</p>
 *
 * @author Tom Gibara
 */

public class CannyEdgeDetector
{

    // statics

    private final static float GAUSSIAN_CUT_OFF = 0.005f;
    private final static float MAGNITUDE_SCALE = 100F;
    private final static float MAGNITUDE_LIMIT = 1000F;
    private final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);

    // fields

    private int height;
    private int width;
    private int picsize;
    private byte[] sourceData;
    private int[] data;
    private int[] magnitude;
    private byte[] output;
    private BufferedImage sourceImage;
    private BufferedImage edgesImage;

    private float gaussianKernelRadius;
    private float lowThreshold;
    private float highThreshold;
    private int gaussianKernelWidth;
    private boolean contrastNormalized;

    // constructors

    /**
     * Constructs a new detector with default parameters.
     */

    public CannyEdgeDetector()
    {
        lowThreshold = 2.5f;
        highThreshold = 7.5f;
        gaussianKernelRadius = 2f;
        gaussianKernelWidth = 16;
        contrastNormalized = false;
    }

    // accessors

    /**
     * The image that provides the luminance data used by this detector to
     * generate edges.
     *
     * @return the source image, or null
     */

    public BufferedImage getSourceImage()
    {
        return sourceImage;
    }

    /**
     * Specifies the image that will provide the luminance data in which edges
     * will be detected. A source image must be set before the process method
     * is called.
     *
     * @param image a source of luminance data
     */

    public void setSourceImage(BufferedImage image)
    {
        sourceImage = image;
    }

    /**
     * Obtains an image containing the edges detected during the last call to
     * the process method. The buffered image is an opaque image of type
     * BufferedImage.TYPE_INT_ARGB in which edge pixels are white and all other
     * pixels are black.
     *
     * @return an image containing the detected edges, or null if the process
     *         method has not yet been called.
     */

    public BufferedImage getEdgesImage()
    {
        writeEdges(output);
        return edgesImage;
    }

    /**
     * Sets the edges image. Calling this method will not change the operation
     * of the edge detector in any way. It is intended to provide a means by
     * which the memory referenced by the detector object may be reduced.
     *
     * @param edgesImage expected (though not required) to be null
     */

    public void setEdgesImage(BufferedImage edgesImage)
    {
        this.edgesImage = edgesImage;
    }

    /**
     * The low threshold for hysteresis. The default value is 2.5.
     *
     * @return the low hysteresis threshold
     */

    public float getLowThreshold()
    {
        return lowThreshold;
    }

    /**
     * Sets the low threshold for hysteresis. Suitable values for this parameter
     * must be determined experimentally for each application. It is nonsensical
     * (though not prohibited) for this value to exceed the high threshold value.
     *
     * @param threshold a low hysteresis threshold
     */

    public void setLowThreshold(float threshold)
    {
        if (threshold < 0) throw new IllegalArgumentException();
        lowThreshold = threshold;
    }

    /**
     * The high threshold for hysteresis. The default value is 7.5.
     *
     * @return the high hysteresis threshold
     */

    public float getHighThreshold()
    {
        return highThreshold;
    }

    /**
     * Sets the high threshold for hysteresis. Suitable values for this
     * parameter must be determined experimentally for each application. It is
     * nonsensical (though not prohibited) for this value to be less than the
     * low threshold value.
     *
     * @param threshold a high hysteresis threshold
     */

    public void setHighThreshold(float threshold)
    {
        if (threshold < 0) throw new IllegalArgumentException();
        highThreshold = threshold;
    }

    /**
     * The number of pixels across which the Gaussian kernel is applied.
     * The default value is 16.
     *
     * @return the radius of the convolution operation in pixels
     */

    public int getGaussianKernelWidth()
    {
        return gaussianKernelWidth;
    }

    /**
     * The number of pixels across which the Gaussian kernel is applied.
     * This implementation will reduce the radius if the contribution of pixel
     * values is deemed negligable, so this is actually a maximum radius.
     *
     * @param gaussianKernelWidth a radius for the convolution operation in
     *                            pixels, at least 2.
     */

    public void setGaussianKernelWidth(int gaussianKernelWidth)
    {
        if (gaussianKernelWidth < 2) throw new IllegalArgumentException();
        this.gaussianKernelWidth = gaussianKernelWidth;
    }

    /**
     * The radius of the Gaussian convolution kernel used to smooth the source
     * image prior to gradient calculation. The default value is 16.
     *
     * @return the Gaussian kernel radius in pixels
     */

    public float getGaussianKernelRadius()
    {
        return gaussianKernelRadius;
    }

    /**
     * Sets the radius of the Gaussian convolution kernel used to smooth the
     * source image prior to gradient calculation.
     *
     * @return a Gaussian kernel radius in pixels, must exceed 0.1f.
     */

    public void setGaussianKernelRadius(float gaussianKernelRadius)
    {
        if (gaussianKernelRadius < 0.1f) throw new IllegalArgumentException();
        this.gaussianKernelRadius = gaussianKernelRadius;
    }

    /**
     * Whether the luminance data extracted from the source image is normalized
     * by linearizing its histogram prior to edge extraction. The default value
     * is false.
     *
     * @return whether the contrast is normalized
     */

    public boolean isContrastNormalized()
    {
        return contrastNormalized;
    }

    /**
     * Sets whether the contrast is normalized
     *
     * @param contrastNormalized true if the contrast should be normalized,
     *                           false otherwise
     */

    public void setContrastNormalized(boolean contrastNormalized)
    {
        this.contrastNormalized = contrastNormalized;
    }

    public byte[] getOutput()
    {
        return output;
    }

    // methods

    public void process()
    {
        width = sourceImage.getWidth();
        height = sourceImage.getHeight();
        picsize = width * height;
        initArrays();
        long t = System.currentTimeMillis();
        readLuminance();
        t = System.currentTimeMillis() - t;
//        System.out.println("luminance reading: " + t);

        byte firstLuminance = sourceData[0];
        boolean containsData = false;
        for (byte b : sourceData)
        {
            if (b != firstLuminance)
            {
                containsData = true;
                break;
            }
        }
        if (!containsData)
            return;

        t = System.currentTimeMillis();
        computeGradients(gaussianKernelRadius, gaussianKernelWidth);
        t = System.currentTimeMillis() - t;
//        System.out.println("GRADIENTS: " + t);
        int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
        int high = Math.round(highThreshold * MAGNITUDE_SCALE);
        t = System.currentTimeMillis();
        performHysteresis(low, high);
        t = System.currentTimeMillis() - t;
//        System.out.println("hysteresis: " + t);
        t = System.currentTimeMillis();
        output = thresholdEdges();
        t = System.currentTimeMillis() - t;
//        System.out.println("thresholding: " + t);
    }

    // private utility methods

    private void initArrays()
    {
        if (data == null || picsize != data.length)
        {
            data = new int[picsize];
        }
    }

    //NOTE: The elements of the method below (specifically the technique for
    //non-maximal suppression and the technique for gradient computation)
    //are derived from an implementation posted in the following forum (with the
    //clear intent of others using the code):
    //  http://forum.java.sun.com/thread.jspa?threadID=546211&start=45&tstart=0
    //My code effectively mimics the algorithm exhibited above.
    //Since I don't know the providence of the code that was posted it is a
    //possibility (though I think a very remote one) that this code violates
    //someone's intellectual property rights. If this concerns you feel free to
    //contact me for an alternative, though less efficient, implementation.

    private void computeGradients(float kernelRadius, int kernelWidth)
    {

        //generate the gaussian convolution masks
        float kernel[] = new float[kernelWidth];
        float diffKernel[] = new float[kernelWidth];
        int kwidth;
        for (kwidth = 0; kwidth < kernelWidth; kwidth++)
        {
            float g1 = gaussian(kwidth, kernelRadius);
            if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2) break;
            float g2 = gaussian(kwidth - 0.5f, kernelRadius);
            float g3 = gaussian(kwidth + 0.5f, kernelRadius);
            kernel[kwidth] = (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
            diffKernel[kwidth] = g3 - g2;
        }

        int initX = kwidth - 1;
        int maxX = width - (kwidth - 1);
        int initY = width * (kwidth - 1);
        int maxY = width * (height - (kwidth - 1));

        CLContext context = OpenCLUtil.getContext();
        Pointer<Byte> sourceDataPointer = Pointer.pointerToBytes(sourceData);
        Pointer<Float> diffKernelPointer = Pointer.pointerToFloats(diffKernel);
        CLBuffer<Byte> inputBuffer = context.createByteBuffer(CLMem.Usage.Input, sourceDataPointer);
        CLBuffer<Float> diffKernelBuffer = context.createFloatBuffer(CLMem.Usage.Input, diffKernelPointer);

        CLBuffer<Float> xGradientsOut = context.createFloatBuffer(CLMem.Usage.InputOutput, picsize);
        CLBuffer<Float> yGradientsOut = context.createFloatBuffer(CLMem.Usage.InputOutput, picsize);


        long t = System.currentTimeMillis();

        CLQueue queue = OpenCLUtil.getQueue();
        CLProgram program = OpenCLUtil.getProgram();

        CLKernel xGradientsKernel = program.createKernel("calculate_x_gradients");
        xGradientsKernel.setArgs(inputBuffer, initX, maxX, kwidth - 1, height - (kwidth - 1), width, kwidth, diffKernelBuffer, xGradientsOut);
        CLEvent xGradientsEvent = xGradientsKernel.enqueueNDRange(queue, new int[] { maxX, height - (kwidth - 1) });

        CLKernel yGradientsKernel = program.createKernel("calculate_y_gradients");
        yGradientsKernel.setArgs(inputBuffer, kwidth, width - kwidth, kwidth - 1, height - (kwidth - 1), width, kwidth, diffKernelBuffer, yGradientsOut);
        CLEvent yGradientsEvent = yGradientsKernel.enqueueNDRange(queue, new int[] { maxX, height - (kwidth - 1) });

        Pointer<Float> xOutPtr = xGradientsOut.read(queue, xGradientsEvent);
        Pointer<Float> yOutPtr = yGradientsOut.read(queue, yGradientsEvent);

        sourceDataPointer.release();

        t = System.currentTimeMillis() - t;
//        System.out.println("gradients: " + t);

        t = System.currentTimeMillis();
        initX = kwidth;
        maxX = width - kwidth;
        initY = width * kwidth;
        maxY = width * (height - kwidth);

        CLBuffer<Integer> magnitudeOut = context.createIntBuffer(CLMem.Usage.Output, picsize);
        CLKernel magnitudeKernel = program.createKernel("calculate_magnitude");
        magnitudeKernel.setArgs(xGradientsOut, yGradientsOut, initX, maxX, kwidth, height - kwidth , width, magnitudeOut);
        CLEvent magnitudeEvent = magnitudeKernel.enqueueNDRange(queue, new int[] { maxX, height - kwidth});
        Pointer<Integer> magnitudePtr = magnitudeOut.read(queue, magnitudeEvent);
        magnitude = magnitudePtr.getInts();

        xGradientsOut.release();
        yGradientsOut.release();
        magnitudePtr.release();

        t = System.currentTimeMillis() - t;
//        System.out.println("suppression: " + t);

    }

    private float gaussian(float x, float sigma)
    {
        return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
    }

    private void performHysteresis(int low, int high)
    {
        //NOTE: this implementation reuses the data array to store both
        //luminance data from the image, and edge intensity from the processing.
        //This is done for memory efficiency, other implementations may wish
        //to separate these functions.
        Arrays.fill(data, 0);

        int offset = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if (data[offset] == 0 && magnitude[offset] >= high)
                {
                    follow(x, y, offset, low);
                }
                offset++;
            }
        }
    }

    private void follow(int x1, int y1, int i1, int threshold)
    {
        int x0 = x1 == 0 ? x1 : x1 - 1;
        int x2 = x1 == width - 1 ? x1 : x1 + 1;
        int y0 = y1 == 0 ? y1 : y1 - 1;
        int y2 = y1 == height - 1 ? y1 : y1 + 1;

        data[i1] = magnitude[i1];
        for (int x = x0; x <= x2; x++)
        {
            for (int y = y0; y <= y2; y++)
            {
                int i2 = x + y * width;
                if ((y != y1 || x != x1)
                        && data[i2] == 0
                        && magnitude[i2] >= threshold)
                {
                    follow(x, y, i2, threshold);
                    return;
                }
            }
        }
    }

    private byte[] thresholdEdges()
    {
        byte[] output = new byte[picsize];
        for (int i = 0; i < picsize; i++)
        {
            output[i] = data[i] > 0 ? (byte) 1 : 0;
        }
        return output;
    }

    private void readLuminance()
    {
        int type = sourceImage.getType();
        if (type == BufferedImage.TYPE_BYTE_BINARY)
        {
            sourceData = (byte[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported image type: " + type);
        }
    }

    private void writeEdges(byte pixels[])
    {
        //NOTE: There is currently no mechanism for obtaining the edge data
        //in any other format other than an INT_ARGB type BufferedImage.
        //This may be easily remedied by providing alternative accessors.
        if (edgesImage == null)
        {
            edgesImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        }
        edgesImage.getWritableTile(0, 0).setDataElements(0, 0, width, height, pixels);
    }

}