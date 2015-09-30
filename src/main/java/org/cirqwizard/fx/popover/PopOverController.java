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

package org.cirqwizard.fx.popover;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;

public abstract class PopOverController
{
    private VBox box;
    @FXML protected Parent view;
    protected MainApplication mainApplication;
    protected Popup popup;
    private Label closeIcon;

    public PopOverController()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getFxmlName()));
            loader.setController(this);
            loader.load();

            box = new VBox();
            box.setSpacing(5);
            box.getStyleClass().add("popover");
            closeIcon = new Label();
            closeIcon.setGraphic(createCloseIcon());
            closeIcon.getStyleClass().add("icon");
            closeIcon.setPadding(new Insets(10, 0, 0, 10));
            closeIcon.managedProperty().bind(closeIcon.visibleProperty());
            closeIcon.setOnMouseClicked(event -> popup.hide());
            box.getChildren().add(closeIcon);
            box.getChildren().add(view);
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error loading FXML", e);
        }
    }

    protected abstract String getFxmlName();

    public Parent getView()
    {
        return box;
    }

    public MainApplication getMainApplication()
    {
        return mainApplication;
    }

    public void setMainApplication(MainApplication mainApplication)
    {
        this.mainApplication = mainApplication;
    }

    public Popup getPopup()
    {
        return popup;
    }

    public void setPopup(Popup popup)
    {
        this.popup = popup;
    }

    protected Node createCloseIcon()
    {
        Group group = new Group();
        group.getStyleClass().add("graphics"); //$NON-NLS-1$

        Circle circle = new Circle();
        circle.getStyleClass().add("circle"); //$NON-NLS-1$
        circle.setRadius(6);
        circle.setCenterX(6);
        circle.setCenterY(6);
        circle.setFill(Color.GRAY);
        circle.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.color(0, 0, 0, 0.2), 3, 0.5, 1.0, 1.0));

        group.getChildren().add(circle);

        Line line1 = new Line();
        line1.getStyleClass().add("line"); //$NON-NLS-1$
        line1.setStartX(4);
        line1.setStartY(4);
        line1.setEndX(8);
        line1.setEndY(8);
        line1.setStroke(Color.WHITE);
        line1.setStrokeWidth(2);
        group.getChildren().add(line1);

        Line line2 = new Line();
        line2.getStyleClass().add("line"); //$NON-NLS-1$
        line2.setStartX(8);
        line2.setStartY(4);
        line2.setEndX(4);
        line2.setEndY(8);
        line2.setStroke(Color.WHITE);
        line2.setStrokeWidth(2);
        group.getChildren().add(line2);

        return group;
    }

}
