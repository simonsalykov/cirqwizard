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

import javafx.scene.control.CheckBox;
import org.cirqwizard.fx.controls.RealNumberTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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


    private final static int REFERENCE_PIN_POSITION_ON_LAMINATE = 5000;


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
        int laminateX = Integer.valueOf(getMainApplication().getSettings().getMachineReferencePinX()) - REFERENCE_PIN_POSITION_ON_LAMINATE;
        int laminateY = Integer.valueOf(getMainApplication().getSettings().getMachineReferencePinY()) - REFERENCE_PIN_POSITION_ON_LAMINATE;
        double xOffsetToPcb = x - laminateX;
        double yOffsetToPcb  = y - laminateY;
        if (((xOffsetToPcb >= 0) && (xOffsetToPcb + context.getBoardWidth() <= context.getPcbSize().getWidth())) &&
            ((yOffsetToPcb >= 0) && (yOffsetToPcb + context.getBoardHeight() <= context.getPcbSize().getHeight())))
        {
            offsetErrorLabel.setVisible(false);
            continueButton.setDisable(false);
        }
        else
        {
            offsetErrorLabel.setVisible(true);
            continueButton.setDisable(true);
        }
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
}
