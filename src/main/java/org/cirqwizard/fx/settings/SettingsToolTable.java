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
package org.cirqwizard.fx.settings;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.cirqwizard.fx.controls.RealNumberTextFieldTableCell;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ToolSettings;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsToolTable implements Initializable
{
    @FXML private Parent view;

    @FXML private TableView<ToolSettings> table;
    @FXML private TableColumn<ToolSettings, String> nameColumn;
    @FXML private TableColumn<ToolSettings, Integer> diameterColumn;
    @FXML private TableColumn<ToolSettings, Integer> speedColumn;
    @FXML private TableColumn<ToolSettings, Integer> feedXYColumn;
    @FXML private TableColumn<ToolSettings, Integer> feedZColumn;
    @FXML private TableColumn<ToolSettings, Integer> arcsColumn;
    @FXML private TableColumn<ToolSettings, Integer> zOffsetColumn;
    @FXML private TableColumn<ToolSettings, Integer> additionalPassesColumn;
    @FXML private TableColumn<ToolSettings, Integer> additionalPassesOverlapColumn;
    @FXML private TableColumn<ToolSettings, Boolean> additionalPassesPadsOnly;

    public SettingsToolTable()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolTable.fxml"));
            loader.setController(this);
            loader.load();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error loading FXML", e);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        table.setItems(FXCollections.observableArrayList());

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        diameterColumn.setCellValueFactory(new PropertyValueFactory<>("diameter"));
        diameterColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        speedColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        feedXYColumn.setCellValueFactory(new PropertyValueFactory<>("feedXY"));
        feedXYColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        feedZColumn.setCellValueFactory(new PropertyValueFactory<>("feedZ"));
        feedZColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        arcsColumn.setCellValueFactory(new PropertyValueFactory<>("arcs"));
        arcsColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        additionalPassesColumn.setCellValueFactory(new PropertyValueFactory<>("additionalPasses"));
        additionalPassesColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        additionalPassesOverlapColumn.setCellValueFactory(new PropertyValueFactory<>("additionalPassesOverlap"));
        additionalPassesOverlapColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        additionalPassesPadsOnly.setCellValueFactory(new PropertyValueFactory<>("additionalPassesPadsOnly"));
        additionalPassesPadsOnly.setCellFactory(param -> new CheckBoxTableCell<>());
    }

    public Parent getView()
    {
        return view;
    }

    public void addNewTool()
    {
        ToolSettings tool = new ToolSettings();
        tool.setName("Default tool");
        tool.setDiameter(300);
        tool.setSpeed(1390);
        tool.setFeedXY(300_000);
        tool.setFeedZ(300_000);
        tool.setArcs(100);
        tool.setAdditionalPasses(0);
        tool.setAdditionalPassesOverlap(50);
        tool.setAdditionalPassesPadsOnly(false);
        table.getItems().add(tool);
    }

}
