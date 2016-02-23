package org.cirqwizard.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import org.cirqwizard.gerber.GerberParser;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.layers.BoardLayer;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PanelController extends ScreenController implements Initializable
{
    @FXML private ComboBox<PCBSize> sizeComboBox;
    @FXML private PanelPane panelPane;

    @Override
    protected String getFxmlName()
    {
        return "Panel.fxml";
    }

    @Override
    protected String getName()
    {
        return "Panel";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        sizeComboBox.getItems().addAll(PCBSize.values());
        sizeComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) -> panelPane.setSize(newV));
    }

    public void addBoard()
    {
        try
        {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Gerber files", "*.sol", "*.cmp");
            chooser.getExtensionFilters().add(filter);
            File file = chooser.showOpenDialog(null);
            String filename = file.getAbsolutePath();
            String commonName = filename.substring(0, filename.lastIndexOf('.'));
            List<GerberPrimitive> primitives =  new GerberParser(new FileReader(commonName + ".cmp")).parse();
            BoardLayer topLayer = new BoardLayer();
            topLayer.setElements(primitives);
            Board board = new Board();
            board.setLayer(Board.LayerType.TOP, topLayer);
            Panel panel = new Panel();
            panel.addBoard(new PanelBoard(10000, 10000, board));
            panelPane.setPanel(panel);
            panelPane.render();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
