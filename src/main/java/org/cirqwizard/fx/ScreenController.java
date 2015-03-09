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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import org.cirqwizard.logging.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class ScreenController
{
    @FXML protected Parent view;

    private MainApplication mainApplication;
    private ScreenController parent;
    private List<ScreenController> children = new ArrayList<>();

    public ScreenController()
    {
        if (getFxmlName() != null)
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(getFxmlName()));
                loader.setController(this);
                loader.load();
            }
            catch (IOException e)
            {
                LoggerFactory.logException("FXML loading failed: ", e);
            }
        }
    }

    public Parent getView()
    {
        return view;
    }

    public ScreenController setMainApplication(MainApplication mainApplication)
    {
        this.mainApplication = mainApplication;
        return this;
    }

    public ScreenController getParent()
    {
        return parent;
    }

    public void setParent(ScreenController parent)
    {
        this.parent = parent;
    }

    public List<ScreenController> getChildren()
    {
        return children;
    }

    public ScreenController addChild(ScreenController child)
    {
        child.setParent(this);
        children.add(child);
        return this;
    }

    protected String getFxmlName()
    {
        return null;
    }

    protected String getName()
    {
        return null;
    }

    protected boolean isMandatory()
    {
        return true;
    }

    protected boolean isEnabled()
    {
        return true;
    }

    public MainApplication getMainApplication()
    {
        return mainApplication;
    }

    public EventHandler<? super KeyEvent> getShortcutHandler()
    {
        return null;
    }

    public void refresh()
    {
    }

    public void next()
    {
        getNext(this).select();
    }

    public ScreenController getNext(ScreenController scene)
    {
        List<ScreenController> siblings = getMainApplication().getSiblings(scene);
        for (int i = siblings.indexOf(scene) + 1; i < siblings.size(); i++)
            if (siblings.get(i).isMandatory() && siblings.get(i).isEnabled())
                return siblings.get(i);
        return getNext(scene.getParent());
    }

    public void select()
    {
        getMainApplication().setCurrentScreen(this);
    }
}
