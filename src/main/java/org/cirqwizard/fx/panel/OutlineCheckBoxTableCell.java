package org.cirqwizard.fx.panel;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.cirqwizard.fx.PanelPane;
import org.cirqwizard.generation.outline.OutlineGenerator;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.LayerElement;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by simon on 03.03.16.
 */
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
