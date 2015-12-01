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

package org.cirqwizard.fx.traces;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.Tool;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ToolLibrary;
import org.cirqwizard.settings.ToolSettings;

public class InsertTool extends ScreenController
{
    public static final Tool EXPECTED_TOOL = new Tool(Tool.ToolType.V_TOOL, 0);

    @FXML private ComboBox<ToolSettings> toolComboBox;
    @FXML private Button continueButton;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/traces/InsertTool.fxml";
    }

    @Override
    protected String getName()
    {
        return "Tool";
    }

    @FXML
    public void initialize()
    {
        continueButton.disableProperty().bind(Bindings.isNull(toolComboBox.getSelectionModel().selectedItemProperty()));
    }

    @Override
    public void refresh()
    {
        super.refresh();
        getMainApplication().getContext().setInsertedTool(null);

        try
        {
            toolComboBox.setItems(FXCollections.observableArrayList(ToolLibrary.load().getToolSettings()));
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not load tool library", e);
        }
    }

    @Override
    protected boolean isMandatory()
    {
        return !EXPECTED_TOOL.equals(getMainApplication().getContext().getInsertedTool());
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setInsertedTool(EXPECTED_TOOL);
        getMainApplication().getContext().setCurrentMillingTool(toolComboBox.getSelectionModel().getSelectedItem());
        getMainApplication().getContext().setCurrentMillingToolIndex(toolComboBox.getSelectionModel().getSelectedIndex());
        super.next();
    }
}
