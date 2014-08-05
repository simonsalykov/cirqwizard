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

package org.cirqwizard.fx.pp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.SceneController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.settings.SettingsFactory;
import org.cirqwizard.toolpath.PPPoint;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;


public class FeederSelectionController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private Label header;
    @FXML private Label countOfComponents;
    @FXML private RadioButton smallFeeder;
    @FXML private RadioButton mediumFeeder;
    @FXML private RadioButton largeFeeder;
    @FXML private Button moveHeadAwayButton;

    @FXML private ComboBox<Integer> row;
    @FXML private RealNumberTextField pitch;

    @FXML private Button continueButton;

    private ObservableList<Integer> rows;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        rows = FXCollections.observableArrayList();
        row.setItems(rows);

        row.valueProperty().addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2)
            {
                updateControls();
            }
        });
        pitch.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                updateControls();
            }
        });
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        ComponentId id =  context.getComponentIds().get(context.getCurrentComponent());

        int count=0;
        for (PPPoint component : context.getComponentsLayer().getPoints())
        {
            if (component.getId().equals(id))
                count++;
        }
        countOfComponents.setText("You will need " + count + " such component(s)");

        header.setText("Placing component " + id.getPackaging() + " " + id.getValue());
        smallFeeder.setSelected(false);
        mediumFeeder.setSelected(false);
        largeFeeder.setSelected(false);
        if (context.getFeeder() == Feeder.SMALL)
            smallFeeder.setSelected(true);
        else if (context.getFeeder() == Feeder.MEDIUM)
            mediumFeeder.setSelected(true);
        else if (context.getFeeder() == Feeder.LARGE)
            largeFeeder.setSelected(true);
        updateRows();

        pitch.setIntegerValue(context.getComponentPitch() == null ? null : context.getComponentPitch());

        moveHeadAwayButton.setDisable(getMainApplication().getCNCController() == null);
    }

    public void updateRows()
    {
        Context context = getMainApplication().getContext();
        if (smallFeeder.isSelected())
            context.setFeeder(Feeder.SMALL);
        else if (mediumFeeder.isSelected())
            context.setFeeder(Feeder.MEDIUM);
        else if (largeFeeder.isSelected())
            context.setFeeder(Feeder.LARGE);
        Integer selectedRow = row.getSelectionModel().getSelectedItem();
        rows.clear();
        if (context.getFeeder() != null)
            for (int i = 1; i < context.getFeeder().getRowCount() + 1; i++)
                rows.add(i);
        row.getSelectionModel().select(selectedRow);
        updateControls();
    }

    private void updateControls()
    {
        continueButton.setDisable(row.getSelectionModel().getSelectedItem() == null || pitch.getRealNumberText() ==null);
    }

    public void moveHeadAway()
    {
        getMainApplication().getCNCController().moveHeadAway(SettingsFactory.getMachineSettings().getFarAwayY().getValue());
    }

    @Override
    public void next()
    {
        Context context = getMainApplication().getContext();
        if (smallFeeder.isSelected())
            context.setFeeder(Feeder.SMALL);
        else if (mediumFeeder.isSelected())
            context.setFeeder(Feeder.MEDIUM);
        else if (largeFeeder.isSelected())
            context.setFeeder(Feeder.LARGE);
        context.setComponentPitch(pitch.getIntegerValue());
        context.setFeederRow(row.getValue() - 1);
        super.next();
    }
}
