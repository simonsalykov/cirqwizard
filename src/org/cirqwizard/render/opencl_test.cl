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


__kernel void calculate_magnitude(__global const float* xGradient, __global const float *yGradient,
    int initX, int maxX,
    int initY, int maxY,
    int width,
    __global int* out)
{
    int x = get_global_id(0);
    if (x < initX || x >= maxX)
        return;
    int y = get_global_id(1);
    if (y < initY || y >= maxY)
        return;
    y *= width;

    int index = x + y;
    int indexN = index - width;
    int indexS = index + width;
    int indexW = index - 1;
    int indexE = index + 1;
    int indexNW = indexN - 1;
    int indexNE = indexN + 1;
    int indexSW = indexS - 1;
    int indexSE = indexS + 1;

    float xGrad = xGradient[index];
    float yGrad = yGradient[index];
    float gradMag = hypot(xGrad, yGrad);

    float tmp;
    char b;

    if (xGrad * yGrad <= 0)
    {
        if (fabs(xGrad) >= fabs(yGrad))
        {
            float wMag = hypot(xGradient[indexW], yGradient[indexW]);
            float eMag = hypot(xGradient[indexE], yGradient[indexE]);
            float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
            float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);

            tmp = fabs(xGrad * gradMag);
            b = tmp >= fabs(yGrad * neMag - (xGrad + yGrad) * eMag)
                    && tmp > fabs(yGrad * swMag - (xGrad + yGrad) * wMag);
        }
        else
        {
            float nMag = hypot(xGradient[indexN], yGradient[indexN]);
            float sMag = hypot(xGradient[indexS], yGradient[indexS]);
            float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
            float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);

            tmp = fabs(yGrad * gradMag);
            b = tmp >= fabs(xGrad * neMag - (yGrad + xGrad) * nMag)
                    && tmp > fabs(xGrad * swMag - (yGrad + xGrad) * sMag);
        }
    }
    else
    {
        if (fabs(xGrad) >= fabs(yGrad))
        {
            float wMag = hypot(xGradient[indexW], yGradient[indexW]);
            float eMag = hypot(xGradient[indexE], yGradient[indexE]);
            float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
            float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);

            tmp = fabs(xGrad * gradMag);
            b = tmp >= fabs(yGrad * seMag + (xGrad - yGrad) * eMag)
                    && tmp > fabs(yGrad * nwMag + (xGrad - yGrad) * wMag);
        }
        else
        {
            float nMag = hypot(xGradient[indexN], yGradient[indexN]);
            float sMag = hypot(xGradient[indexS], yGradient[indexS]);
            float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
            float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);

            tmp = fabs(yGrad * gradMag);
            b = tmp >= fabs(xGrad * seMag + (yGrad - xGrad) * sMag)
                    && tmp > fabs(xGrad * nwMag + (yGrad - xGrad) * nMag);
        }
    }

    if (b)
        out[index] = gradMag >= 1000 ? 1000 : (int) (100 * gradMag);
    else
        out[index] = 0;
}
