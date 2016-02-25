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

package org.cirqwizard.fx.pp;

import org.cirqwizard.fx.OperationsScreenGroup;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.ScreenGroup;
import org.cirqwizard.generation.toolpath.PPPoint;
import org.cirqwizard.layers.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PPGroup extends OperationsScreenGroup
{
    private List<ScreenController> dynamicChildren;

    public PPGroup(String name)
    {
        super(name);
    }

    @Override
    protected boolean isEnabled()
    {
        return super.isEnabled() && !getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.PLACEMENT).isEmpty();
    }

    @Override
    public List<ScreenController> getChildren()
    {
        List<ScreenController> children = new ArrayList<>(super.getChildren());
        if (!isEnabled())
            return children;

        children.addAll(getDynamicChildren());
        return children;
    }

    public void resetDynamicChildren()
    {
        dynamicChildren = null;
    }

    private List<ScreenController> getDynamicChildren()
    {
        if (dynamicChildren != null)
            return dynamicChildren;

        dynamicChildren = getMainApplication().getContext().getPanel().getCombinedElements(Board.LayerType.PLACEMENT).stream().
                map(c -> ((PPPoint)c).getId()).
                distinct().
                map(c ->
                new ScreenGroup(c.getPackaging() + " " + c.getValue())
                {
                    @Override
                    public void select()
                    {
                        getMainApplication().getContext().setCurrentComponent(c);
                        super.select();
                    }

                    @Override
                    public ScreenController getParent()
                    {
                        return PPGroup.this;
                    }

                }.setMainApplication(getMainApplication()).
                        addChild(new FeederSelection().setMainApplication(getMainApplication())).
                        addChild(new ComponentPlacement().setMainApplication(getMainApplication()))).
                collect(Collectors.toList());

        return dynamicChildren;
    }
}
