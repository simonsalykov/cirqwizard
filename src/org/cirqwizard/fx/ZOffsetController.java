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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.settings.ApplicationValues;
import org.cirqwizard.settings.InsulationMillingSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;


public class ZOffsetController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private Button continueButton;

    @FXML private RadioButton manualEntryRadioButton;
    @FXML private RealNumberTextField manualZOffset;

    @FXML private RadioButton automaticEntryRadioButton;

    @FXML private TitledPane scrapPlacePane;
    @FXML private RealNumberTextField scrapPlaceX;
    @FXML private RealNumberTextField scrapPlaceY;
    @FXML private Button goButton;

    @FXML private TitledPane zOffsetPane;
    @FXML private RealNumberTextField automaticZOffset;
    @FXML private RadioButton horizontalTestCut;
    @FXML private RadioButton verticalTestCut;
    @FXML private Button testButton;
    @FXML private Button lowerTestButton;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");


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
        manualZOffset.realNumberTextProperty().addListener(changeListener);
        scrapPlaceX.realNumberTextProperty().addListener(changeListener);
        scrapPlaceY.realNumberTextProperty().addListener(changeListener);
        automaticZOffset.realNumberTextProperty().addListener(changeListener);
        scrapPlaceX.realNumberIntegerProperty().addListener((v, oldV, newV) -> SettingsFactory.getApplicationValues().getScrapPlaceX().setValue(newV));
        scrapPlaceY.realNumberIntegerProperty().addListener((v, oldV, newV) -> SettingsFactory.getApplicationValues().getScrapPlaceY().setValue(newV));
        ChangeListener<Boolean> testCutDirectionListener = (v, oldV, newV) -> SettingsFactory.getApplicationValues().getTestCutDirection().setValue(
                horizontalTestCut.isSelected());
        horizontalTestCut.selectedProperty().addListener(testCutDirectionListener);
        verticalTestCut.selectedProperty().addListener(testCutDirectionListener);
    }

    @Override
    public void refresh()
    {
        if (!getMainApplication().getContext().iszOffsetEstablished())
        {
            manualEntryRadioButton.setSelected(false);
            automaticEntryRadioButton.setSelected(false);
            manualZOffset.setText("");
            automaticZOffset.setIntegerValue(SettingsFactory.getInsulationMillingSettings().getZOffset().getValue());
        }
        else if (getMainApplication().getContext().getG54Z() != null)
            automaticZOffset.setIntegerValue(getMainApplication().getContext().getG54Z());

        ApplicationValues applicationValues = SettingsFactory.getApplicationValues();
        scrapPlaceX.setIntegerValue(applicationValues.getScrapPlaceX().getValue());
        scrapPlaceY.setIntegerValue(applicationValues.getScrapPlaceY().getValue());
        if (applicationValues.getTestCutDirection().getValue() != null)
            horizontalTestCut.setSelected(applicationValues.getTestCutDirection().getValue());
        updateComponents();
    }

    public void updateComponents()
    {
        continueButton.setDisable(!manualEntryRadioButton.isSelected() && !automaticEntryRadioButton.isSelected());
        if (manualEntryRadioButton.isSelected() && manualZOffset.getRealNumberText() == null)
            continueButton.setDisable(true);
        if (automaticEntryRadioButton.isSelected() && automaticZOffset.getRealNumberText() == null)
            continueButton.setDisable(true);

        manualZOffset.setDisable(!manualEntryRadioButton.isSelected());

        scrapPlacePane.setDisable(!automaticEntryRadioButton.isSelected());
        goButton.setDisable(getMainApplication().getCNCController() == null || scrapPlaceX.getRealNumberText() == null ||
                scrapPlaceY.getRealNumberText() == null);

        zOffsetPane.setDisable(!automaticEntryRadioButton.isSelected());
        if (goButton.isDisabled())
            zOffsetPane.setDisable(true);

        testButton.setDisable(getMainApplication().getCNCController() == null || automaticZOffset.getRealNumberText() == null ||
                (!horizontalTestCut.isSelected() && !verticalTestCut.isSelected()));
        lowerTestButton.setDisable(testButton.isDisabled());
    }

    public void moveXY()
    {
        if (!goButton.isDisabled())
            getMainApplication().getCNCController().moveTo(scrapPlaceX.getIntegerValue(), scrapPlaceY.getIntegerValue());
    }

    public void test()
    {
        if (testButton.isDisabled())
            return;
        InsulationMillingSettings settings = SettingsFactory.getInsulationMillingSettings();
        getMainApplication().getCNCController().testCut(scrapPlaceX.getIntegerValue(), scrapPlaceY.getIntegerValue(), automaticZOffset.getIntegerValue(),
                settings.getClearance().getValue(), settings.getSafetyHeight().getValue(), settings.getWorkingHeight().getValue(),
                settings.getFeedXY().getValue(), settings.getFeedZ().getValue(), settings.getSpeed().getValue(), horizontalTestCut.isSelected());
    }

    public void lowerAndTest()
    {
        automaticZOffset.setText(decimalFormat.format(Double.valueOf(automaticZOffset.getRealNumberText()) - 0.02));
        test();
    }

    public void next()
    {
        Context context = getMainApplication().getContext();
        if (manualEntryRadioButton.isSelected())
            context.setG54Z(manualZOffset.getIntegerValue());
        else if (automaticEntryRadioButton.isSelected())
            context.setG54Z(automaticZOffset.getIntegerValue());
        context.setzOffsetEstablished(true);

        super.next();
    }
}
