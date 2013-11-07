__kernel void add_floats(__global const float* a, __global const float* b, __global float* out, int n)
{
    int i = get_global_id(0);
    if (i >= n)
        return;

    out[i] = a[i] + b[i];
}

__kernel void fill_in_values(__global float* a, __global float* b, int n)
{
    int i = get_global_id(0);
    if (i >= n)
        return;

    a[i] = cos((float)i);
    b[i] = sin((float)i);
}

__kernel void calculate_x_gradients(__global const char* sourceData,
    int initX, int maxX,
    int initY, int maxY,
    int width, int kwidth,
    __global const float *diffKernel,  __global float* out)
{
    int x = get_global_id(0);
    if (x < initX || x >= maxX)
        return;
    int y = get_global_id(1);
    if (y < initY || y >= maxY)
       return;
    y *= width;

    float sum = 0;
    int index = x + y;
    for (int i = 1; i < kwidth; i++)
        sum += diffKernel[i] * (sourceData[index - i] - sourceData[index + i]);

    out[index] = sum;
}

__kernel void calculate_y_gradients(__global const char* sourceData,
    int initX, int maxX,
    int initY, int maxY,
    int width, int kwidth,
    __global const float *diffKernel,  __global float* out)
{
    int x = get_global_id(0);
    if (x < initX || x >= maxX)
        return;
    int y = get_global_id(1);
    if (y < initY || y >= maxY)
        return;
    y *= width;

    float sum = 0.0;
    int index = x + y;
    int yOffset = width;
    for (int i = 1; i < kwidth; i++)
    {
        sum += diffKernel[i] * (sourceData[index - yOffset] - sourceData[index + yOffset]);
        yOffset += width;
    }

    out[index] = sum;
}
