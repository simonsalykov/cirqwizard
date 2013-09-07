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

import org.cirqwizard.fx.controls.RealNumberTextField;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.SerialInterfaceFactory;
import org.cirqwizard.settings.Settings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;


public class SettingsController extends SceneController implements Initializable
{
    @FXML private Parent view;
    @FXML private RealNumberTextField yAxisDiff;
    @FXML private RealNumberTextField referencePinX;
    @FXML private RealNumberTextField referencePinY;
    @FXML private RealNumberTextField smallPCBWidth;
    @FXML private RealNumberTextField largePCBWidth;
    @FXML private RealNumberTextField farAwayY;
    @FXML private ComboBox<String> serialPortComboBox;
    @FXML private ComboBox<String> logLevelComboBox;

    @FXML private RealNumberTextField tracesToolDiameter;
    @FXML private RealNumberTextField tracesFeedXY;
    @FXML private RealNumberTextField tracesFeedZ;
    @FXML private RealNumberTextField tracesSpeed;
    @FXML private RealNumberTextField tracesClearance;
    @FXML private RealNumberTextField tracesSafetyHeight;
    @FXML private RealNumberTextField tracesZOffset;
    @FXML private RealNumberTextField tracesWorkingHeight;

    @FXML private RealNumberTextField drillingFeed;
    @FXML private RealNumberTextField drillingSpeed;
    @FXML private RealNumberTextField drillingClearance;
    @FXML private RealNumberTextField drillingSafetyHeight;
    @FXML private RealNumberTextField drillingZOffset;
    @FXML private RealNumberTextField drillingWorkingHeight;

    @FXML private RealNumberTextField contourFeedXY;
    @FXML private RealNumberTextField contourFeedZ;
    @FXML private RealNumberTextField contourSpeed;
    @FXML private RealNumberTextField contourClearance;
    @FXML private RealNumberTextField contourSafetyHeight;
    @FXML private RealNumberTextField contourZOffset;
    @FXML private RealNumberTextField contourWorkingHeight;

    @FXML private RealNumberTextField dispensingNeedleDiameter;
    @FXML private RealNumberTextField dispensingPrefeedPause;
    @FXML private RealNumberTextField dispensingFeed;
    @FXML private RealNumberTextField dispensingClearance;
    @FXML private RealNumberTextField dispensingZOffset;
    @FXML private RealNumberTextField dispensingWorkingHeight;
    @FXML private RealNumberTextField dispensingBleedingDuration;
    @FXML private RealNumberTextField dispensingPostfeedPause;

    @FXML private RealNumberTextField ppPickupHeight;
    @FXML private RealNumberTextField ppMoveHeight;
    @FXML private RealNumberTextField ppRotationFeed;

