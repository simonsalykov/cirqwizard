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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

import java.util.ArrayList;
import java.util.List;

public class MainViewController extends ScreenController
{
    @FXML private HBox breadCrumbBarBox;
    @FXML private AnchorPane contentPane;
    @FXML private Hyperlink manualControlLink;

    private ManualControlPopOver manualControlPopOver = new ManualControlPopOver();
    PopOver popOver;

    private ScreenController currentScreen;

    @Override
    protected String getFxmlName()
    {
        return "MainView.fxml";
    }


    public ScreenController getCurrentScreen()
    {
        return currentScreen;
    }

    public void setScreen(ScreenController screen)
    {
        this.currentScreen = screen;

        contentPane.getChildren().clear();
        screen.refresh();
        contentPane.getChildren().add(screen.getView());
        AnchorPane.setTopAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setLeftAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setRightAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setBottomAnchor(contentPane.getChildren().get(0), 0.0);
        updateBreadCrumbBar(getPath(screen));

    }

    private List<ScreenController> getPath(ScreenController scene)
    {
        ArrayList<ScreenController> path = new ArrayList<>();
        for (; scene != null; scene = scene.getParent())
            path.add(0, scene);
        return path;
    }

    private void updateBreadCrumbBar(List<ScreenController> path)
    {
        breadCrumbBarBox.getChildren().clear();
        for (int i = 0; i < path.size(); i++)
        {
            final ScreenController item = path.get(i);
            Button b = new Button(item.getName());
            b.setFocusTraversable(false);
            b.getStyleClass().setAll("button");
            if (path.size() == 1)
                b.getStyleClass().add("only-button");
            else if (i == 0)
                b.getStyleClass().add("first-button");
            else if (i == path.size() - 1)
                b.getStyleClass().add("last-button");
            else
                b.getStyleClass().addAll("middle-button");

            List<Action> contextMenuActions = new ArrayList<>();
            List<ScreenController> siblings = getMainApplication().getSiblings(item);
            if (siblings != null)
            {
                for (ScreenController sibling : siblings)
                {
                    AbstractAction action = new AbstractAction(sibling.getName())
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            sibling.select();
                        }
                    };
                    action.setDisabled(!sibling.isEnabled());
                    contextMenuActions.add(action);
                }
                ContextMenu contextMenu = ActionUtils.createContextMenu(contextMenuActions);
                b.setOnAction(event ->
                {
                    Button button = (Button) event.getSource();
                    final Scene scene = button.getScene();
                    final Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
                    final Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
                    final Point2D nodeCoord = button.localToScene(0.0, 0.0);
                    final double clickX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
                    final double clickY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY() + button.getHeight());
                    contextMenu.show((Node) event.getSource(), clickX, clickY);
                });
            }
            else
                b.setOnAction(event -> item.select());

            breadCrumbBarBox.getChildren().add(b);
        }
    }

    public void manualControl()
    {
        if (popOver == null)
        {
            popOver = new PopOver(manualControlPopOver.getView());
            popOver.setDetachedTitle("Manual control");
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
            getMainApplication().getPrimaryStage().setOnCloseRequest(event -> popOver.hide(javafx.util.Duration.millis(0)));
        }
        popOver.show(manualControlLink);
    }

}
