package org.cirqwizard.fx.util;

import com.sun.javafx.util.Utils;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.stage.Popup;
import org.cirqwizard.fx.popover.PopOverController;

/**
 * Created by simon on 24.09.15.
 */
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
