package org.cirqwizard.fx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.settings.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by simon on 04/08/14.
 */
public class SettingsEditorController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private ListView<SettingsGroup> groups;
    @FXML private GridPane settingsPane;

    @Override
    public Parent getView()
    {
        return view;
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
        try
        {
            settingsPane.getChildren().clear();
            SettingsGroup group = groups.getSelectionModel().getSelectedItem();
            if (group == null)
                return;

            String preferenceGroupName = null;
            GridPane container = settingsPane;
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
                        settingsPane.add(container, 0, rootRow.get(), 3, 1);
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
                    container = settingsPane;
                    row = rootRow;
                }

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];

                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), group.getClass()).getReadMethod().invoke(group);
                container.add(new Label(p.getUserName()), 0, row.get());
                container.add(getEditor(argumentClass, p, group), 1, row.get());
                container.add(new Label(p.getUnits()), 2, row.get());
                row.setValue(row.get() + 1);
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IntrospectionException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private Control getEditor(Class clazz, UserPreference p, SettingsGroup group)
    {
        Control editor = null;
        if (Integer.class.equals(clazz))
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
            }
            else
            {
                editor = new RealNumberTextField();
                ((RealNumberTextField)editor).setIntegerValue(p.getValue() == null ? null : (Integer) p.getValue());
                ((RealNumberTextField)editor).realNumberIntegerProperty().addListener((v, oldV, newV) ->
                {
                    p.setValue(newV);
                    group.save();
                });
                ((TextField)editor).setAlignment(Pos.CENTER_RIGHT);
            }
        }
        else if (String.class.equals(clazz))
        {
            editor = new TextField(p.getValue() == null ? null : (String) p.getValue());
            ((TextField)editor).textProperty().addListener((v, oldV, newV) ->
            {
                p.setValue(newV);
                group.save();
            });
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
        editor.setPrefWidth(75);
        return editor;
    }

    public void resetToDefaults()
    {

    }
}
