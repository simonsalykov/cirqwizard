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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.cirqwizard.fx.misc.About;
import org.cirqwizard.fx.misc.Firmware;
import org.cirqwizard.fx.misc.ManualDataInput;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.logging.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class Welcome extends ScreenController
{
    private static final String PREFERENCE_NAME = "interface.recent.files";

    @FXML
    private GridPane recentFilesPane;

    @Override
    protected String getFxmlName()
    {
        return "Welcome.fxml";
    }

    @Override
    protected String getName()
    {
        return "Home";
    }

    @Override
    public void refresh()
    {
        EventHandler<ActionEvent> handler = (event) ->
        {
            File file = new File(((Hyperlink) event.getSource()).getText() + ".cxml");
            if (file.exists())
                loadPanel(file);
            else
                loadFile(file);
        };
        recentFilesPane.getChildren().clear();
        List<String> recentFiles = getRecentFiles();
        for (int i = 0; i < recentFiles.size(); i++)
        {
            Hyperlink hyperlink = new Hyperlink(recentFiles.get(i));
            hyperlink.setOnAction(handler);
            recentFilesPane.add(hyperlink, 0, i);
        }
    }

    public void openFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Panel files", "*.cxml"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gerber files", "*.sol", "*.cmp"));
        File file = chooser.showOpenDialog(null);
        if (file != null)
        {
            String filename = file.getAbsolutePath();
            setRecentFile(filename.substring(0, filename.lastIndexOf('.')));
            if (file.getAbsolutePath().toLowerCase().endsWith("cxml"))
                loadPanel(file);
            else
                loadFile(file);
        }
    }

    private void loadPanel(File file)
    {
        Panel panel = Panel.loadFromFile(file);
        getMainApplication().getContext().setPanel(panel);
        getMainApplication().getContext().setPanelFile(file);
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(PanelController.class));
    }

    private void loadFile(File file)
    {
        getMainApplication().getContext().setFile(file);
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(Orientation.class));
    }

    public void createPanel()
    {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Panel files", "*.cxml");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(null);
        if (file != null)
        {
            Panel panel = new Panel();
            panel.save(file);
            getMainApplication().getContext().setPanel(panel);
            getMainApplication().getContext().setPanelFile(file);
            getMainApplication().setCurrentScreen(getMainApplication().getScreen(PanelController.class));
        }
    }

    private List<String> getRecentFiles()
    {
        Preferences preferences = Preferences.userRoot().node("org.cirqwizard");
        ArrayList<String> files = new ArrayList<>();
        for (int i = 1; i <= 5; i++)
        {
            String str = preferences.get(PREFERENCE_NAME + "." + i, null);
            if (str == null)
                break;
            files.add(str);
        }
        return files;
    }

    private void setRecentFile(String file)
    {
        Preferences preferences = Preferences.userRoot().node("org.cirqwizard");
        List<String> files = getRecentFiles();
        if (files.indexOf(file) >= 0)
            files.remove(file);
        files.add(0, file);
        for (int i = 0; i < Math.min(files.size(), 5); i++)
            preferences.put(PREFERENCE_NAME + "." + (i + 1), files.get(i));
        try
        {
            preferences.flush();
        }
        catch (BackingStoreException e)
        {
            LoggerFactory.logException("Could not save preferences", e);
        }
    }

    public void showSettings()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(SettingsEditor.class));
    }

    public void openMDI()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(ManualDataInput.class));
    }

    public void firmware()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(Firmware.class));
    }

    public void showAbout()
    {
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(About.class));
    }
}
