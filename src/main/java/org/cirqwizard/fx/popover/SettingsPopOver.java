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
import javafx.scene.layout.GridPane;
import org.cirqwizard.fx.SettingsDependentScreenController;
import org.cirqwizard.fx.misc.SettingsEditor;
import org.cirqwizard.settings.SettingsGroup;

public class SettingsPopOver extends PopOverController
{
    @FXML private GridPane settingsPane;

    @Override
    protected String getFxmlName()
    {
        return "SettingsPopOver.fxml";
    }

    public void setGroup(SettingsGroup group, SettingsDependentScreenController listener)
    {
        SettingsEditor.renderSettings(settingsPane, group, getMainApplication(), listener);
    }

}
