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

package org.cirqwizard.fx.common;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.cirqwizard.fx.misc.SettingsEditor;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.ApplicationValues;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.settings.SettingsGroup;

import java.net.URL;
import java.util.ResourceBundle;


public class XYOffsets extends ScreenController implements Initializable
{
    private SettingsGroup[] settingsGroups;
    @FXML private RealNumberTextField x;
    @FXML private RealNumberTextField y;

    @FXML private Button goButton;
    @FXML private Button continueButton;

    @FXML private VBox offsetErrorPane;
    @FXML private CheckBox ignoreCheckBox;

    @FXML private Pane positionPreviewPane;

    @FXML private VBox settingsMissingErrorPane;
    @FXML private Label missingSettingLabel;

    private final static int REFERENCE_PIN_POSITION_ON_LAMINATE = 5000;

    private Canvas offsetImageCanvas;

    public XYOffsets(SettingsGroup... settingsGroup)
    {
        this.settingsGroups = settingsGroup;
    }

    @Override
    protected String getFxmlName()
    {
        return "XYOffsets.fxml";
    }

    @Override
    protected String getName()
    {
        return "X and Y offsets";
    }

    @Override
    protected boolean isMandatory()
    {
        Context context = getMainApplication().getContext();
        for (SettingsGroup g : settingsGroups)
            if (g.validate() != null)
                return true;
        return context.getG54X() == null || context.getG54Y() == null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        ChangeListener<String> changeListener = (v, oldV, newV) -> updateComponents();
        x.realNumberTextProperty().addListener(changeListener);
        y.realNumberTextProperty().addListener(changeListener);
        ApplicationValues applicationValues = SettingsFactory.getApplicationValues();
        x.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            checkOffsetLimit(newV == null ? null : newV, y.getIntegerValue());
            getMainApplication().getContext().setG54X(newV == null ? null : newV);
            applicationValues.getG54X().setValue(newV);
            applicationValues.save();
        });
        y.realNumberIntegerProperty().addListener((v, oldV, newV) ->
        {
            checkOffsetLimit(x.getIntegerValue(), newV == null ? null : newV);
            getMainApplication().getContext().setG54Y(newV == null ? null : newV);
            applicationValues.getG54Y().setValue(newV);
            applicationValues.save();
        });
        ignoreCheckBox.selectedProperty().addListener((v, oldV, newV) ->
                        checkOffsetLimit(getMainApplication().getContext().getG54X(), getMainApplication().getContext().getG54Y()));
        offsetImageCanvas = new Canvas(positionPreviewPane.getPrefWidth(), positionPreviewPane.getPrefHeight());
        positionPreviewPane.getChildren().addAll(offsetImageCanvas);
    }

    @Override
    public void refresh()
    {
        Integer x = getMainApplication().getContext().getG54X();
        if (x == null)
            x = SettingsFactory.getApplicationValues().getG54X().getValue();
        this.x.setIntegerValue(x);
        Integer y = getMainApplication().getContext().getG54Y();
        if (y == null)
            y = SettingsFactory.getApplicationValues().getG54Y().getValue();
        this.y.setIntegerValue(y);
        getMainApplication().getContext().setG54X(x);
        getMainApplication().getContext().setG54Y(y);
        updateComponents();

        String missingSetting = SettingsFactory.getApplicationSettings().validate();
        if (missingSetting != null)
            missingSetting = "The following setting is not set: Application -> " + missingSetting;
        else
        {
            for (SettingsGroup g : settingsGroups)
            {
                missingSetting = g.validate();
                if (missingSetting != null)
                    missingSetting = "The following setting is not set: " + g.getName() + " -> " + missingSetting;
            }
        }
        settingsMissingErrorPane.setVisible(missingSetting != null);
        missingSettingLabel.setText(missingSetting);
    }

    private void checkOffsetLimit(Integer x, Integer y)
    {
        if (x == null || y == null)
            return;
        Context context = getMainApplication().getContext();
        int laminateX = SettingsFactory.getMachineSettings().getReferencePinX().getValue() - REFERENCE_PIN_POSITION_ON_LAMINATE;
        int laminateY = SettingsFactory.getMachineSettings().getReferencePinY().getValue() - REFERENCE_PIN_POSITION_ON_LAMINATE;
        double xOffsetToPcb = x - laminateX;
        double yOffsetToPcb  = y - laminateY;
        boolean pcbFitsLaminate = ((xOffsetToPcb >= 0) && (xOffsetToPcb + context.getBoardWidth() <= context.getPcbSize().getWidth())) &&
            ((yOffsetToPcb >= 0) && (yOffsetToPcb + context.getBoardHeight() <= context.getPcbSize().getHeight()));
        offsetErrorPane.setVisible(!pcbFitsLaminate);
        continueButton.setDisable(!pcbFitsLaminate && !(ignoreCheckBox.isSelected() && ignoreCheckBox.isVisible()));
        updateOffsetImage(context.getPcbSize(), xOffsetToPcb, yOffsetToPcb, context.getBoardWidth(), context.getBoardHeight());
    }

    public void updateComponents()
    {
        if (x.getRealNumberText() == null || y.getRealNumberText() == null)
            continueButton.setDisable(true);
        else
            checkOffsetLimit(x.getIntegerValue(), y.getIntegerValue());

        goButton.setDisable(getMainApplication().getCNCController() == null ||
                x.getRealNumberText() == null || y.getRealNumberText() == null);
    }

    public void moveXY()
    {
        if (!goButton.isDisabled())
            getMainApplication().getCNCController().moveTo(x.getIntegerValue(), y.getIntegerValue());
    }

    public void updateOffsetImage(PCBSize pcbSize, double pcbX, double pcbY, double pcbWidth, double pcbHeight)
    {
        GraphicsContext gc = offsetImageCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, offsetImageCanvas.getWidth(), offsetImageCanvas.getHeight());

        double scale = 2;
        double xOffset = (positionPreviewPane.getPrefWidth() * ApplicationConstants.RESOLUTION - pcbSize.getWidth() * scale) / 2 / ApplicationConstants.RESOLUTION;
        double yOffset = (positionPreviewPane.getPrefHeight() * ApplicationConstants.RESOLUTION - pcbSize.getHeight() * scale) / 2 / ApplicationConstants.RESOLUTION;
        double pinRadius = 1.5 * scale;

        double pinX1 = (REFERENCE_PIN_POSITION_ON_LAMINATE / ApplicationConstants.RESOLUTION);
        double pinX2 = (pcbSize.getWidth() - REFERENCE_PIN_POSITION_ON_LAMINATE) / ApplicationConstants.RESOLUTION;
        double pinY1 = (REFERENCE_PIN_POSITION_ON_LAMINATE / ApplicationConstants.RESOLUTION);
        double pinY2 = (pcbSize.getHeight() - REFERENCE_PIN_POSITION_ON_LAMINATE) / ApplicationConstants.RESOLUTION;

        gc.setFill(Color.GRAY);
        gc.fillRect(xOffset, yOffset, pcbSize.getWidth() * 2 / ApplicationConstants.RESOLUTION, pcbSize.getHeight() * 2 / ApplicationConstants.RESOLUTION);

        gc.setFill(Color.WHITE);
        gc.fillOval(pinX1 * scale + xOffset - pinRadius, pinY1 * scale + yOffset - pinRadius, pinRadius * 2, pinRadius * 2);
        gc.fillOval(pinX2 * scale + xOffset - pinRadius, pinY1 * scale + yOffset - pinRadius, pinRadius * 2, pinRadius * 2);
        gc.fillOval(pinX1 * scale + xOffset - pinRadius, pinY2 * scale + yOffset - pinRadius, pinRadius * 2, pinRadius * 2);
        gc.fillOval(pinX2 * scale + xOffset - pinRadius, pinY2 * scale + yOffset - pinRadius, pinRadius * 2, pinRadius * 2);

        double px = xOffset + pcbX  * scale / ApplicationConstants.RESOLUTION;
        double py = yOffset + (pcbSize.getHeight() - pcbY - pcbHeight) * scale / ApplicationConstants.RESOLUTION;
        double pw = pcbWidth * scale / ApplicationConstants.RESOLUTION;
        double ph = pcbHeight * scale / ApplicationConstants.RESOLUTION;

        gc.setFill(Color.rgb(191, 255, 0, 0.8));
        gc.fillRect(px, py, pw, ph);
    }

    public void goToSettings()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(SettingsEditor.class));
    }
}
