package org.cirqwizard.fx.dispensing;

import org.cirqwizard.layers.Board;

/**
 * Created by simon on 29.06.17.
 */
public class TopDispensing extends Dispensing
{
    @Override
    protected Board.LayerType getCurrentLayer()
    {
        return Board.LayerType.SOLDER_PASTE_TOP;
    }

    @Override
    protected boolean mirror()
    {
        return false;
    }
}
