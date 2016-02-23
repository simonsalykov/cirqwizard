package org.cirqwizard.fx;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import org.cirqwizard.fx.controls.RealNumberTextFieldTableCell;
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
    @FXML private ScrollPane scrollPane;
    @FXML private PanelPane panelPane;

    @FXML TableView<PanelBoard> boardsTable;
    @FXML TableColumn<PanelBoard, String> boardFileColumn;
    @FXML TableColumn<PanelBoard, Integer> boardXColumn;
    @FXML TableColumn<PanelBoard, Integer> boardYColumn;
    @FXML TableColumn<PanelBoard, Boolean> boardOutlineColumn;

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
        sizeComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) ->
        {
            panelPane.setSize(newV);
            panelPane.zoomToFit(scrollPane.getViewportBounds().getWidth(), scrollPane.getViewportBounds().getHeight());
        });

        boardsTable.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) -> panelPane.selectBoard(newV));
        boardFileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        boardFileColumn.setEditable(false);
        boardFileColumn.setCellFactory(p -> new TextFieldTableCell<PanelBoard, String>()
        {
            @Override
            public void updateItem(String item, boolean empty)
            {
                super.updateItem(item != null ? item.substring(item.lastIndexOf(File.separatorChar) + 1, item.length()) : null, empty);
                setTooltip(item == null ? null : new Tooltip(item));
            }
        });
        boardXColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        boardXColumn.setCellFactory(p -> new RealNumberTextFieldTableCell<>());
        boardXColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setX(event.getNewValue());
            panelPane.render();
        });
        boardYColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        boardYColumn.setCellFactory(p -> new RealNumberTextFieldTableCell<>());
        boardYColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setY(event.getNewValue());
            panelPane.render();
        });
    }

    public void zoomIn()
    {
        panelPane.zoomIn();
    }

    public void zoomOut()
    {
        panelPane.zoomOut();
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
            board.moveToOrigin();
            Panel panel = panelPane.getPanel();
            if (panel == null)
                panel = new Panel();
            PCBSize panelSize = sizeComboBox.getSelectionModel().getSelectedItem();
            panel.addBoard(new PanelBoard(commonName, (panelSize.getWidth() - board.getWidth()) / 2,
                    (panelSize.getHeight() - board.getHeight()) / 2, board));
            panelPane.setPanel(panel);
            panelPane.render();

            List<PanelBoard> b = panel.getBoards();
            ObservableList<PanelBoard> ob = FXCollections.observableArrayList(b);
            boardsTable.itemsProperty().bind(new SimpleListProperty<>(ob));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
