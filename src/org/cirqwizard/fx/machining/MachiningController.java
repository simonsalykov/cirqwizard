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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.SceneController;
import org.cirqwizard.fx.State;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.fx.services.SerialInterfaceService;
import org.cirqwizard.fx.services.ToolpathGenerationService;
import org.cirqwizard.gcode.DrillGCodeGenerator;
import org.cirqwizard.gcode.MillingGCodeGenerator;
import org.cirqwizard.gcode.PasteGCodeGenerator;
import org.cirqwizard.gcode.TraceGCodeGenerator;
import org.cirqwizard.layers.Layer;
import org.cirqwizard.layers.SolderPasteLayer;
import org.cirqwizard.layers.TraceLayer;
import org.cirqwizard.post.RTPostprocessor;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.Toolpath;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class MachiningController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private PCBPaneFX pcbPane;
    @FXML private ScrollPane scrollPane;
    @FXML private TitledPane offsetsPane;
    @FXML private TitledPane miscPane;

    @FXML private RealNumberTextField toolDiameter;
    @FXML private RealNumberTextField feed;
    @FXML private Button goButton;
    @FXML private Button moveHeadAwayButton;

    @FXML private RealNumberTextField g54X;
    @FXML private RealNumberTextField g54Y;
    @FXML private RealNumberTextField g54Z;

    @FXML private RealNumberTextField clearance;
    @FXML private RealNumberTextField safetyHeight;
    @FXML private RealNumberTextField zFeed;

    @FXML private Region veil;
    @FXML private AnchorPane gcodePane;
    @FXML private TextArea gcodeListing;

    @FXML private BorderPane executionPane;
    @FXML private ProgressBar executionProgressBar;
    @FXML private Label timeElapsedLabel;

    @FXML private BorderPane generationPane;
    @FXML private Label generationStageLabel;
    @FXML private ProgressBar overallProgressBar;
    @FXML private Label machiningTimeEstimationLabel;
    private StringProperty estimatedMachiningTimeProperty = new SimpleStringProperty();
    @FXML private Button stopGenerationButton;

    private ToolpathGenerationService toolpathGenerationService;

    private PCBPaneMouseHandler mouseHandler;

    private SerialInterfaceService serialService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        final KeyCombination keyEnable = new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination keyDisable = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        final KeyCombination keySelectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
        final KeyCodeCombination keyZoomIn = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN);
        final KeyCodeCombination keyZoomOut = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);

        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                if (event.isShortcutDown())
                {
                    double scale = pcbPane.scaleProperty().getValue() + event.getDeltaY() / 10000.0;
                    scale = Math.max(scale, 0.005);
                    scale = Math.min(scale, 1);
                    pcbPane.scaleProperty().setValue(scale);
                    event.consume();
                }
            }
        });
        view.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event)
            {
                if (!event.isShortcutDown())
                    pcbPane.setCursor(Cursor.CROSSHAIR);
                else
                    pcbPane.setCursor(Cursor.DEFAULT);
            }
        });

        mouseHandler = new PCBPaneMouseHandler(pcbPane);
        pcbPane.addEventFilter(MouseEvent.ANY, mouseHandler);
        offsetsPane.expandedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean nowExpanded)
            {
                if (nowExpanded)
                    offsetsPane.toFront();
                else
                    offsetsPane.toBack();
            }
        });
        miscPane.expandedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean nowExpanded)
            {
                if (nowExpanded)
                    miscPane.toFront();
                else
                    miscPane.toBack();
            }
        });
        view.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event)
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
                else if (keyZoomIn.match(event) && "+".equals(event.getText())) // Workaround for PCBCAM-95
                {
                    zoomIn();
                    event.consume();
                }
                else if (keyZoomOut.match(event))
                {
                    zoomOut();
                    event.consume();
                }
            }
        });
        g54X.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getContext().setG54X(integer2);
            }
        });
        g54Y.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getContext().setG54Y(integer2);
            }
        });
        g54Z.realNumberIntegerProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                if (integer2 != null)
                    getMainApplication().getContext().setG54Z(integer2);
            }
        });

        gcodePane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCode() == KeyCode.ESCAPE)
                {
                    hideGCodeListing();
                    event.consume();
                }
            }
        });
        toolDiameter.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2)
            {
                if (!aBoolean2)
                    restartService();
            }
        });

        machiningTimeEstimationLabel.textProperty().bind(estimatedMachiningTimeProperty);
    }

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        State state = getMainApplication().getState();
        toolpathGenerationService = new ToolpathGenerationService(getMainApplication(), overallProgressBar.progressProperty(),
                estimatedMachiningTimeProperty);
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
        Settings settings = getMainApplication().getSettings();

        stopGenerationButton.setDisable(false);

        if (state == State.MILLING_TOP_INSULATION || state == State.MILLING_BOTTOM_INSULATION)
        {
            toolDiameter.setDisable(false);
            toolDiameter.setIntegerValue(settings.getDefaultTraceToolDiameter());
            feed.setIntegerValue(settings.getDefaultTracesFeedXY());

            clearance.setIntegerValue(settings.getDefaultTracesClearance());
            safetyHeight.setIntegerValue(settings.getDefaultTracesSafetyHeight());
            zFeed.setDisable(false);
            zFeed.setIntegerValue(settings.getDefaultTracesFeedZ());

            pcbPane.setGerberColor(state == State.MILLING_TOP_INSULATION ? PCBPaneFX.TOP_TRACE_COLOR : PCBPaneFX.BOTTOM_TRACE_COLOR);
            pcbPane.setToolpathColor(PCBPaneFX.ENABLED_TOOLPATH_COLOR);
            pcbPane.setGerberPrimitives(((TraceLayer)getCurrentLayer()).getElements());

            toolpathGenerationService.arcFeedProperty().set(feed.getIntegerValue() / 100 * settings.getDefaultTracesFeedArc());
        }
        else if (state == State.DRILLING)
        {
            toolDiameter.setDisable(true);
            toolDiameter.setText(context.getDrillDiameters().get(context.getCurrentDrill()));
            feed.setIntegerValue(settings.getDefaultDrillingFeed());

            clearance.setIntegerValue(settings.getDefaultDrillingClearance());
            safetyHeight.setIntegerValue(settings.getDefaultDrillingSafetyHeight());
            zFeed.setDisable(true);

            context.setG54Z(settings.getDefaultDrillingZOffset());

            pcbPane.setGerberColor(PCBPaneFX.DRILL_POINT_COLOR);
            pcbPane.setToolpathColor(PCBPaneFX.DRILL_POINT_COLOR);
            pcbPane.setGerberPrimitives(null);
        }
        else if (state == State.MILLING_CONTOUR)
        {
            toolDiameter.setDisable(true);
            toolDiameter.setText(context.getContourMillDiameter());
            feed.setIntegerValue(settings.getDefaultContourFeedXY());

            clearance.setIntegerValue(settings.getDefaultContourClearance());
            safetyHeight.setIntegerValue(settings.getDefaultContourSafetyHeight());
            zFeed.setDisable(false);
            zFeed.setIntegerValue(settings.getDefaultContourFeedZ());

            context.setG54Z(settings.getDefaultContourZOffset());

            pcbPane.setGerberPrimitives(null);
            pcbPane.setGerberColor(PCBPaneFX.CONTOUR_COLOR);
            pcbPane.setToolpathColor(PCBPaneFX.CONTOUR_COLOR);

            toolpathGenerationService.arcFeedProperty().set(feed.getIntegerValue() / 100 * settings.getDefaultContourFeedArc());
        }
        else if (state == State.DISPENSING)
        {
            toolDiameter.setDisable(false);
            toolDiameter.setIntegerValue(settings.getDefaultDispensingNeedleDiameter());
            feed.setIntegerValue(settings.getDefaultDispensingFeed());

            clearance.setIntegerValue(settings.getDefaultDispensingClearance());
            safetyHeight.setIntegerValue(null);
            safetyHeight.setDisable(true);
            zFeed.setIntegerValue(null);
            zFeed.setDisable(true);

            context.setG54Z(settings.getDefaultDispensingZOffset());

            pcbPane.setGerberColor(PCBPaneFX.SOLDER_PAD_COLOR);
            pcbPane.setToolpathColor(PCBPaneFX.PASTE_TOOLPATH_COLOR);
            pcbPane.setGerberPrimitives(((SolderPasteLayer)getCurrentLayer()).getElements());
        }

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

    private Layer getCurrentLayer()
    {
        Context context = getMainApplication().getContext();
        switch (getMainApplication().getState())
        {
            case MILLING_TOP_INSULATION: return context.getTopTracesLayer();
            case MILLING_BOTTOM_INSULATION: return context.getBottomTracesLayer();
            case DRILLING: return context.getDrillingLayer();
            case MILLING_CONTOUR: return context.getMillingLayer();
            case DISPENSING: return context.getSolderPasteLayer();
        }
        return null;
    }

    public void selectAll()
    {
        for (Toolpath toolpath : toolpathGenerationService.getValue())
            toolpath.setSelected(true);
        pcbPane.repaint(toolpathGenerationService.getValue());
    }

    public void enableSelected()
    {
        ArrayList<Toolpath> changedToolpaths = new ArrayList<Toolpath>();
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
        ArrayList<Toolpath> changedToolpaths = new ArrayList<Toolpath>();
        for (Toolpath toolpath : getCurrentLayer().getToolpaths())
        {
            if (toolpath.isSelected())
            {
                toolpath.setEnabled(false);
                toolpath.setSelected(false);
                changedToolpaths.add(toolpath);
            }
        }
        pcbPane.repaint(changedToolpaths);
    }

    private String generateGCode()
    {
        State state = getMainApplication().getState();
        Settings settings = getMainApplication().getSettings();

        if (state == State.MILLING_TOP_INSULATION || state == State.MILLING_BOTTOM_INSULATION)
        {
            int arcFeed = (feed.getIntegerValue() / 100 * settings.getDefaultTracesFeedArc());
            TraceGCodeGenerator generator = new TraceGCodeGenerator(getMainApplication().getContext(), state, settings);
            return generator.generate(new RTPostprocessor(), feed.getIntegerValue(), zFeed.getIntegerValue(), arcFeed,
                    clearance.getIntegerValue(), safetyHeight.getIntegerValue(), settings.getDefaultTracesWorkingHeight(),
                    settings.getDefaultTracesSpeed());

        }
        else if (state == State.DRILLING)
        {
            DrillGCodeGenerator generator = new DrillGCodeGenerator(getMainApplication().getContext());
            return generator.generate(new RTPostprocessor(), feed.getIntegerValue(), clearance.getIntegerValue(),
                    safetyHeight.getIntegerValue(), settings.getDefaultDrillingWorkingHeight(),
                    settings.getDefaultDrillingSpeed());
        }
        else if (state == State.MILLING_CONTOUR)
        {
            int arcFeed = (feed.getIntegerValue() / 100 * settings.getDefaultContourFeedArc());
            MillingGCodeGenerator generator = new MillingGCodeGenerator(getMainApplication().getContext());
            return generator.generate(new RTPostprocessor(), feed.getIntegerValue(), zFeed.getIntegerValue(), arcFeed,
                    clearance.getIntegerValue(), safetyHeight.getIntegerValue(), settings.getDefaultContourWorkingHeight(),
                    settings.getDefaultContourSpeed());
        }
        else if (state == State.DISPENSING)
        {
            PasteGCodeGenerator generator = new PasteGCodeGenerator(getMainApplication().getContext());
            return generator.generate(new RTPostprocessor(), settings.getDefaultDispensingPrefeedPause(),
                    settings.getDispensingPostfeedPause(), feed.getIntegerValue(), clearance.getIntegerValue(),
                    settings.getDefaultDispensingWorkingHeight());
        }

        return null;
    }

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
            getMainApplication().getCNCController().moveHeadAway(getMainApplication().getSettings().getFarAwayY());
    }
}
