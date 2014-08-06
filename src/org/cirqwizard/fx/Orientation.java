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

import org.cirqwizard.fx.services.ShapesGenerationService;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;


public class Orientation extends ScreenController
{
    @FXML private PCBPane pcbPane;
    @FXML private ProgressIndicator progressIndicator;

    private ShapesGenerationService service;

    @Override
    protected String getFxmlName()
    {
        return "orientation.fxml";
    }

    @Override
    protected String getName()
    {
        return "Orientation";
    }

    public void refresh()
    {
        service = new ShapesGenerationService(getMainApplication().getContext());
        progressIndicator.visibleProperty().bind(service.runningProperty());
        pcbPane.itemsProperty().bind(service.valueProperty());
        service.start();
    }

    public void rotateCCW()
    {
        getMainApplication().getContext().getPcbLayout().rotate(false);
        service.restart();
    }

    public void rotateCW()
    {
        getMainApplication().getContext().getPcbLayout().rotate(true);
        service.restart();
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setBoardWidth((int)pcbPane.getUnscaledWidth());
        getMainApplication().getContext().setBoardHeight((int)pcbPane.getUnscaledHeight());
        super.next();
    }
}
