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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;


public class AboutController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private Label versionLabel;
    @FXML private ScrollPane licensePane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        InputStream input = getClass().getResourceAsStream("/version.number");
        try
        {
            byte[] inputArray = new byte[input.available()];
            input.read(inputArray, 0, inputArray.length);
            input.close();

            versionLabel.setText("Version " + new String(inputArray));
        }
        catch (IOException e)
        {
            versionLabel.setText("Unknown version");
            LoggerFactory.logException("Could not open version number file: ", e);
        }

        input = getClass().getResourceAsStream("/gpl-3.0.txt");
        try
        {
            byte[] inputArray = new byte[input.available()];
            input.read(inputArray, 0, inputArray.length);
            input.close();

            Label licenseText = new Label(new String(inputArray));
            licenseText.setWrapText(true);
            licensePane.setContent(licenseText);
        }
        catch (IOException e)
        {
            licensePane.setContent(new Label(""));
            LoggerFactory.logException("Could not open license text file: ", e);
        }
    }

    @Override
    public Parent getView()
    {
        return view;
    }
}
