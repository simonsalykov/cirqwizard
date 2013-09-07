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

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.SceneController;
import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.toolpath.PPPoint;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ResourceBundle;


public class ComponentPlacementController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private Label header;
    @FXML private ComboBox<String> componentName;

    @FXML private TitledPane pickupPane;
    @FXML private RealNumberTextField pickupX;
    @FXML private RealNumberTextField pickupY;
    @FXML private Button gotoTargetButton;
    @FXML private Button pickupNGoButton;

    @FXML private TitledPane placementPane;
    @FXML private RealNumberTextField placementX;
    @FXML private RealNumberTextField placementY;
    @FXML private RealNumberTextField placementAngle;

    @FXML private RealNumberTextField manualZ;

    @FXML private RealNumberTextField targetX;
    @FXML private RealNumberTextField targetY;
    @FXML private RealNumberTextField targetAngle;

    private ObservableList<String> componentNames = FXCollections.observableArrayList();
    private HashMap<Integer, String[]> placementOffsets = new HashMap<Integer, String[]>();

    private static final RealNumber feederOffsetX = new RealNumber("10");
    private static final RealNumber feederOffsetY = new RealNumber("-15");

    private static final DecimalFormat coordinatesFormat = new DecimalFormat("0.00");

    private boolean atPickupLocation = false;

    private RealNumber placementZ;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        componentName.setItems(componentNames);
        componentName.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                updateComponent();
            }
        });

        KeyboardHandler keyboardHandler = new KeyboardHandler();
        pickupX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        pickupY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementX.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementY.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        placementAngle.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
        manualZ.addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    private class KeyboardHandler implements EventHandler<KeyEvent>
    {
        @Override
        public void handle(KeyEvent event)
        {
            if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
                return;
            RealNumberTextField textField = (RealNumberTextField) event.getSource();
            try
            {
                RealNumber delta = textField == placementAngle ? new RealNumber(1) : new RealNumber("0.1");
                if (event.getCode() == KeyCode.DOWN)
                    delta = delta.negate();
                RealNumber currentValue = textField.getRealNumberText() == null ? new RealNumber(0) : new RealNumber(textField.getRealNumberText());
                textField.setText(coordinatesFormat.format(currentValue.add(delta).getValue()));
                textField.fireEvent(new ActionEvent());
            }
            catch (NumberFormatException e)
            {
                // Not a big deal
            }
        }
    }


    @Override
    public void refresh()
    {
        Context context = getMainApplication().getContext();
        ComponentId id =  context.getComponentIds().get(context.getCurrentComponent());
        header.setText("Placing component " + id.getPackaging() + " " + id.getValue());

        componentNames.clear();
        ComponentId currentId = context.getComponentIds().get(context.getCurrentComponent());
        for (PPPoint p : context.getComponentsLayer().getPoints())
            if (p.getId().equals(currentId))
                componentNames.add(p.getName());
        componentName.getSelectionModel().select(0);
        pickupNGoButton.setDisable(true);
        placementPane.setDisable(true);
        manualZ.setDisable(true);

        RealNumber x = new RealNumber(getMainApplication().getSettings().getMachineReferencePinX());
        pickupX.setText(coordinatesFormat.format(x.add(feederOffsetX).add(context.getComponentPitch().divide(2)).getValue()));
        RealNumber y = new RealNumber(getMainApplication().getSettings().getMachineReferencePinY());
        pickupY.setText(coordinatesFormat.format(context.getFeeder().getYForRow(y.add(feederOffsetY), context.getFeederRow()).getValue()));

        gotoTargetButton.setDisable(true);
    }

    private void updateComponent()
    {
        Context context = getMainApplication().getContext();
        for (PPPoint p : context.getComponentsLayer().getPoints())
        {
            if (p.getName().equals(componentName.getValue()))
            {
                targetX.setText(coordinatesFormat.format(p.getPoint().getX().getValue()));
                targetY.setText(coordinatesFormat.format(p.getPoint().getY().getValue()));
                targetAngle.setText(coordinatesFormat.format(p.getAngle().getValue()));

                String[] offsets = placementOffsets.get(p.getAngle().getValue().intValue());
                if (offsets != null)
                {
                    placementX.setText(offsets[0]);
                    placementY.setText(offsets[1]);
                    placementAngle.setText(offsets[2]);
                }
                else
                {
                    placementX.setText("");
                    placementY.setText("");
                    placementAngle.setText("");
                }

                break;
            }
        }
        pickupPane.setDisable(false);
        atPickupLocation = false;
    }

    private void rotatePP(String angle)
    {
        getMainApplication().getCNCController().rotatePP(angle, getMainApplication().getSettings().getPPRotationFeed());
    }

    public void gotoPickup()
    {
        getMainApplication().getCNCController().moveTo(pickupX.getRealNumberText(), pickupY.getRealNumberText(),
                getMainApplication().getSettings().getPPMoveHeight());
        rotatePP("0");
        manualZ.setDisable(false);
        manualZ.setText(getMainApplication().getSettings().getPPMoveHeight());
        atPickupLocation = true;
    }

    public void pickup()
    {
        Settings settings = getMainApplication().getSettings();
        if (!atPickupLocation)
        {
            getMainApplication().getCNCController().moveTo(pickupX.getRealNumberText(), pickupY.getRealNumberText(),
                    getMainApplication().getSettings().getPPMoveHeight());
            rotatePP("0");
        }
        getMainApplication().getCNCController().pickup(settings.getPPPickupHeight(), settings.getPPMoveHeight());
        manualZ.setDisable(false);
        manualZ.setText(settings.getPPMoveHeight());
        gotoTargetButton.setDisable(false);
    }

    private String getTargetX()
    {
        RealNumber x = new RealNumber(targetX.getRealNumberText()).add(new RealNumber(getMainApplication().getContext().getG54X()));
        if (placementX.getRealNumberText() != null)
            x = x.add(new RealNumber(placementX.getRealNumberText()));
        return coordinatesFormat.format(x.getValue());
    }

    private String getTargetY()
    {
        RealNumber y = new RealNumber(targetY.getRealNumberText()).add(new RealNumber(getMainApplication().getContext().getG54Y()));
        if (placementY.getRealNumberText() != null)
            y = y.add(new RealNumber(placementY.getRealNumberText()));
        return coordinatesFormat.format(y.getValue());
    }

    private String getTargetAngle()
    {
        RealNumber angle = new RealNumber(targetAngle.getRealNumberText());
        if (placementAngle.getRealNumberText() != null)
            angle = angle.add(new RealNumber(placementAngle.getRealNumberText()));
        angle = angle.subtract(new RealNumber(90));
        return coordinatesFormat.format(angle.getValue());
    }

    public void gotoTarget()
    {
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(),
                getMainApplication().getSettings().getPPMoveHeight());
        rotatePP(getTargetAngle());
        manualZ.setDisable(false);
        manualZ.setText(getMainApplication().getSettings().getPPMoveHeight());
        pickupPane.setDisable(true);
        placementPane.setDisable(false);
    }

    public void pickupAndGo()
    {
        gotoPickup();
        pickup();
        gotoTarget();
        if (placementZ != null)
        {
            manualZ.setText(coordinatesFormat.format(placementZ.getValue()));
            manualZ.fireEvent(new ActionEvent());
        }
    }

    public void place()
    {
        Settings settings = getMainApplication().getSettings();
        RealNumber z = new RealNumber(settings.getPPPickupHeight()).subtract(3);
        getMainApplication().getCNCController().place(z.toString(), settings.getPPMoveHeight());
        manualZ.setDisable(false);
        manualZ.setText(settings.getPPMoveHeight());
        placementPane.setDisable(true);
        gotoTargetButton.setDisable(true);
        int selectedIndex = componentName.getSelectionModel().getSelectedIndex();
        if (selectedIndex == componentName.getItems().size() - 1)
            pickupPane.setDisable(true);
        else
        {
            componentName.getSelectionModel().select(selectedIndex + 1);
            pickupX.setText(coordinatesFormat.format(new RealNumber(pickupX.getRealNumberText()).add(getMainApplication().getContext().getComponentPitch()).getValue()));
            pickupNGoButton.setDisable(false);
        }
    }

    public void moveHeadAway()
    {
        getMainApplication().getCNCController().moveHeadAway(getMainApplication().getSettings().getFarAwayY());
    }

    public void vacuumOff()
    {
        getMainApplication().getCNCController().vacuumOff();
        placementPane.setDisable(true);
        pickupPane.setDisable(false);
    }

    public void pickupLocationUpdated()
    {
        try
        {
            new RealNumber(pickupX.getRealNumberText());
            new RealNumber(pickupY.getRealNumberText());
        }
        catch (NumberFormatException e)
        {
            return;
        }
        getMainApplication().getCNCController().moveTo(pickupX.getRealNumberText(), pickupY.getRealNumberText(), null);
        atPickupLocation = true;
    }

    public void placementLocationUpdated()
    {
        getMainApplication().getCNCController().moveTo(getTargetX(), getTargetY(), null);
        rotatePP(getTargetAngle());
        placementOffsets.put(new RealNumber(targetAngle.getRealNumberText()).getValue().intValue(),
                new String[]{placementX.getRealNumberText(), placementY.getRealNumberText(), placementAngle.getRealNumberText()});
    }

    public void manualZUpdated()
    {
        try
        {
            RealNumber z = new RealNumber(manualZ.getRealNumberText());
            if (!placementPane.isDisabled())
                placementZ = z;
            getMainApplication().getCNCController().moveZ(manualZ.getRealNumberText());
        }
        catch (NumberFormatException e) {}
    }

}
