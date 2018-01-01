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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.cirqwizard.fx.misc.About;
import org.cirqwizard.fx.misc.Firmware;
import org.cirqwizard.fx.misc.ManualDataInput;
import org.cirqwizard.fx.panel.PanelController;
import org.cirqwizard.fx.settings.SettingsEditor;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.SettingsFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class Welcome extends ScreenController
{
    private static final String PREFERENCE_NAME = "interface.recent.files";

    @FXML private GridPane recentFilesPane;
    @FXML private VBox missingSettingsBox;

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
        EventHandler<ActionEvent> handler = (event) -> openFile(((Hyperlink) event.getSource()).getText());
        recentFilesPane.getChildren().clear();
        List<String> recentFiles = getRecentFiles();
        for (int i = 0; i < recentFiles.size(); i++)
        {
            Hyperlink hyperlink = new Hyperlink(recentFiles.get(i));
            hyperlink.setOnAction(handler);
            recentFilesPane.add(hyperlink, 0, i);
        }
        missingSettingsBox.setVisible(SettingsFactory.getAllGroups().stream().anyMatch(g -> g.validate() != null));
        missingSettingsBox.setManaged(missingSettingsBox.isVisible());
    }

    private void openFile(String filename)
    {
        File file = new File(filename + ".cxml");
        if (file.exists())
        {
            loadPanel(file);
            return;
        }
        file = new File(filename + ".cmp");
        if (file.exists())
        {
            createPanel(file);
            return;
        }
        file = new File(filename + ".sol");
        if (file.exists())
        {
            createPanel(file);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load file " + filename, ButtonType.OK);
        alert.setHeaderText("File not found");
        alert.show();

        removeRecentFile(filename);
        refresh();
    }

    public void openFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All supported files", "*.cxml", "*.sol", "*.cmp"));
        File file = chooser.showOpenDialog(null);
        if (file != null)
        {
            String filename = file.getAbsolutePath();
            filename = filename.substring(0, filename.lastIndexOf('.'));
            setRecentFile(filename);
            openFile(filename);
        }
    }

    private void createPanel(File file)
    {
        try
        {
            String basename = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.'));
            Panel panel = new Panel();
            panel.setSize(PCBSize.Small);
            PanelBoard panelBoard = new PanelBoard();
            panelBoard.setFilename(basename);
            panelBoard.loadBoard();
            panelBoard.centerInPanel(panel);
            panel.addBoard(panelBoard);
            File panelFile = new File(basename + ".cxml");
            panel.save(panelFile);
            loadPanel(panelFile);
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Could not create panel", e);
        }
    }

    private void loadPanel(File file)
    {
        getMainApplication().resetContext();
        setRecentFile(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')));
        Panel panel = Panel.loadFromFile(file);
        getMainApplication().getContext().setPanel(panel);
        getMainApplication().getContext().setPanelFile(file);
        getMainApplication().setCurrentScreen(getMainApplication().getScreen(PanelController.class));
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

    private void saveRecentFiles(List<String> files)
    {
        Preferences preferences = Preferences.userRoot().node("org.cirqwizard");
        for (int i = 0; i < 5; i++)
        {
            if (files.size() > i)
                preferences.put(PREFERENCE_NAME + "." + (i + 1), files.get(i));
            else
                preferences.remove(PREFERENCE_NAME + "." + (i + 1));
        }

        try
        {
            preferences.flush();
        }
        catch (BackingStoreException e)
        {
            LoggerFactory.logException("Could not save preferences", e);
        }
    }

    private void setRecentFile(String file)
    {
        List<String> files = getRecentFiles();
        if (files.indexOf(file) >= 0)
            files.remove(file);
        files.add(0, file);

        saveRecentFiles(files);
    }

    private void removeRecentFile(String file)
    {
        List<String> files = getRecentFiles();
        files.remove(file);

        saveRecentFiles(files);
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
