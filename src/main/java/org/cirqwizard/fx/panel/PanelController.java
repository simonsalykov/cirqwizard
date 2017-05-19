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

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBSize;
import org.cirqwizard.fx.PanelPane;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextFieldTableCell;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.MachineSettings;
import org.cirqwizard.settings.SettingsFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PanelController extends ScreenController implements Initializable
{
    private static final KeyCodeCombination KEY_CODE_ZOOM_IN = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCodeCombination KEY_CODE_ZOOM_OUT = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN);


    @FXML private ComboBox<PCBSize> sizeComboBox;
    @FXML private ScrollPane scrollPane;
    @FXML private PanelPane panelPane;

    @FXML TableView<PanelBoard> boardsTable;
    @FXML TableColumn<PanelBoard, String> boardFileColumn;
    @FXML TableColumn<PanelBoard, Integer> boardXColumn;
    @FXML TableColumn<PanelBoard, Integer> boardYColumn;
    @FXML TableColumn<PanelBoard, Boolean> boardOutlineColumn;

    @FXML private Button removeButton;

    @FXML private VBox errorBox;
    private CheckBox ignoreErrorCheckBox;
    @FXML private Button continueButton;
    private PanelValidator validator;

    private boolean resetCacheOnChange = true;

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
            if (resetCacheOnChange)
                panelPane.getPanel().resetCacheTimestamps();
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
            panelPane.getPanel().resetCacheTimestamps();
            validator.validateBoards();
            savePanel();
            panelPane.render();
        });
        boardYColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        boardYColumn.setCellFactory(p -> new RealNumberTextFieldTableCell<>());
        boardYColumn.setOnEditCommit(event ->
        {
            event.getRowValue().setY(event.getNewValue());
            panelPane.getPanel().resetCacheTimestamps();
            validator.validateBoards();
            savePanel();
            panelPane.render();
        });
        boardOutlineColumn.setCellValueFactory(new PropertyValueFactory<>("generateOutline"));
        boardOutlineColumn.setCellFactory(p -> new OutlineCheckBoxTableCell(boardsTable, panelPane,
                getMainApplication().getContext().getPanelFile(), validator));

        panelPane.setBoardDragListener(() ->
        {
            panelPane.getPanel().resetCacheTimestamps();
            panelPane.getPanel().save(getMainApplication().getContext().getPanelFile());
            validator.validateBoards();
            refreshTable();
        });

        ignoreErrorCheckBox = new CheckBox("Ignore the errors. I know what I am doing");
        continueButton.disableProperty().bind(Bindings.and(Bindings.isNotEmpty(errorBox.getChildren()),
                Bindings.not(ignoreErrorCheckBox.selectedProperty())));

        removeButton.disableProperty().bind(Bindings.isNull(boardsTable.getSelectionModel().selectedItemProperty()));
    }

    @Override
    public void refresh()
    {
        resetCacheOnChange = false;
        panelPane.setPanel(getMainApplication().getContext().getPanel());
        sizeComboBox.getSelectionModel().select(panelPane.getPanel().getSize());
        resetCacheOnChange = true;
        refreshTable();
        zoomToFit(true);
        MachineSettings machineSettings = SettingsFactory.getMachineSettings();
        if (machineSettings.getReferencePinX().getValue() != null && machineSettings.getReferencePinY().getValue() != null)
        {
            getMainApplication().getContext().setG54X(machineSettings.getReferencePinX().getValue() -
                    ApplicationConstants.getRegistrationPinsInset());
            getMainApplication().getContext().setG54Y(machineSettings.getReferencePinY().getValue() -
                    ApplicationConstants.getRegistrationPinsInset());
        }
        validator = new PanelValidator(panelPane.getPanel(), errorBox, ignoreErrorCheckBox, () ->
        {
            savePanel();
            panelPane.render();
            refreshTable();
        });
        validator.validateBoards();
    }

    private EventHandler<? super KeyEvent> shortcutHandler = event -> {

        if (event.isConsumed())
            return;
        if (KEY_CODE_ZOOM_IN.match(event))
        {
            zoomIn();
            event.consume();
        }
        else if (KEY_CODE_ZOOM_OUT.match(event))
        {
            zoomOut();
            event.consume();
        }
    };

    @Override
    public EventHandler<? super KeyEvent> getShortcutHandler()
    {
        return shortcutHandler;
    }

    private void refreshTable()
    {
        boardsTable.getItems().clear();
        boardsTable.getItems().addAll(panelPane.getPanel().getBoards());
    }

    public void rotateCw()
    {
        if (panelPane.getSelectedBoard() != null)
        {
            panelPane.getSelectedBoard().rotate(true);
            panelPane.getPanel().save(getMainApplication().getContext().getPanelFile());
            panelPane.render();
            validator.validateBoards();
        }
    }

    public void rotateCcw()
    {
        if (panelPane.getSelectedBoard() != null)
        {
            panelPane.getSelectedBoard().rotate(false);
            panelPane.getPanel().save(getMainApplication().getContext().getPanelFile());
            panelPane.render();
            validator.validateBoards();
        }
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
            board.centerInPanel(panelPane.getPanel());
            panelPane.getPanel().addBoard(board);
            panelPane.getPanel().resetCacheTimestamps();
            savePanel();
            panelPane.render();
            refreshTable();
            validator.validateBoards();
        }
        catch (IOException e)
        {
            LoggerFactory.logException("Error adding board", e);
        }
    }

    public void removeBoard()
    {
        panelPane.getPanel().getBoards().remove(boardsTable.getSelectionModel().getSelectedItem());
        savePanel();
        panelPane.render();
        refreshTable();
        validator.validateBoards();
    }

    private void savePanel()
    {
        Context context = getMainApplication().getContext();
        context.getPanel().save(context.getPanelFile());
    }

}
