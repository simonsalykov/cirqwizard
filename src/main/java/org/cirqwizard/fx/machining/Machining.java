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

package org.cirqwizard.fx.machining;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.services.SerialInterfaceCommandsService;
import org.cirqwizard.generation.toolpath.Toolpath;
import org.cirqwizard.layers.Board;
import org.cirqwizard.logging.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public abstract class Machining extends SettingsDependentScreenController implements Initializable
{
    @FXML protected PCBPane pcbPane;
    @FXML protected ScrollPane scrollPane;

    @FXML protected Button goButton;
    @FXML protected Button showGCodeButton;

    @FXML protected Region veil;
    @FXML protected VBox gcodePane;
    @FXML protected TextArea gcodeListing;

    @FXML protected VBox executionPane;
    @FXML protected ProgressBar executionProgressBar;
    @FXML protected Label timeElapsedLabel;


    private SerialInterfaceCommandsService serialService;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/machining/Machining.fxml";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        scrollPane.addEventFilter(ScrollEvent.ANY, (event) ->
        {
            if (event.isShortcutDown())
            {
                double scale = pcbPane.scaleProperty().getValue() + event.getDeltaY() / 10000.0;
                scale = Math.max(scale, 0.005);
                scale = Math.min(scale, 1);
                pcbPane.scaleProperty().setValue(scale);
                event.consume();
            }
        });
        view.addEventFilter(KeyEvent.ANY, event -> pcbPane.setCursor(!event.isShortcutDown() ? Cursor.CROSSHAIR : Cursor.DEFAULT));

        PCBPaneMouseHandler mouseHandler = new PCBPaneMouseHandler(pcbPane);
        mouseHandler.toolpathsProperty().bind(pcbPane.toolpathsProperty());
        pcbPane.addEventFilter(MouseEvent.ANY, mouseHandler);

        gcodePane.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.getCode() == KeyCode.ESCAPE)
            {
                hideGCodeListing();
                event.consume();
            }
        });

    }

    protected abstract void generateToolpaths();

    @Override
    public void settingsInvalidated()
    {
        generateToolpaths();
    }

    private class ShortcutHandler implements EventHandler<KeyEvent>
    {
        private final KeyCombination keyEnable = new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN);
        private final KeyCombination keyDisable = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        private final KeyCombination keySelectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
        private final KeyCodeCombination keyZoomIn = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN);
        private final KeyCodeCombination keyZoomOut = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);
        private final KeyCodeCombination keyFlipHorizontal = new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN);

        @Override
        public void handle(KeyEvent event)
        {
            if (event.isConsumed())
                return;
            if (keyEnable.match(event))
            {
                enableSelected();
                event.consume();
            }
            else if (keyDisable.match(event))
            {
                disableSelected();
                event.consume();
            }
            else if (keySelectAll.match(event))
            {
                selectAll();
                event.consume();
            }
            else if (keyZoomIn.match(event))
            {
                zoomIn();
                event.consume();
            }
            else if (keyZoomOut.match(event))
            {
                zoomOut();
                event.consume();
            }
            else if (keyFlipHorizontal.match(event))
            {
                flipHorizontal();
                event.consume();
            }
        }
    }

    private ShortcutHandler shortcutHandler = new ShortcutHandler();

    @Override
    public EventHandler<? super KeyEvent> getShortcutHandler()
    {
        return shortcutHandler;
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        serialService = new SerialInterfaceCommandsService(getMainApplication());
        if (!goButton.disableProperty().isBound())
            goButton.setDisable(isRunningDisabled());
        showGCodeButton.setDisable(isWcsDefined());
        executionProgressBar.progressProperty().bind(serialService.progressProperty());
        timeElapsedLabel.textProperty().bind(serialService.executionTimeProperty());
        executionPane.visibleProperty().bind(serialService.runningProperty());

        pcbPane.setBoardWidth(context.getPanel().getSize().getWidth());
        pcbPane.setBoardHeight(context.getPanel().getSize().getHeight());
        pcbPane.toolpathsProperty().setValue(null);
        pcbPane.repaint();
        generateToolpaths();
        pcbPane.repaint();
    }

    protected boolean isRunningDisabled()
    {
        return getMainApplication().getCNCController() == null ||
                isWcsDefined();
    }

    private boolean isWcsDefined()
    {
        return getMainApplication().getContext().getG54X() == null ||
                getMainApplication().getContext().getG54Y() == null ||
                getMainApplication().getContext().getG54Z() == null;
    }

    private void zoom(double factor)
    {
        double scale = pcbPane.scaleProperty().getValue() * factor;
        scale = Math.max(scale, 0.005);
        scale = Math.min(scale, 0.03);
        pcbPane.scaleProperty().setValue(scale);
    }

    public void zoomIn()
    {
        zoom(1.5);
    }

    public void zoomOut()
    {
        zoom(1 / 1.5);
    }

    public void flipHorizontal()
    {
        pcbPane.setFlipHorizontal(!pcbPane.isFlipHorizontal());
    }

    protected abstract Board.LayerType getCurrentLayer();

    public void selectAll()
    {
        pcbPane.toolpathsProperty().getValue().stream().forEach(t -> t.setSelected(true));
        pcbPane.repaint(pcbPane.toolpathsProperty().getValue());
    }

    public void enableSelected()
    {
        List<Toolpath> changedToolpaths = pcbPane.toolpathsProperty().getValue().stream().
                filter(Toolpath::isSelected).collect(Collectors.toList());
        changedToolpaths.forEach(t ->
        {
            t.setEnabled(true);
            t.setSelected(false);

        });
        pcbPane.repaint(changedToolpaths);
    }


    public void disableSelected()
    {
        List<Toolpath> changedToolpaths = pcbPane.toolpathsProperty().getValue().stream().
                filter(Toolpath::isSelected).collect(Collectors.toList());
        changedToolpaths.forEach(toolpath ->
        {
            toolpath.setEnabled(false);
            toolpath.setSelected(false);
        });

        pcbPane.repaint(changedToolpaths);
    }

    protected abstract String generateGCode();

    public void showGCodeListing()
    {
        veil.visibleProperty().unbind();
        veil.setVisible(true);
        gcodePane.setVisible(true);
        gcodeListing.setText(generateGCode());
        gcodeListing.requestFocus();
    }

    public void hideGCodeListing()
    {
        gcodePane.setVisible(false);
        veil.visibleProperty().bind(serialService.runningProperty());
    }


    public void executeProgram()
    {
        try
        {
            List<Command> commands = getMainApplication().getCNCController().parseBlocks(generateGCode());
            veil.visibleProperty().bind(serialService.runningProperty());
            serialService.setCommands(commands);
            serialService.restart();
        }
        catch (ParsingException e)
        {
            LoggerFactory.logException("Error parsing generated gcode", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error in generated gcode");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Failed command: " + e.getFailedBlock());
            alert.showAndWait();
        }
    }

    public void stopExecution()
    {
        serialService.cancel();
    }

    public void stopGeneration()
    {
    }
}
