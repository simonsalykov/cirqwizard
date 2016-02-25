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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.generation.toolpath.PPPoint;
import org.cirqwizard.layers.Board;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.pp.Feeder;
import org.cirqwizard.pp.PackageAttributesCache;

import java.net.URL;
import java.util.ResourceBundle;


public class FeederSelection extends ScreenController implements Initializable
{
    @FXML private Label header;
    @FXML private Label countOfComponents;
    @FXML private RadioButton smallFeeder;
    @FXML private RadioButton mediumFeeder;
    @FXML private RadioButton largeFeeder;

    @FXML private ComboBox<Integer> row;
    @FXML private RealNumberTextField pitch;

    @FXML private Button continueButton;

    private ObservableList<Integer> rows;

    @Override
    protected String getFxmlName()
    {
        return "FeederSelection.fxml";
    }

    @Override
    protected String getName()
    {
        return "Panel";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        rows = FXCollections.observableArrayList();
        row.setItems(rows);
        row.valueProperty().addListener((v, oldV, newV) -> updateControls());
        pitch.realNumberTextProperty().addListener((v, oldV, newV) -> updateControls());
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        ComponentId id = context.getCurrentComponent();

        long count = context.getPanel().getCombinedElements(Board.LayerType.PLACEMENT).stream().
                map(c -> (PPPoint)c).
                filter(c -> c.getId().equals(id)).count();
        countOfComponents.setText("You will need " + count + " such component(s)");

        header.setText("Placing component " + id.getPackaging() + " " + id.getValue());
        smallFeeder.setSelected(false);
        mediumFeeder.setSelected(false);
        largeFeeder.setSelected(false);
        PackageAttributesCache.PackageAttributes attributes = PackageAttributesCache.getInstance().getAttributes(id.getPackaging());
        if (attributes != null)
        {
            switch (attributes.getFeeder())
            {
                case SMALL: smallFeeder.setSelected(true); break;
                case MEDIUM: mediumFeeder.setSelected(true); break;
                case LARGE: largeFeeder.setSelected(true); break;
            }
        }
        updateRows();
        if (attributes != null)
        {
            row.getSelectionModel().select(attributes.getRow());
            pitch.setIntegerValue(attributes.getPitch());
        }
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
        rows.clear();
        if (context.getFeeder() != null)
            for (int i = 1; i < context.getFeeder().getRowCount() + 1; i++)
                rows.add(i);
        updateControls();
    }

    private void updateControls()
    {
        continueButton.setDisable(row.getSelectionModel().getSelectedItem() == null || pitch.getRealNumberText() ==null);
    }

    @Override
    public void next()
    {
        Context context = getMainApplication().getContext();
        Feeder selectedFeeder = null;
        if (smallFeeder.isSelected())
            selectedFeeder = Feeder.SMALL;
        else if (mediumFeeder.isSelected())
            selectedFeeder = Feeder.MEDIUM;
        else if (largeFeeder.isSelected())
            selectedFeeder = Feeder.LARGE;
        context.setFeeder(selectedFeeder);
        context.setComponentPitch(pitch.getIntegerValue());
        context.savePitchToCache(context.getCurrentComponent().getPackaging(), pitch.getIntegerValue());
        context.setFeederRow(row.getValue() - 1);
        PackageAttributesCache.getInstance().saveAttributes(context.getCurrentComponent().getPackaging(),
                selectedFeeder, row.getValue() - 1, pitch.getIntegerValue());
        super.next();
    }
}
