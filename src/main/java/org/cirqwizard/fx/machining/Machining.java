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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.cirqwizard.fx.*;
import org.cirqwizard.fx.services.SerialInterfaceService;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.toolpath.Toolpath;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public abstract class Machining extends SettingsDependentScreenController implements Initializable
{
    @FXML protected PCBPaneFX pcbPane;
    @FXML protected ScrollPane scrollPane;

    @FXML protected Button goButton;

    @FXML protected Region veil;
    @FXML protected AnchorPane gcodePane;
    @FXML protected TextArea gcodeListing;

    @FXML protected BorderPane executionPane;
    @FXML protected ProgressBar executionProgressBar;
    @FXML protected Label timeElapsedLabel;

    @FXML protected BorderPane generationPane;
    @FXML protected Label generationStageLabel;
    @FXML protected ProgressBar overallProgressBar;
    @FXML protected Label machiningTimeEstimationLabel;
    protected StringProperty estimatedMachiningTimeProperty;
    @FXML protected Button stopGenerationButton;

    protected ToolpathGenerationService toolpathGenerationService;

    private PCBPaneMouseHandler mouseHandler;

    private SerialInterfaceService serialService;

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
        view.addEventFilter(KeyEvent.ANY, (event) ->
        {
            if (!event.isShortcutDown())
                pcbPane.setCursor(Cursor.CROSSHAIR);
            else
                pcbPane.setCursor(Cursor.DEFAULT);
        });

        mouseHandler = new PCBPaneMouseHandler(pcbPane);
        pcbPane.addEventFilter(MouseEvent.ANY, mouseHandler);

        gcodePane.addEventFilter(KeyEvent.KEY_PRESSED, (event) ->
        {
            if (event.getCode() == KeyCode.ESCAPE)
            {
                hideGCodeListing();
                event.consume();
            }
        });

        estimatedMachiningTimeProperty = new SimpleStringProperty();
        machiningTimeEstimationLabel.textProperty().bind(estimatedMachiningTimeProperty);
    }

    @Override
    public void settingsInvalidated()
    {
        restartService();
    }

    protected abstract ToolpathGenerationService getToolpathGenerationService();

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
            else if (keyZoomIn.match(event) && ("+".equals(event.getText()) || !System.getProperty("os.name").startsWith("Mac"))) // Workaround for PCBCAM-95 and CQ-86
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
        toolpathGenerationService = getToolpathGenerationService();
        generationStageLabel.textProperty().bind(toolpathGenerationService.generationStageProperty());
        serialService = new SerialInterfaceService(getMainApplication());
        mouseHandler.setService(toolpathGenerationService);
        generationPane.visibleProperty().bind(toolpathGenerationService.runningProperty());
        if (getMainApplication().getCNCController() == null)
            goButton.setDisable(true);
        else
            goButton.disableProperty().bind(toolpathGenerationService.runningProperty());
        pcbPane.toolpathsProperty().bind(toolpathGenerationService.valueProperty());

        executionProgressBar.progressProperty().bind(serialService.progressProperty());
        timeElapsedLabel.textProperty().bind(serialService.executionTimeProperty());
        executionPane.visibleProperty().bind(serialService.runningProperty());

        stopGenerationButton.setDisable(false);

        pcbPane.setBoardWidth(context.getBoardWidth());
        pcbPane.setBoardHeight(context.getBoardHeight());

        veil.visibleProperty().bind(toolpathGenerationService.runningProperty());
        pcbPane.repaint();
        toolpathGenerationService.start();
    }

    public void restartService()
    {
        if (!toolpathGenerationService.needsRestart())
            return;
        veil.visibleProperty().bind(toolpathGenerationService.runningProperty());
        stopGenerationButton.setDisable(false);
        toolpathGenerationService.restart();
    }

    public void zoomIn()
    {
        double scale = pcbPane.scaleProperty().getValue() + 0.005;
        scale = Math.max(scale, 0.005);
        scale = Math.min(scale, 1);
        pcbPane.scaleProperty().setValue(scale);
    }

    public void zoomOut()
    {
        double scale = pcbPane.scaleProperty().getValue() - 0.005;
        scale = Math.max(scale, 0.005);
        scale = Math.min(scale, 1);
        pcbPane.scaleProperty().setValue(scale);
    }

    public void flipHorizontal()
    {
        pcbPane.setFlipHorizontal(!pcbPane.isFlipHorizontal());
    }

    protected abstract Layer getCurrentLayer();

    public void selectAll()
    {
        for (Toolpath toolpath : toolpathGenerationService.getValue())
            toolpath.setSelected(true);
        pcbPane.repaint(toolpathGenerationService.getValue());
    }

    public void enableSelected()
    {
        ArrayList<Toolpath> changedToolpaths = new ArrayList<>();
        for (Toolpath toolpath : getCurrentLayer().getToolpaths())
        {
            if (toolpath.isSelected())
            {
                toolpath.setEnabled(true);
                toolpath.setSelected(false);
                changedToolpaths.add(toolpath);
            }
        }
        pcbPane.repaint(changedToolpaths);
    }


    public void disableSelected()
    {
        List<Toolpath> changedToolpaths = getCurrentLayer().getToolpaths().stream().
                filter(Toolpath::isSelected).map(t -> (Toolpath)t).collect(Collectors.toList());
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
        veil.visibleProperty().bind(serialService.runningProperty());
        serialService.setProgram(generateGCode());
        serialService.restart();
    }

    public void stopExecution()
    {
        serialService.cancel();
    }

    public void stopGeneration()
    {
        stopGenerationButton.setDisable(true);
        toolpathGenerationService.cancel();
    }
}
