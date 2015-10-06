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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.SerialInterfaceFactory;
import org.cirqwizard.settings.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsEditor extends ScreenController implements Initializable
{
    @FXML private ListView<SettingsGroup> groups;
    @FXML private GridPane settingsPane;

    @Override
    protected String getFxmlName()
    {
        return "SettingsEditor.fxml";
    }

    @Override
    protected String getName()
    {
        return "Settings";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        groups.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) -> refreshSettingsPane());
    }

    @Override
    public void refresh()
    {
        groups.setItems(FXCollections.observableArrayList(SettingsFactory.getAllGroups()));
        groups.getSelectionModel().select(0);
    }

    private void refreshSettingsPane()
    {
        renderSettings(settingsPane, groups.getSelectionModel().getSelectedItem(), getMainApplication(), null);
    }

    public static void renderSettings(GridPane rootPane, SettingsGroup group, MainApplication mainApplication,
                                      SettingsDependentScreenController listener)
    {
        try
        {
            rootPane.getChildren().clear();
            if (group == null)
                return;

            String preferenceGroupName = null;
            GridPane container = rootPane;
            IntegerProperty rootRow = new SimpleIntegerProperty(0);
            IntegerProperty row = null;
            for (Field f : group.getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;
                if (f.isAnnotationPresent(PreferenceGroup.class))
                {
                    String name = f.getAnnotation(PreferenceGroup.class).name();
                    if (!name.equals(preferenceGroupName))
                    {
                        container = new GridPane();
                        container.setHgap(10);
                        container.setVgap(10);
                        rootPane.add(container, 0, rootRow.get(), 3, 1);
                        rootRow.setValue(rootRow.get() + 1);
                        container.getStyleClass().add("settings-group");
                        Label header = new Label(name);
                        header.getStyleClass().add("settings-group-header");
                        container.add(header, 0, 0, 2, 1);
                        preferenceGroupName = name;
                        row = new SimpleIntegerProperty(1);
                    }
                }
                else
                {
                    container = rootPane;
                    row = rootRow;
                }

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];

                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), group.getClass()).getReadMethod().invoke(group);
                if (listener == null || p.showInPopOver())
                {
                    if (p.getType() == PreferenceType.TOOL_TABLE)
                    {
                        Node editor = getEditor(argumentClass, p, group, mainApplication, listener);
                        container.add(editor, 0, row.get(), 3, 1);
                    }
                    else
                    {
                        container.add(new Label(p.getUserName()), 0, row.get());
                        container.add(getEditor(argumentClass, p, group, mainApplication, listener), 1, row.get());
                        container.add(new Label(p.getUnits()), 2, row.get());
                    }
                    row.setValue(row.get() + 1);
                }
            }
        }
        catch (IllegalAccessException | IntrospectionException | InvocationTargetException e)
        {
            LoggerFactory.logException("Error accessing settings group", e);
        }
    }

    private static void addInvalidationListenerToTextField(TextField textField, SettingsDependentScreenController listener)
    {
        textField.setOnAction((event) -> listener.settingsInvalidated());
    }

    private static Node getEditor(Class clazz, UserPreference p, SettingsGroup group, MainApplication mainApplication, SettingsDependentScreenController listener)
    {
        Node editor;
        if (p.getType() == PreferenceType.TOOL_TABLE)
        {
            editor = new SettingsToolTable().getView();
        }
        else if (Integer.class.equals(clazz))
        {
            if (p.getType() == PreferenceType.INTEGER || p.getType() == PreferenceType.PERCENT)
            {
                editor = new TextField(p.getValue() == null ? null : String.valueOf(p.getValue()));
                ((TextField)editor).textProperty().addListener((v, oldV, newV) ->
                {
                    p.setValue(Integer.valueOf(newV));
                    group.save();
                });
                ((TextField)editor).setAlignment(Pos.CENTER_RIGHT);
                if (p.triggersInvalidation() && listener != null)
                    addInvalidationListenerToTextField((TextField) editor, listener);
            }
            else
            {
                RealNumberTextField rnEditor = new RealNumberTextField();
                editor = rnEditor;
                rnEditor.setIntegerValue(p.getValue() == null ? null : (Integer) p.getValue());
                rnEditor.realNumberIntegerProperty().addListener((v, oldV, newV) ->
                {
                    p.setValue(newV);
                    group.save();
                });
                if (p.triggersInvalidation() && listener != null)
                    addInvalidationListenerToTextField(rnEditor, listener);
                rnEditor.setAlignment(Pos.CENTER_RIGHT);
            }
        }
        else if (p.getType() == null && String.class.equals(clazz))
        {
            editor = new TextField(p.getValue() == null ? null : (String) p.getValue());
            ((TextField)editor).textProperty().addListener((v, oldV, newV) ->
            {
                p.setValue(newV);
                group.save();
            });
            if (p.triggersInvalidation() && listener != null)
                addInvalidationListenerToTextField((TextField) editor, listener);
            ((TextField)editor).setAlignment(Pos.CENTER_RIGHT);
        }
        else if (Boolean.class.equals(clazz))
        {
            editor = new CheckBox();
            ((CheckBox)editor).setSelected(p.getValue() == null ? false : (Boolean)p.getValue());
            ((CheckBox)editor).selectedProperty().addListener((v, oldV, newV) ->
            {
                p.setValue(newV);
                group.save();
            });
        }
        else
        {
            List items = p.getItems();
            if (p.getType() == PreferenceType.SERIAL_PORT)
                items = SerialInterfaceFactory.getSerialInterfaces(mainApplication.getSerialInterface());
            editor = new ComboBox(FXCollections.observableArrayList(items));
            ((ComboBox)editor).getSelectionModel().select(p.getValue());
            ((ComboBox)editor).getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) ->
            {
                p.setValue(newV);
                group.save();
            });
            if (p.getType() == PreferenceType.SERIAL_PORT)
            {
                ((ComboBox)editor).getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) ->
                {
                    mainApplication.connectSerialPort((String) newV);
                    group.save();
                });
            }
            ((Control)editor).setPrefWidth(150);
        }
        if (editor instanceof TextField)
            ((Control)editor).setPrefWidth(75);
        return editor;
    }

    public void resetToDefaults()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Reset confirmation");
        alert.setHeaderText("Are you sure you want to reset all settings to default?");
        alert.showAndWait().filter(response -> response == ButtonType.YES).ifPresent(response ->
        {
            SettingsFactory.resetAll();
            refresh();
        });
    }
}