    private ChangeListener<String> serialPortNameListener = new ChangeListener<String>()
    {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
        {
            getMainApplication().getSettings().setSerialPort(s2);
            if (s2 != null && !s2.trim().isEmpty())
                getMainApplication().connectSerialPort(s2);
        }
    };


    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        yAxisDiff.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setMachineYDiff(s2);
            }
        });
        referencePinX.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setMachineReferencePinX(s2);
            }
        });
        referencePinY.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setMachineReferencePinY(s2);
            }
        });
        smallPCBWidth.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setMachineSmallPCBWidth(s2);
            }
        });
        largePCBWidth.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setMachineLargePCBWidth(s2);
            }
        });
        farAwayY.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setFarAwayY(s2);
            }
        });
        logLevelComboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                LoggerFactory.getApplicationLogger().setLevel(Level.parse(s2));
                getMainApplication().getSettings().setLogLevel(s2);
            }
        });

        tracesToolDiameter.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTraceToolDiameter(s2);
            }
        });
        tracesFeedXY.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesFeedXY(s2);
            }
        });
        tracesFeedZ.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesFeedZ(s2);
            }
        });
        tracesClearance.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesClearance(s2);
            }
        });
        tracesSafetyHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesSafetyHeight(s2);
            }
        });
        tracesZOffset.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesZOFfset(s2);
            }
        });
        tracesWorkingHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultTracesWorkingHeight(s2);
            }
        });

        drillingFeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingFeed(s2);
            }
        });
        drillingSpeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingSpeed(s2);
            }
        });
        drillingClearance.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingClearance(s2);
            }
        });
        drillingSafetyHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingSafetyHeight(s2);
            }
        });
        drillingZOffset.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingZOffset(s2);
            }
        });
        drillingWorkingHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDrillingWorkingHeight(s2);
            }
        });

        contourFeedXY.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourFeedXY(s2);
            }
        });
        contourFeedZ.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourFeedZ(s2);
            }
        });
        contourSpeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourSpeed(s2);
            }
        });
        contourClearance.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourClearance(s2);
            }
        });
        contourSafetyHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourSafetyHeight(s2);
            }
        });
        contourZOffset.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourZOffset(s2);
            }
        });
        contourWorkingHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultContourWorkingHeight(s2);
            }
        });

        dispensingNeedleDiameter.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingNeedleDiameter(s2);
            }
        });
        dispensingPrefeedPause.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingPrefeedPause(s2);
            }
        });
        dispensingFeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingFeed(s2);
            }
        });
        dispensingClearance.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingClearance(s2);
            }
        });
        dispensingZOffset.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingZOffset(s2);
            }
        });
        dispensingWorkingHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDefaultDispensingWorkingHeight(s2);
            }
        });
        dispensingBleedingDuration.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDispensingBleedingDuration(s2);
            }
        });
        dispensingPostfeedPause.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setDispensingPostfeedPause(s2);
            }
        });

        ppPickupHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setPPPickupHeight(s2);
            }
        });
        ppMoveHeight.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setPPMoveHeight(s2);
            }
        });
        ppRotationFeed.realNumberTextProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2)
            {
                getMainApplication().getSettings().setPPRotationFeed(s2);
            }
        });
    }

    @Override
    public void refresh()
    {
        serialPortComboBox.valueProperty().removeListener(serialPortNameListener);
        ArrayList<String> serialInterfaceList = SerialInterfaceFactory.getSerialInterfaces(getMainApplication().getSerialInterface());
        serialPortComboBox.getItems().clear();
        serialPortComboBox.getItems().add("");
        serialPortComboBox.getItems().addAll(serialInterfaceList);
        Settings settings = getMainApplication().getSettings();
        if (serialInterfaceList.contains(settings.getSerialPort()))
            serialPortComboBox.setValue(settings.getSerialPort());
        serialPortComboBox.valueProperty().addListener(serialPortNameListener);

        logLevelComboBox.setValue(settings.getLogLevel());

        yAxisDiff.setText(settings.getMachineYDiff());
        referencePinX.setText(settings.getMachineReferencePinX());
        referencePinY.setText(settings.getMachineReferencePinY());
        smallPCBWidth.setText(settings.getMachineSmallPCBWidth());
        largePCBWidth.setText(settings.getMachineLargePCBWidth());
        farAwayY.setText(settings.getFarAwayY());

        tracesToolDiameter.setText(settings.getDefaultTraceToolDiameter());
        tracesFeedXY.setText(settings.getDefaultTracesFeedXY());
        tracesFeedZ.setText(settings.getDefaultTracesFeedZ());
        tracesSpeed.setText(settings.getDefaultTracesSpeed());
        tracesClearance.setText(settings.getDefaultTracesClearance());
        tracesSafetyHeight.setText(settings.getDefaultTracesSafetyHeight());
        tracesZOffset.setText(settings.getDefaultTracesZOffset());
        tracesWorkingHeight.setText(settings.getDefaultTracesWorkingHeight());

        drillingFeed.setText(settings.getDefaultDrillingFeed());
        drillingSpeed.setText(settings.getDefaultDrillingSpeed());
        drillingClearance.setText(settings.getDefaultDrillingClearance());
        drillingSafetyHeight.setText(settings.getDefaultDrillingSafetyHeight());
        drillingZOffset.setText(settings.getDefaultDrillingZOffset());
        drillingWorkingHeight.setText(settings.getDefaultDrillingWorkingHeight());

        contourFeedXY.setText(settings.getDefaultContourFeedXY());
        contourFeedZ.setText(settings.getDefaultContourFeedZ());
        contourSpeed.setText(settings.getDefaultContourSpeed());
        contourClearance.setText(settings.getDefaultContourClearance());
        contourSafetyHeight.setText(settings.getDefaultContourSafetyHeight());
        contourZOffset.setText(settings.getDefaultContourZOffset());
        contourWorkingHeight.setText(settings.getDefaultContourWorkingHeight());

        dispensingNeedleDiameter.setText(settings.getDefaultDispensingNeedleDiameter());
        dispensingPrefeedPause.setText(settings.getDefaultDispensingPrefeedPause());
        dispensingFeed.setText(settings.getDefaultDispensingFeed());
        dispensingClearance.setText(settings.getDefaultDispensingClearance());
        dispensingZOffset.setText(settings.getDefaultDispensingZOffset());
        dispensingWorkingHeight.setText(settings.getDefaultDispensingWorkingHeight());
        dispensingBleedingDuration.setText(settings.getDispensingBleedingDuration());
        dispensingPostfeedPause.setText(settings.getDispensingPostfeedPause());

        ppPickupHeight.setText(settings.getPPPickupHeight());
        ppMoveHeight.setText(settings.getPPMoveHeight());
        ppRotationFeed.setText(settings.getPPRotationFeed());
    }

}
