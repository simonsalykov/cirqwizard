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
package org.cirqwizard.fx.panel;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.cirqwizard.fx.PanelPane;
import org.cirqwizard.generation.outline.OutlineGenerator;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class OutlineCheckBoxTableCell extends CheckBoxTableCell<PanelBoard, Boolean>
{

    public OutlineCheckBoxTableCell(TableView<PanelBoard> boardsTable, PanelPane panelPane, File panelFile, PanelValidator validator)
    {
        super();
        setSelectedStateCallback(index ->
        {
            PanelBoard board = boardsTable.getItems().get(index);
            SimpleBooleanProperty generate = new SimpleBooleanProperty(board.isGenerateOutline());
            generate.addListener((v, oldV, newV) ->
            {
                board.setGenerateOutline(newV);
                if (newV)
                {
                    new OutlineGenerator(board).generate();
                }
                else
                {
                    try
                    {
                        board.loadBoard();
                    }
                    catch (IOException e)
                    {
                        LoggerFactory.logException("Could not load board files", e);
                    }
                }
                validator.validateBoards();
                panelPane.render();
                panelPane.getPanel().save(panelFile);
            });
            return generate;
        });
    }

}
