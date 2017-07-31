package org.cirqwizard.fx;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import org.cirqwizard.serial.CNCController;

import java.util.HashMap;

public class StatusIndicator extends Region
{
    private CNCController.Status status;

    private static HashMap<CNCController.Status, String> tooltips = new HashMap<>();

    static
    {
        tooltips.put(CNCController.Status.NOT_CONNECTED, "Machine is not detected");
        tooltips.put(CNCController.Status.ERROR, "Machine has reported an error. It is recommended to restart the application");
        tooltips.put(CNCController.Status.NOT_HOMED, "Your machine needs to be homed before any motion is performed");
        tooltips.put(CNCController.Status.OK, "All good!");
        tooltips.put(CNCController.Status.RUNNING, "Command is being executed");
    }

    public StatusIndicator()
    {
        setPrefSize(30, 30);
    }

    public void setStatus(CNCController.Status status)
    {
        this.status = status;
        getChildren().clear();
        Circle circle = new Circle(15, 15, 10);
        Tooltip tooltip = new Tooltip();
        Tooltip.install(circle, tooltip);
        circle.getStyleClass().add("status-" + status);
        tooltip.setText(tooltips.get(status));
        getChildren().add(circle);
    }

}
