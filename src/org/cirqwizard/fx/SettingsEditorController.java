package org.cirqwizard.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.cirqwizard.settings.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by simon on 04/08/14.
 */
public class SettingsEditorController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private ListView<SettingsGroup> groups;
    @FXML private VBox settingsPane;

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
    }

    private void refreshSettingsPane()
    {
        try
        {
            settingsPane.getChildren().clear();
            SettingsGroup group = groups.getSelectionModel().getSelectedItem();
            String preferenceGroupName = null;
            VBox container = settingsPane;
            for (Field f : group.getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;
                if (f.isAnnotationPresent(PreferenceGroup.class))
                {
                    String name = f.getAnnotation(PreferenceGroup.class).name();
                    if (!name.equals(preferenceGroupName))
                    {
                        container = new VBox(10);
                        settingsPane.getChildren().add(container);
                        container.getChildren().add(new Label(name));
                        preferenceGroupName = name;
                    }
                }
                else
                    container = settingsPane;
                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), group.getClass()).getReadMethod().invoke(group);
                container.getChildren().add(new Label(p.getUserName()));
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

    public void resetToDefaults()
    {

    }
}
