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
package org.cirqwizard.fx.util;

import com.sun.javafx.util.Utils;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.stage.Popup;
import org.cirqwizard.fx.popover.PopOverController;

public class ToolbarPopup extends Popup
{
    public ToolbarPopup(PopOverController controller)
    {
        super();
        setAutoHide(true);
        setHideOnEscape(true);
        getContent().add(controller.getView());
        controller.setPopup(this);
    }

    public void show(Node anchor)
    {
        Point2D p = Utils.pointRelativeTo(anchor, -1, -1, HPos.CENTER, VPos.BOTTOM, 0, 0, true);
        show(anchor, p.getX(), p.getY());

    }
}
