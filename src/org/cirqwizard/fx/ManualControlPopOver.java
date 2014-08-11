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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;

public class ManualControlPopOver
{
    @FXML private Parent view;

    public ManualControlPopOver()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ManualControl.fxml"));
            loader.setController(this);
            loader.load();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error loading FXML", e);
        }
    }

    public Parent getView()
    {
        return view;
    }
}
