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

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.cirqwizard.fx.controls.RealNumberTextFieldTableCell;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ToolLibrary;
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

    @FXML private Button deleteButton;

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
        nameColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setName(event.getNewValue());
            saveLibrary();
        });
        diameterColumn.setCellValueFactory(new PropertyValueFactory<>("diameter"));
        diameterColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        diameterColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setDiameter(event.getNewValue());
            saveLibrary();
        });
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        speedColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        speedColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setSpeed(event.getNewValue());
            saveLibrary();
        });
        feedXYColumn.setCellValueFactory(new PropertyValueFactory<>("feedXY"));
        feedXYColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        feedXYColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setFeedXY(event.getNewValue());
            saveLibrary();
        });
        feedZColumn.setCellValueFactory(new PropertyValueFactory<>("feedZ"));
        feedZColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        feedZColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setFeedZ(event.getNewValue());
            saveLibrary();
        });
        arcsColumn.setCellValueFactory(new PropertyValueFactory<>("arcs"));
        arcsColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        arcsColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setArcs(event.getNewValue());
            saveLibrary();
        });
        zOffsetColumn.setCellValueFactory(new PropertyValueFactory<>("zOffset"));
        zOffsetColumn.setCellFactory(param -> new RealNumberTextFieldTableCell<>());
        zOffsetColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setZOffset(event.getNewValue());
            saveLibrary();
        });
        additionalPassesColumn.setCellValueFactory(new PropertyValueFactory<>("additionalPasses"));
        additionalPassesColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        additionalPassesColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setAdditionalPasses(event.getNewValue());
            saveLibrary();
        });
        additionalPassesOverlapColumn.setCellValueFactory(new PropertyValueFactory<>("additionalPassesOverlap"));
        additionalPassesOverlapColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        additionalPassesOverlapColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setAdditionalPassesOverlap(event.getNewValue());
            saveLibrary();
        });
        additionalPassesPadsOnly.setCellValueFactory(new PropertyValueFactory<>("additionalPassesPadsOnly"));
        additionalPassesPadsOnly.setCellFactory(param -> new CheckBoxTableCell<>());
        additionalPassesPadsOnly.setOnEditCommit(event ->
        {
            event.getRowValue().setAdditionalPassesPadsOnly(event.getNewValue());
            saveLibrary();
        });

        try
        {
            table.setItems(FXCollections.observableArrayList(ToolLibrary.load().getToolSettings()));
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Couldn't load tool library", e);
        }

        deleteButton.disableProperty().bind(Bindings.isNull(table.getSelectionModel().selectedItemProperty()));
    }

    public Parent getView()
    {
        return view;
    }

    public void addNewTool()
    {
        table.getItems().add(ToolLibrary.getDefaultTool());
        saveLibrary();
    }

    public void deleteTool()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Delete confirmation");
        alert.setHeaderText("Are you sure you want to delete the selected tool?");
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            table.getItems().remove(table.getSelectionModel().getSelectedIndex());
            saveLibrary();
        });
    }

    private void saveLibrary()
    {
        ToolLibrary library = new ToolLibrary();
        library.setToolSettings(table.getItems().toArray(new ToolSettings[0]));
        library.save();
    }

    public void resetToolLibrary()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "All user defined tools will be deleted.", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Reset confirmation");
        alert.setHeaderText("Are you sure you want to reset tool library?");
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            ToolLibrary.reset();
            try
            {
                table.setItems(FXCollections.observableArrayList(ToolLibrary.load().getToolSettings()));
            }
            catch (Exception e)
            {
                LoggerFactory.logException("Couldn't load tool library", e);
            }
        });

    }
}
