package org.cirqwizard.generation.gcode;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.settings.MachineSettings;
import org.cirqwizard.settings.SettingsFactory;

/**
 * Created by simon on 29.06.17.
 */
public class GCodeGenerator
{
    private Context context;
    private boolean mirror;

    public GCodeGenerator(Context context, boolean mirror)
    {
        this.context = context;
        this.mirror = mirror;
    }

    protected int getX(int x)
    {
        return mirror ? -x : x;
    }

    protected int getG54X()
    {
        int g54X = context.getG54X();
        if (mirror)
        {
            MachineSettings machineSettings = SettingsFactory.getMachineSettings();
            int laminateWidth = context.getPanel().getSize() == PCBSize.Small ? machineSettings.getSmallPcbWidth().getValue() : machineSettings.getLargePcbWidth().getValue();
            int pinX = machineSettings.getReferencePinX().getValue();
            g54X = pinX * 2 + laminateWidth - context.getG54X();
        }
        return g54X;
    }
}
