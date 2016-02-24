package org.cirqwizard.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import org.cirqwizard.fx.controls.RealNumberTextFieldTableCell;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        scrollPane.viewportBoundsProperty().addListener((v, oldV, newV) -> zoomToFit(false));
        sizeComboBox.getItems().addAll(PCBSize.values());
        sizeComboBox.getSelectionModel().selectedItemProperty().addListener((v, oldV, newV) ->
        {
            panelPane.getPanel().setSize(newV);
            savePanel();
            zoomToFit(true);
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
            savePanel();
            panelPane.render();
        });
        boardYColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        boardYColumn.setCellFactory(p -> new RealNumberTextFieldTableCell<>());
        boardYColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setY(event.getNewValue());
            savePanel();
            panelPane.render();
        });

        panelPane.setBoardDragListener(() ->
        {
            panelPane.getPanel().save(getMainApplication().getContext().getPanelFile());
            refreshTable();
        });
    }

    @Override
    public void refresh()
    {
        panelPane.setPanel(getMainApplication().getContext().getPanel());
        sizeComboBox.getSelectionModel().select(panelPane.getPanel().getSize());
        refreshTable();
        zoomToFit(true);
    }

    private void refreshTable()
    {
        boardsTable.getItems().clear();
        boardsTable.getItems().addAll(panelPane.getPanel().getBoards());
    }

    public void zoomIn()
    {
        panelPane.zoomIn();
    }

    public void zoomOut()
    {
        panelPane.zoomOut();
    }

    public void zoomToFit(boolean force)
    {
        panelPane.zoomToFit(scrollPane.getViewportBounds().getWidth(), scrollPane.getViewportBounds().getHeight(), force);
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

            PanelBoard board = new PanelBoard(commonName, 0, 0);
            board.loadBoard();
            PCBSize panelSize = sizeComboBox.getSelectionModel().getSelectedItem();
            board.setX((panelSize.getWidth() - board.getBoard().getWidth()) / 2);
            board.setY((panelSize.getHeight() - board.getBoard().getHeight()) / 2);
            panelPane.getPanel().addBoard(board);
            savePanel();
            panelPane.render();
            refreshTable();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error adding board", e);
        }
    }

    private void savePanel()
    {
        Panel panel = getMainApplication().getContext().getPanel();
        panel.save(getMainApplication().getContext().getPanelFile());
    }

}
