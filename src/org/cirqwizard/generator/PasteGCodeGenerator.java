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

package org.cirqwizard.generator;

import org.cirqwizard.fx.Context;
import org.cirqwizard.geom.Curve;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.post.Postprocessor;
import org.cirqwizard.toolpath.CuttingToolpath;
import org.cirqwizard.toolpath.Toolpath;


public class PasteGCodeGenerator
{
    private Context context;

    public PasteGCodeGenerator(Context context)
    {
        this.context = context;
    }

    public String generate(Postprocessor postprocessor, String preFeedPause, String postFeedPause, String feed, String clearance, String workingHeight)
    {
        StringBuilder str = new StringBuilder();
        postprocessor.header(str);

        RealNumber x = new RealNumber(context.getG54X());
        RealNumber y = new RealNumber(context.getG54Y());
        RealNumber z = new RealNumber(context.getG54Z());
        postprocessor.setupG54(str, x, y, z);
        postprocessor.selectWCS(str);

        RealNumber _clearance = new RealNumber(clearance);
        RealNumber _workingHeight = new RealNumber(workingHeight);
        RealNumber _preFeedPause = new RealNumber(preFeedPause);
        RealNumber _postFeedPause = new RealNumber(postFeedPause);
        RealNumber _feed = new RealNumber(feed);

        postprocessor.rapid(str, null, null, _clearance);
        for (Toolpath toolpath : context.getSolderPasteLayer().getToolpaths())
        {
            if (!toolpath.isEnabled())
                continue;
            Curve curve = ((CuttingToolpath)toolpath).getCurve();
            postprocessor.rapid(str, curve.getFrom().getX(), curve.getFrom().getY(), _clearance);
            postprocessor.rapid(str, null, null, _workingHeight);
            postprocessor.syringeOn(str);
            postprocessor.pause(str, _preFeedPause);
            postprocessor.linearInterpolation(str, curve.getTo().getX(), curve.getTo().getY(),
                    _workingHeight, _feed);
            postprocessor.syringeOff(str);
            postprocessor.pause(str, _postFeedPause);
            postprocessor.rapid(str, null, null, _clearance);
        }
        postprocessor.footer(str);

        return str.toString();
    }
}
