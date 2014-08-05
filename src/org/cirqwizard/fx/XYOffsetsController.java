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

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.cirqwizard.fx.controls.RealNumberTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.SettingsFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class XYOffsetsController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private RealNumberTextField x;
    @FXML private RealNumberTextField y;
    @FXML private RealNumberTextField z;

    @FXML private Button goButton;
    @FXML private Button moveZButton;
    @FXML private Label offsetErrorLabel;

    @FXML private Button continueButton;
    @FXML private CheckBox ignoreCheckBox;

    @FXML private Pane offsetImage;

    private final static int REFERENCE_PIN_POSITION_ON_LAMINATE = 5000;

    private Canvas offsetImageCanvas;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        ChangeListener<String> changeListener = new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                updateComponents();
            }
        };
        x.realNumberTextProperty().addListener(changeListener);
        y.realNumberTextProperty().addListener(changeListener);
        z.realNumberTextProperty().addListener(changeListener);
        x.realNumberIntegerProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2)
            {
                checkOffsetLimit(number2 == null ? null : number2.intValue(), y.getIntegerValue());
                getMainApplication().getContext().setG54X(number2 == null ? null : number2.intValue());
            }
        });
        y.realNumberIntegerProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2)
            {
                checkOffsetLimit(x.getIntegerValue(), number2 == null ? null : number2.intValue());
                getMainApplication().getContext().setG54Y(number2 == null ? null : number2.intValue());
            }
        });
        ignoreCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2)
            {
                checkOffsetLimit(getMainApplication().getContext().getG54X(), getMainApplication().getContext().getG54Y());
            }
        });
        offsetImageCanvas = new Canvas(offsetImage.getPrefWidth(), offsetImage.getPrefHeight());
        offsetImage.getChildren().addAll(offsetImageCanvas);
    }

    @Override
    public void refresh()
    {
        x.setIntegerValue(getMainApplication().getContext().getG54X());
        y.setIntegerValue(getMainApplication().getContext().getG54Y());
        updateComponents();
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
        offsetErrorLabel.setVisible(!pcbFitsLaminate);
        ignoreCheckBox.setVisible(!pcbFitsLaminate);
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
        moveZButton.setDisable(getMainApplication().getCNCController() == null || z.getRealNumberText() == null);
    }

    public void moveXY()
    {
        if (!goButton.isDisabled())
            getMainApplication().getCNCController().moveTo(x.getIntegerValue(), y.getIntegerValue());
    }

    public void moveZ()
    {
        if (!moveZButton.isDisabled())
            getMainApplication().getCNCController().moveZ(z.getIntegerValue());
    }

    public void updateOffsetImage(PCBSize pcbSize, double pcbX, double pcbY, double pcbWidth, double pcbHeight)
    {
        GraphicsContext gc = offsetImageCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, offsetImageCanvas.getWidth(), offsetImageCanvas.getHeight());

        double scale = 2;
        double xOffset = (offsetImage.getPrefWidth() * ApplicationConstants.RESOLUTION - pcbSize.getWidth() * scale) / 2 / ApplicationConstants.RESOLUTION;
        double yOffset = (offsetImage.getPrefHeight() * ApplicationConstants.RESOLUTION - pcbSize.getHeight() * scale) / 2 / ApplicationConstants.RESOLUTION;
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
}
