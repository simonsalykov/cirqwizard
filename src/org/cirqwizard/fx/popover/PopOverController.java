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

package org.cirqwizard.fx.popover;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.cirqwizard.fx.MainApplication;
import org.cirqwizard.logging.LoggerFactory;
import org.controlsfx.control.PopOver;

import java.io.IOException;

public abstract class PopOverController
{
    @FXML protected Parent view;
    protected MainApplication mainApplication;
    protected PopOver popOver;

    public PopOverController()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getFxmlName()));
            loader.setController(this);
            loader.load();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error loading FXML", e);
        }
    }

    protected abstract String getFxmlName();

    public Parent getView()
    {
        return view;
    }

    public MainApplication getMainApplication()
    {
        return mainApplication;
    }

    public void setMainApplication(MainApplication mainApplication)
    {
        this.mainApplication = mainApplication;
    }

    public PopOver getPopOver()
    {
        return popOver;
    }

    public void setPopOver(PopOver popOver)
    {
        this.popOver = popOver;
    }
}
