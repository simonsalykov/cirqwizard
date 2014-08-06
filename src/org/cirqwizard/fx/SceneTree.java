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

package org.cirqwizard.fx;

import java.util.ArrayList;
import java.util.List;

public class SceneTree
{
    private static SceneEnum root = SceneEnum.Welcome.
            addChild(SceneEnum.Orientation).
            addChild(SceneEnum.Homing).
        addChild(SceneEnum.TopTraces.
                addChild(SceneEnum.PCBPlacement).
                addChild(SceneEnum.Message).
                addChild(SceneEnum.ZOffset));

    public static SceneEnum getRoot()
    {
        return root;
    }

    public static List<SceneEnum> getPath(SceneEnum scene)
    {
        ArrayList<SceneEnum> path = new ArrayList<>();
        for (; scene != null; scene = scene.getParent())
            path.add(0, scene);
        return path;
    }


    public static List<SceneEnum> getSiblings(SceneEnum scene)
    {
        return scene.getParent() == null ? null : scene.getParent().getChildren();
    }

    public static SceneEnum getNext(SceneEnum scene)
    {
        int index = getSiblings(scene).indexOf(scene);
        SceneEnum next;
        if (index < getSiblings(scene).size() - 1)
            next = getSiblings(scene).get(index + 1);
        else
            next = getNext(scene.getParent());
        return getVisibleChild(next);
    }

    public static SceneEnum getVisibleChild(SceneEnum scene)
    {
        if (scene.getFxml() != null)
            return scene;
        for (SceneEnum s : scene.getChildren())
            if (getVisibleChild(s) != null)
                return getVisibleChild(s);
        return null;
    }

}
