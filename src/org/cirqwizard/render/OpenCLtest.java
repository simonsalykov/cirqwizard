package org.cirqwizard.render;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.util.*;
import com.nativelibs4java.util.*;
import org.bridj.Pointer;
import static org.bridj.Pointer.*;
import static java.lang.Math.*;
import java.io.IOException;
import java.nio.ByteOrder;


/**
 * Created with IntelliJ IDEA.
 * User: simon
 * Date: 07/11/13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class OpenCLtest
{
    public void test() throws IOException
    {
        CLContext context = JavaCL.createBestContext();
        CLQueue queue = context.createDefaultQueue();

        int n = 1024;
        Pointer<Float>
                aPtr = allocateFloats(n),
                bPtr = allocateFloats(n);

        for (int i = 0; i < n; i++) {
            aPtr.set(i, (float)cos(i));
            bPtr.set(i, (float)sin(i));
        }

        // Create OpenCL input buffers (using the native memory pointers aPtr and bPtr) :
        CLBuffer<Float>
                a = context.createFloatBuffer(Usage.Input, aPtr),
                b = context.createFloatBuffer(Usage.Input, bPtr);

        // Create an OpenCL output buffer :
        CLBuffer<Float> out = context.createFloatBuffer(Usage.Output, n);

        // Read the program sources and compile them :
        String src = IOUtils.readText(this.getClass().getResource("opencl_test.cl"));
        CLProgram program = context.createProgram(src);

        // Get and call the kernel :
        CLKernel addFloatsKernel = program.createKernel("add_floats");
        addFloatsKernel.setArgs(a, b, out, n);
        int[] globalSizes = new int[] { n };
        CLEvent addEvt = addFloatsKernel.enqueueNDRange(queue, globalSizes);

        Pointer<Float> outPtr = out.read(queue, addEvt); // blocks until add_floats finished

        // Print the first 10 output values :
        for (int i = 0; i < 10 && i < n; i++)
            System.out.println("out[" + i + "] = " + outPtr.get(i));
    }
}
