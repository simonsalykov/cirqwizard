package org.cirqwizard.render;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: simon
 * Date: 07/11/13
 * Time: 18:41
 */
public class OpenCLUtil
{
    private static CLContext context = JavaCL.createBestContext();
    private static CLQueue queue = context.createDefaultQueue();

    private static CLProgram program;

    public static CLContext getContext()
    {
        return context;
    }

    public static CLQueue getQueue()
    {
        return queue;
    }

    public static CLProgram getProgram()
    {
        if (program == null)
        {
            String src = null;
            try
            {
                src = IOUtils.readText(OpenCLUtil.class.getResource("opencl_test.cl"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            program = context.createProgram(src);
        }
        return program;
    }
}
