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

package org.cirqwizard.fx.pp;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.generation.toolpath.PPPoint;
import org.cirqwizard.layers.Board;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.settings.*;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class ComponentPlacement extends ScreenController implements Initializable
{
    @FXML private Label header;
    @FXML private ComboBox<PPPoint> componentName;

    @FXML private TitledPane pickupPane;
    @FXML private RealNumberTextField pickupX;
    @FXML private RealNumberTextField pickupY;
    @FXML private Button gotoPickupButton;
    @FXML private Button pickupButton;
    @FXML private Button gotoTargetButton;
    @FXML private Button pickupNGoButton;

    @FXML private TitledPane placementPane;
    @FXML private RealNumberTextField placementX;
    @FXML private RealNumberTextField placementY;
    @FXML private RealNumberTextField placementAngle;
    @FXML private Button placeButton;
    @FXML private HBox placementXPane;
    @FXML private HBox placementYPane;
    @FXML private HBox placementAnglePane;

    @FXML private RealNumberTextField manualZ;

    @FXML private RealNumberTextField targetX;
    @FXML private RealNumberTextField targetY;
    @FXML private RealNumberTextField targetAngle;

    @FXML private TitledPane manualControlPane;
    @FXML private Button moveHeadAwayButton;
    @FXML private Button vacuumOffButton;
    @FXML private Button microscopeOnButton;

    @FXML private AnchorPane regularPane;
    @FXML private ImageView microscopeImageView;
    @FXML private AnchorPane microscopeControlPane;

    @FXML private HBox pickupXPane;
    @FXML private HBox pickupYPane;
    @FXML private HBox zControlPane;
    @FXML private HBox microscopeControlsBox;

    private ObservableList<String> componentNames;
    private ObservableList<PPPoint> components;
    private HashMap<Integer, Integer[]> placementOffsets = new HashMap<>();

    private static final int feederOffsetX = 10 * ApplicationConstants.RESOLUTION;
    private static final int feederOffsetY = -15 * ApplicationConstants.RESOLUTION;

    private boolean atPickupLocation = false;

    private Integer placementZ;

    private MicroscopeController microscopeController;

    @Override
    protected String getFxmlName()
    {
        return "ComponentPlacement.fxml";
    }

    @Override
    protected String getName()
    {
        return "Placement";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        components = FXCollections.observableArrayList();
        componentName.setItems(components);
        componentName.valueProperty().addListener((v, oldV, newV) -> updateComponent());

        KeyboardHandler keyboardHandler = new KeyboardHandler();
        pickupX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        pickupY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementAngle.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        manualZ.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);

        microscopeControlPane.visibleProperty().bind(microscopeImageView.visibleProperty());
        microscopeController = new MicroscopeController();
        microscopeOnButton.disableProperty().bind(Bindings.not(microscopeController.isRunningProperty()));
    }

    private class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
                return;
            RealNumberTextField textField = (RealNumberTextField) event.getSource();
            int currentValue = textField.getIntegerValue() == null ? 0 : textField.getIntegerValue();
            int delta = textField == placementAngle ? 1000 : 100;
            if (event.getCode() == KeyCode.DOWN)
                delta = -delta;
            textField.setIntegerValue(currentValue + delta);
            textField.fireEvent(new ActionEvent());
        }
    }


    @Override
    public void refresh()
    {
        microscopeImageView.fitWidthProperty().bind(((StackPane)getView()).widthProperty());
        microscopeImageView.fitHeightProperty().bind(((StackPane)getView()).heightProperty());

        Context context = getMainApplication().getContext();
        ComponentId id =  context.getCurrentComponent();
        header.setText(id.getPackaging() + " " + id.getValue());

        components.setAll(context.getPanel().getCombinedElements(Board.LayerType.PLACEMENT).stream().
                map(c -> (PPPoint)c).
                filter(p -> p.getId().equals(id)).collect(Collectors.toList()));
        componentName.getSelectionModel().select(0);
        pickupNGoButton.setDisable(true);
        placementPane.setDisable(true);
        manualZ.setDisable(true);

        MachineSettings machineSettings = SettingsFactory.getMachineSettings();

        boolean referencePinsDefined = machineSettings.getReferencePinX().getValue() != null &&
                machineSettings.getReferencePinY().getValue() != null;
        if (referencePinsDefined)
        {
            pickupX.setIntegerValue(machineSettings.getReferencePinX().getValue() + feederOffsetX +
                    context.getComponentPitch() / 2);
            pickupY.setIntegerValue(context.getFeeder().getYForRow(machineSettings.getReferencePinY().getValue() +
                    feederOffsetY, context.getFeederRow()));
        }

        gotoTargetButton.setDisable(true);

        boolean noMachineConnected = getMainApplication().getCNCController() == null;
        gotoPickupButton.setDisable(noMachineConnected);
        pickupButton.setDisable(noMachineConnected);
        moveHeadAwayButton.setDisable(noMachineConnected);
        vacuumOffButton.setDisable(noMachineConnected);

        microscopeController.startThread();
        microscopeImageView.setVisible(false);
    }

    @Override
    public void onDeactivation()
    {
        microscopeController.setImageView(null);
        microscopeController.stopThread();
    }

    private void updateComponent()
    {
        PPPoint p = componentName.getSelectionModel().getSelectedItem();
        targetX.setIntegerValue(p.getPoint().getX());
        targetY.setIntegerValue(p.getPoint().getY());
        targetAngle.setIntegerValue(p.getAngle());

        Integer[] offsets = placementOffsets.get(p.getAngle());
        if (offsets != null)
        {
            placementX.setIntegerValue(offsets[0]);
            placementY.setIntegerValue(offsets[1]);
            placementAngle.setIntegerValue(offsets[2]);
        }
        else
        {
            placementX.setIntegerValue(null);
            placementY.setIntegerValue(null);
            placementAngle.setIntegerValue(null);
        }
        pickupPane.setDisable(false);
        atPickupLocation = false;
    }

    private void rotatePP(int angle)
    {
        getMainApplication().getCNCController().rotatePP(angle, SettingsFactory.getPpSettings().getRotationFeed().getValue());
    }

    public void gotoPickup()
    {
        Integer moveHeight = SettingsFactory.getPpSettings().getMoveHeight().getValue();
        getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(),
                moveHeight);
        rotatePP(0);
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        atPickupLocation = true;

        showMicroscopePickupPane();
    }

    public void pickup()
    {
        hideMicroscopePane();

        PPSettings settings = SettingsFactory.getPpSettings();
        Integer moveHeight = settings.getMoveHeight().getValue();
        if (!atPickupLocation)
        {
            getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(),
                    moveHeight);
            rotatePP(0);
        }
        getMainApplication().getCNCController().pickup(settings.getPickupHeight().getValue(), moveHeight);
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        gotoTargetButton.setDisable(false);
    }

    private int getTargetX()
    {
        int x = targetX.getIntegerValue() + getMainApplication().getContext().getG54X();
        if (placementX.getIntegerValue() != null)
            x += placementX.getIntegerValue();
        return x;
    }

    private int getTargetY()
    {
        int y = targetY.getIntegerValue() + getMainApplication().getContext().getG54Y();
        if (placementY.getIntegerValue() != null)
            y += placementY.getIntegerValue();
        return y;
    }

    private int getTargetAngle()
    {
        int angle = targetAngle.getIntegerValue();
        if (placementAngle.getIntegerValue() != null)
            angle += placementAngle.getIntegerValue();
        angle -= 90 * ApplicationConstants.RESOLUTION;
        angle += SettingsFactory.getImportSettings().getCentroidAngularOffset().getValue() * ApplicationConstants.RESOLUTION;
        return angle;
    }

    public void gotoTarget()
    {
        Integer moveHeight = SettingsFactory.getPpSettings().getMoveHeight().getValue();
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(),
                moveHeight);
        rotatePP(getTargetAngle());
        manualZ.setDisable(false);
        manualZ.setIntegerValue(moveHeight);
        pickupPane.setDisable(true);
        placementPane.setDisable(false);
        showMicroscopePlacementPane();
    }

    public void pickupAndGo()
    {
        gotoPickup();
        pickup();
        gotoTarget();
        if (placementZ != null)
        {
            manualZ.setIntegerValue(placementZ);
            manualZ.fireEvent(new ActionEvent());
        }
        showMicroscopePlacementPane();
    }

    public void place()
    {
        hideMicroscopePane();

        PPSettings settings = SettingsFactory.getPpSettings();
        int z = settings.getPickupHeight().getValue() - 3 * ApplicationConstants.RESOLUTION;
        getMainApplication().getCNCController().place(z, settings.getMoveHeight().getValue());
        manualZ.setDisable(false);
        manualZ.setIntegerValue(settings.getMoveHeight().getValue());
        placementPane.setDisable(true);
        gotoTargetButton.setDisable(true);
        int selectedIndex = componentName.getSelectionModel().getSelectedIndex();
        if (selectedIndex == componentName.getItems().size() - 1)
            pickupPane.setDisable(true);
        else
        {
            componentName.getSelectionModel().select(selectedIndex + 1);
            pickupX.setIntegerValue(pickupX.getIntegerValue() + getMainApplication().getContext().getComponentPitch());
            pickupNGoButton.setDisable(false);
        }
    }

    public void moveHeadAway()
    {
        PredefinedLocationSettings settings = SettingsFactory.getPredefinedLocationSettings();
        getMainApplication().getCNCController().moveTo(settings.getFarAwayX().getValue(),
                settings.getFarAwayY().getValue(), settings.getFarAwayZ().getValue());
    }

    public void vacuumOff()
    {
        getMainApplication().getCNCController().vacuumOff();
        placementPane.setDisable(true);
        pickupPane.setDisable(false);
    }

    public void pickupLocationUpdated()
    {
        if (pickupX.getIntegerValue() == null || pickupY.getIntegerValue() == null)
            return;
        getMainApplication().getCNCController().moveTo(pickupX.getIntegerValue(), pickupY.getIntegerValue(), null);
        atPickupLocation = true;
    }

    public void placementLocationUpdated()
    {
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(), null);
        rotatePP(getTargetAngle());
        placementOffsets.put(targetAngle.getIntegerValue(),
                new Integer[]{placementX.getIntegerValue(), placementY.getIntegerValue(), placementAngle.getIntegerValue()});
    }

    public void manualZUpdated()
    {
        if (!placementPane.isDisabled())
            placementZ = manualZ.getIntegerValue();
        getMainApplication().getCNCController().moveZ(manualZ.getIntegerValue());
    }

    public void showMicroscopePickupPane()
    {
        if (SettingsFactory.getPpSettings().getUsbCamera().getValue().equals(PPSettings.NO_CAMERA_STRING) ||
                !microscopeController.isRunning())
            return;
        microscopeControlsBox.getChildren().add(0, pickupButton);
        microscopeControlsBox.getChildren().add(0, zControlPane);
        microscopeControlsBox.getChildren().add(0, pickupYPane);
        microscopeControlsBox.getChildren().add(0, pickupXPane);
        showMicroscopePane();
    }

    public void showMicroscopePlacementPane()
    {
        if (SettingsFactory.getPpSettings().getUsbCamera().getValue().equals(PPSettings.NO_CAMERA_STRING) ||
                !microscopeController.isRunning())
            return;
        microscopeControlsBox.getChildren().add(0, placeButton);
        microscopeControlsBox.getChildren().add(0, zControlPane);
        microscopeControlsBox.getChildren().add(0, placementAnglePane);
        microscopeControlsBox.getChildren().add(0, placementYPane);
        microscopeControlsBox.getChildren().add(0, placementXPane);
        showMicroscopePane();
    }

    public void showMicroscopePane()
    {
        microscopeController.setImageView(microscopeImageView);
        regularPane.setVisible(false);
        microscopeImageView.setVisible(true);
    }


    public void hideMicroscopePane()
    {
        if (!microscopeImageView.isVisible())
            return;
        microscopeController.setImageView(null);
        microscopeImageView.setVisible(false);
        regularPane.setVisible(true);

        if (!pickupPane.isDisabled())
            hideMicroscopePickupPane();
        if (!placementPane.isDisabled())
            hideMicroscopePlacemenentPane();
    }

    public void hideMicroscopePickupPane()
    {
        ((VBox)pickupPane.getContent()).getChildren().add(1, pickupButton);
        ((VBox)pickupPane.getContent()).getChildren().add(0, pickupYPane);
        ((VBox)pickupPane.getContent()).getChildren().add(0, pickupXPane);
        ((VBox)manualControlPane.getContent()).getChildren().add(0, zControlPane);
    }

    public void hideMicroscopePlacemenentPane()
    {
        ((VBox)placementPane.getContent()).getChildren().add(0, placeButton);
        ((VBox)placementPane.getContent()).getChildren().add(0, placementAnglePane);
        ((VBox)placementPane.getContent()).getChildren().add(0, placementYPane);
        ((VBox)placementPane.getContent()).getChildren().add(0, placementXPane);
        ((VBox)manualControlPane.getContent()).getChildren().add(0, zControlPane);
    }

    public void turnMicroscopeOn()
    {
        if (!pickupPane.isDisabled())
            showMicroscopePickupPane();
        if (!placementPane.isDisabled())
            showMicroscopePlacementPane();
        else
            showMicroscopePane();
    }

}
