package org.cirqoid.cnc.controller.settings;

/**
 * Created by simon on 28.06.17.
 */
public class HardwareSettings
{
    private static final HardwareSettings CIRQOID_SETTINGS = new HardwareSettings(new Axis[]{
            new Axis(0, 100_000),
            new Axis(0, 235_000),
            new Axis(-25_000, 0),
            new Axis(-1_000_000, 1_000_000)
    });

    private Axis axes[];

    public HardwareSettings(Axis[] axes)
    {
        this.axes = axes;
    }

    public Axis[] getAxes()
    {
        return axes;
    }

    public static HardwareSettings getCirqoidSettings()
    {
        return CIRQOID_SETTINGS;
    }
}
