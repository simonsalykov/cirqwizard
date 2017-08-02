package org.cirqoid.cnc.analyzer;

/**
 * Created by simon on 21.06.17.
 */
public class AS5311Analyzer
{
    private int misoBit;
    private int sckBit;
    private int csBit;
    private boolean invert;

    private int reading;
    private int bitNumber;
    private int lastReading;
    private double position;

    private boolean lastCs;
    private boolean lastSck;

    public AS5311Analyzer(int misoBit, int sckBit, int csBit, boolean invert)
    {
        this.misoBit = misoBit;
        this.sckBit = sckBit;
        this.csBit = csBit;
        this.invert = invert;
    }

    public void tick(int value)
    {
        boolean miso = (value & misoBit) != 0;
        boolean sck = (value & sckBit) != 0;
        boolean cs = (value & csBit) != 0;

        if (lastCs && !cs) // Falling edge of CS
        {
            reading = 0;
            bitNumber = 0;
        }

        if (!cs && !lastSck && sck && bitNumber < 16) // Rising edge of SCK with CS down
        {
            reading = (reading << 1) | (miso ? 1 : 0);
            bitNumber++;
        }
        if (bitNumber == 16)
        {
            int result = (reading >> 3) & 0x0FFF;
            if (lastReading > 3072 && result < 1024)
                position += 2;
            else if (lastReading < 1024 && result > 3072)
                position -= 2;
            lastReading = result;
        }

        lastSck = sck;
        lastCs = cs;
    }

    public double getPosition()
    {
        return (position + (double)lastReading * 2 / 4096) * (invert ? -1 : 1);
    }

    public boolean isValidReading()
    {
        boolean ocf = (reading & 0x04) != 0;
        boolean cof = (reading & 0x02) != 0;
        boolean lin = (reading & 0x01) != 0;
        return ocf && !cof && !lin;
    }
}
