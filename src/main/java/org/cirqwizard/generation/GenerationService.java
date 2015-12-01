/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cirqwizard.generation;

import org.cirqwizard.fx.Context;
import org.cirqwizard.generation.optimizer.Chain;
import org.cirqwizard.layers.Layer;

import java.util.List;

public abstract class GenerationService extends ProcessingService
{
    private Layer layer;

    public GenerationService(Context context, Layer layer)
    {
        super(context);
        this.layer = layer;
    }

    public Layer getLayer()
    {
        return layer;
    }

    public abstract List<Chain> generate();
}
