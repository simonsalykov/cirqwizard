package org.cirqwizard.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
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

}
