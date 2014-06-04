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
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;


public class WelcomeController extends SceneController
{
    @FXML
    private Parent view;
    @FXML
    private GridPane recentFilesPane;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void refresh()
    {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                loadFile(new File(((Hyperlink) event.getSource()).getText() + ".cmp"));
            }
        };
        recentFilesPane.getChildren().clear();
        List<String> recentFiles = getMainApplication().getSettings().getRecentFiles();
        for (int i = 0; i < recentFiles.size(); i++)
        {
            Hyperlink hyperlink = new Hyperlink(recentFiles.get(i));
            hyperlink.setOnAction(handler);
            recentFilesPane.add(hyperlink, 0, i);
        }
    }

    public void openFile(ActionEvent event)
    {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Gerber files", "*.sol", "*.cmp");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(null);
        loadFile(file);
    }

    private void loadFile(File file)
    {
        String filename = file.getAbsolutePath();
        getMainApplication().getSettings().setRecentFile(filename.substring(0, filename.lastIndexOf('.')));
        getMainApplication().getContext().setFile(file);
        getMainApplication().setState(State.ORIENTATION);
    }

    public void showManualDataInput()
    {
        getMainApplication().setState(State.MANUAL_DATA_INPUT);
    }

    public void showSettings()
    {
        getMainApplication().setState(State.SETTINGS);
    }

    public void openManualMovementScreen()
    {
        getMainApplication().setState(State.MANUAL_MOVEMENT);
    }

    public void firmware()
    {
        getMainApplication().setState(State.FIRMWARE);
    }

    public void showAbout()
    {
        getMainApplication().setState(State.ABOUT);
    }
}
