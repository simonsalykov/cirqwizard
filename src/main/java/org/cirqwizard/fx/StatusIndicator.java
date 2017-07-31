package org.cirqwizard.fx;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

public class StatusIndicator extends Region
{
    public enum Status
    {
        NOT_CONNECTED("status-not-connected", "Machine is not detected"),
        ERROR("status-error", "Machine has reported an error. It is recommended to restart the application"),
        NOT_HOMED("status-not-homed", "Your machine needs to be homed before any motion is performed"),
        OK("status-ok", "All green!"),
        RUNNING("status-running", "Program is being executed");

        private String styleName;
        private String tooltip;

        Status(String styleName, String tooltip)
        {
            this.styleName = styleName;
            this.tooltip = tooltip;
        }
    }

    private Status status;
    private Circle circle;
    private Tooltip tooltip;

    public StatusIndicator()
    {
        setPrefSize(30, 30);
        circle = new Circle(15, 15, 10);
        circle.getStyleClass().add("status-not-connected");
        tooltip = new Tooltip();
        Tooltip.install(circle, tooltip);
        getChildren().add(circle);
    }

    public void setStatus(Status status)
    {
        this.status = status;
        circle.getStyleClass().removeAll();
        circle.getStyleClass().add(status.styleName);
        tooltip.setText(status.tooltip);
    }


}
