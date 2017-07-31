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

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqoid.cnc.controller.commands.StatusResponse;
import org.cirqoid.cnc.controller.serial.SerialInterface;
import org.cirqwizard.fx.popover.ManualControlPopOver;
import org.cirqwizard.fx.popover.OffsetsPopOver;
import org.cirqwizard.fx.popover.SettingsPopOver;
import org.cirqwizard.fx.util.ToolbarPopup;

import java.util.ArrayList;
import java.util.List;

public class MainViewController extends ScreenController
{
    @FXML private HBox breadCrumbBarBox;
    @FXML private AnchorPane contentPane;
    @FXML private Hyperlink offsetsLink;
    @FXML private Hyperlink settingsLink;
    @FXML private Hyperlink manualControlLink;
    @FXML private StatusIndicator statusIndicator;

    private ManualControlPopOver manualControlPopOver = new ManualControlPopOver();
    private OffsetsPopOver offsetsPopOver = new OffsetsPopOver();
    private SettingsPopOver settingsPopOver = new SettingsPopOver();

    private ScreenController currentScreen;

    @Override
    protected String getFxmlName()
    {
        return "MainView.fxml";
    }

    @FXML
    public void initialize()
    {
        settingsLink.managedProperty().bind(settingsLink.visibleProperty());
        statusIndicator.setStatus(StatusIndicator.Status.NOT_CONNECTED);
    }

    public ScreenController getCurrentScreen()
    {
        return currentScreen;
    }

    public void setScreen(ScreenController screen)
    {
        if (currentScreen != null)
            currentScreen.onDeactivation();
        if (currentScreen != null && currentScreen.getShortcutHandler() != null)
            view.removeEventFilter(KeyEvent.KEY_PRESSED, currentScreen.getShortcutHandler());
        this.currentScreen = screen;

        contentPane.getChildren().clear();
        screen.refresh();
        contentPane.getChildren().add(screen.getView());
        AnchorPane.setTopAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setLeftAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setRightAnchor(contentPane.getChildren().get(0), 0.0);
        AnchorPane.setBottomAnchor(contentPane.getChildren().get(0), 0.0);
        updateBreadCrumbBar(getPath(screen));
        settingsLink.setVisible(screen instanceof SettingsDependentScreenController);
        if (currentScreen.getShortcutHandler() != null)
            view.addEventFilter(KeyEvent.KEY_PRESSED, currentScreen.getShortcutHandler());
    }

    private List<ScreenController> getPath(ScreenController scene)
    {
        ArrayList<ScreenController> path = new ArrayList<>();
        for (; scene != null; scene = scene.getParent())
        {
            if (scene instanceof ScreenGroup && !((ScreenGroup)scene).isVisible())
                continue;
            path.add(0, scene);
        }
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

            b.setOnAction(event ->
            {

                List<MenuItem> contextMenuItems = new ArrayList<>();
                List<ScreenController> siblings = getMainApplication().getSiblings(item);
                if (siblings != null)
                {
                    for (ScreenController sibling : siblings)
                    {
                        if (sibling instanceof ScreenGroup && !((ScreenGroup)sibling).isVisible())
                            continue;
                        MenuItem it = new MenuItem(sibling.getName());
                        it.setOnAction(e -> sibling.select());
                        it.setDisable(!sibling.isEnabled());
                        contextMenuItems.add(it);
                    }

                    ContextMenu contextMenu = new ContextMenu(contextMenuItems.toArray(new MenuItem[contextMenuItems.size()]));
                    Button button = (Button) event.getSource();
                    final Scene scene = button.getScene();
                    final Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
                    final Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
                    final Point2D nodeCoord = button.localToScene(0.0, 0.0);
                    final double clickX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
                    final double clickY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY() + button.getHeight());
                    contextMenu.show((Node) event.getSource(), clickX, clickY);
                }
                else
                    item.select();
            });

            breadCrumbBarBox.getChildren().add(b);
        }
    }

    @Override
    public ScreenController setMainApplication(MainApplication mainApplication)
    {
        manualControlPopOver.setMainApplication(mainApplication);
        offsetsPopOver.setMainApplication(mainApplication);
        settingsPopOver.setMainApplication(mainApplication);
        return super.setMainApplication(mainApplication);
    }

    public void manualControl()
    {
        ToolbarPopup popup = new ToolbarPopup(manualControlPopOver);
        popup.show(manualControlLink);
    }

    public void enableManualControl()
    {
        //manualControlLink.setDisable(false);
    }

    public void disableManualControl()
    {
        //manualControlLink.setDisable(true);
    }

    public void offsets()
    {
        ToolbarPopup popup = new ToolbarPopup(offsetsPopOver);
        offsetsPopOver.refresh();
        popup.show(offsetsLink);
    }

    public void settings()
    {
        ToolbarPopup popup = new ToolbarPopup(settingsPopOver);
        SettingsDependentScreenController screen = (SettingsDependentScreenController) currentScreen;
        screen.populateSettingsGroup(settingsPopOver.getPane(), screen);
        popup.show(settingsLink);
    }

    public void addStatuUpdateHook(SerialInterface serialInterface)
    {
        serialInterface.addListener(Response.Code.STATUS, response ->
        {
            int runLevel = ((StatusResponse) response).getRunLevel();
            switch (runLevel)
            {
                case 0: statusIndicator.setStatus(StatusIndicator.Status.ERROR); break;
                case 1: statusIndicator.setStatus(StatusIndicator.Status.NOT_HOMED); break;
                case 2: statusIndicator.setStatus(StatusIndicator.Status.OK); break;
            }
            manualControlLink.setDisable(runLevel < 1);
        });

    }

}
