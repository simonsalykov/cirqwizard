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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.fx.services.SerialInterfaceService;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.Toolpath;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public abstract class Machining extends ScreenController implements Initializable
{
    @FXML protected PCBPaneFX pcbPane;
    @FXML protected ScrollPane scrollPane;
    @FXML protected TitledPane offsetsPane;
    @FXML protected TitledPane miscPane;

    @FXML protected RealNumberTextField toolDiameter;
    @FXML protected RealNumberTextField feed;
    @FXML protected Button goButton;
    @FXML protected Button moveHeadAwayButton;

    @FXML protected RealNumberTextField g54X;
    @FXML protected RealNumberTextField g54Y;
    @FXML protected RealNumberTextField g54Z;

    @FXML protected RealNumberTextField clearance;
    @FXML protected RealNumberTextField safetyHeight;
    @FXML protected RealNumberTextField zFeed;

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
        final KeyCombination keyEnable = new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination keyDisable = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination keySelectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
        final KeyCodeCombination keyZoomIn = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN);
        final KeyCodeCombination keyZoomOut = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);

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
        offsetsPane.expandedProperty().addListener((v, oldV, newV) ->
        {
            if (newV)
                offsetsPane.toFront();
            else
                offsetsPane.toBack();
        });
        miscPane.expandedProperty().addListener((v, oldV, newV) ->
        {
            if (newV)
                miscPane.toFront();
            else
                miscPane.toBack();
        });
        view.addEventFilter(KeyEvent.KEY_PRESSED, (event) ->
        {
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
        });
        g54X.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            if (newV != null)
                getMainApplication().getContext().setG54X(newV);
        });
        g54Y.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            if (newV != null)
                getMainApplication().getContext().setG54Y(newV);
        });
        g54Z.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            if (newV != null)
                getMainApplication().getContext().setG54Z(newV);
        });

        gcodePane.addEventFilter(KeyEvent.KEY_PRESSED, (event) ->
        {
            if (event.getCode() == KeyCode.ESCAPE)
            {
                hideGCodeListing();
                event.consume();
            }
        });
        toolDiameter.focusedProperty().addListener((v, oldV, newV) ->
        {
            if (!newV)
                restartService();
        });

        estimatedMachiningTimeProperty = new SimpleStringProperty();
        machiningTimeEstimationLabel.textProperty().bind(estimatedMachiningTimeProperty);
    }

    protected ToolpathGenerationService getToolpathGenerationService()
    {
        return null;
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
        {
            goButton.setDisable(true);
            moveHeadAwayButton.setDisable(true);
        }
        else
            goButton.disableProperty().bind(toolpathGenerationService.runningProperty());
        pcbPane.toolpathsProperty().bind(toolpathGenerationService.valueProperty());

        executionProgressBar.progressProperty().bind(serialService.progressProperty());
        timeElapsedLabel.textProperty().bind(serialService.executionTimeProperty());
        executionPane.visibleProperty().bind(serialService.runningProperty());

        stopGenerationButton.setDisable(false);

        g54X.setIntegerValue(context.getG54X());
        g54Y.setIntegerValue(context.getG54Y());
        g54Z.setIntegerValue(context.getG54Z());

        pcbPane.setBoardWidth(context.getBoardWidth());
        pcbPane.setBoardHeight(context.getBoardHeight());

        toolpathGenerationService.toolDiameterProperty().bind(toolDiameter.realNumberIntegerProperty());
        toolpathGenerationService.feedProperty().bind(feed.realNumberIntegerProperty());
        if (zFeed.realNumberIntegerProperty().getValue() != null)
            toolpathGenerationService.zFeedProperty().bind(zFeed.realNumberIntegerProperty());
        toolpathGenerationService.clearanceProperty().bind(clearance.realNumberIntegerProperty());
        if (safetyHeight.realNumberIntegerProperty().getValue() != null)
            toolpathGenerationService.safetyHeightProperty().bind(safetyHeight.realNumberIntegerProperty());
        veil.visibleProperty().bind(toolpathGenerationService.runningProperty());
        toolpathGenerationService.start();
    }

    public void restartService()
    {
        if (toolpathGenerationService.getLastToolDiameter() != null && toolpathGenerationService.getLastToolDiameter().equals(toolDiameter.getIntegerValue()))
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
                filter(Toolpath::isSelected).collect(Collectors.toList());
        changedToolpaths.stream().forEach(toolpath ->
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

    public void moveHeadAway()
    {
        if (getMainApplication().getCNCController() != null)
            getMainApplication().getCNCController().moveHeadAway(SettingsFactory.getMachineSettings().getFarAwayY().getValue());
    }
}
