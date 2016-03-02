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

package org.cirqwizard.fx.common;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.ScreenController;


public abstract class PCBPlacement extends ScreenController
{
    @FXML protected Button continueButton;
    @FXML protected Label text;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/common/PCBPlacement.fxml";
    }

    @Override
    protected String getName()
    {
        return "Placement";
    }

    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        context.setPcbPlacement(null);
    }

    protected abstract Context.PcbPlacement getExpectedPlacement();

    @Override
    protected boolean isMandatory()
    {
        return !getExpectedPlacement().equals(getMainApplication().getContext().getPcbPlacement());
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setPcbPlacement(getExpectedPlacement());
        super.next();
    }

}
