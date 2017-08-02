package org.cirqoid.cnc.controller.serial;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.cirqoid.cnc.controller.commands.RequestVersionCommand;
import org.cirqoid.cnc.controller.commands.Response;
import org.cirqoid.cnc.controller.commands.SetParametersCommand;
import org.cirqoid.cnc.controller.commands.VersionResponse;
import org.cirqoid.cnc.controller.settings.HardwareSettings;
import org.cirqwizard.logging.LoggerFactory;

/**
 * Created by simon on 28.06.17.
 */
public class CirqoidInitializer
{
    private static int SUPPORTED_FIRMWARE_MAJOR_VERSION = 1;
    private static int SUPPORTED_FIRMWARE_MIDDLE_VERSION = 0;

    public static void initDevice(SerialInterface serialInterface) throws SerialException
    {
        RequestVersionCommand request = new RequestVersionCommand();
        request.setId(serialInterface.getPacketId());
        ResponseListener l = new ResponseListener()
        {
            @Override
            public void responseReceived(Response response)
            {
                try
                {
                    VersionResponse r = (VersionResponse) response;
                    int middleVersion = (r.getSoftwareVersion() >> 8) & 0xFF;
                    int majorVersion = (r.getSoftwareVersion() >> 16) & 0xFF;
                    if (middleVersion != SUPPORTED_FIRMWARE_MIDDLE_VERSION || majorVersion != SUPPORTED_FIRMWARE_MAJOR_VERSION)
                    {
                        Platform.runLater(() ->
                        {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "This version of cirQWizard is designed to" +
                                    " work with controller firmware version " + SUPPORTED_FIRMWARE_MAJOR_VERSION + "." +
                                    SUPPORTED_FIRMWARE_MIDDLE_VERSION + ".x Please flash new firmware before proceeding.", ButtonType.OK);
                            alert.setHeaderText("Please update your firmware");
                            alert.setTitle("Unsupported firmware version");
                            alert.showAndWait();
                        });
                        throw new SerialException("Unsupported controller firmware");
                    }

                    serialInterface.setHardwareVersion(r.getHardwareVersion());
                    serialInterface.setSoftwareVersion(r.getSoftwareVersion());
                    sendInitPacket(serialInterface, r.getHardwareVersion());
                }
                catch (SerialException e)
                {
                    LoggerFactory.logException("Error initializing controller", e);
                }
                serialInterface.removeListener(Response.Code.VERSION_INFO, this);
            }
        };
        serialInterface.addListener(Response.Code.VERSION_INFO, l);
        serialInterface.send(request);
    }

    private static void sendInitPacket(SerialInterface serialInterface, int hardwareVersion) throws SerialException
    {
        SetParametersCommand packet = new SetParametersCommand();
        packet.setId(serialInterface.getPacketId());
        packet.setAcceleration(100);
        packet.setMaxArcsFeed(450000);
        packet.setMotionCorrectionFrequency(2000);
        packet.setMotionAdjustmentTarget(0.01f);
        packet.setMinimumSpeed(100);
        packet.setAccelerationStartSpeed(1500);
        packet.setMotionCorrecionLagThreshold(0.002f);
        packet.setMotionCorrectionMinLagThreshold(50);
        packet.setPositioningTolerance(5);
        packet.setMotionsJointTolerance(5);

        SetParametersCommand.Axis x = new SetParametersCommand.Axis();
        x.enabled = true;
        x.stepsPerMilli = 800.0f;
        x.lowLimit = HardwareSettings.getCirqoidSettings().getAxes()[0].getLowLimit();
        x.highLimit = HardwareSettings.getCirqoidSettings().getAxes()[0].getHighLimit();
        x.seekrate = 1_500_000;
        x.inverted = true;
        x.homingDirection = true;
        x.homingRate = 300_000;
        x.seekAcceleration = 100.0f;
        packet.setAxis(0, x);

        SetParametersCommand.Axis y = new SetParametersCommand.Axis();
        y.enabled = true;
        y.stepsPerMilli = 800.0f;
        y.lowLimit = HardwareSettings.getCirqoidSettings().getAxes()[1].getLowLimit();
        y.highLimit = HardwareSettings.getCirqoidSettings().getAxes()[1].getHighLimit();
        y.seekrate = 1_000_000;
        y.inverted = true;
        y.homingDirection = true;
        y.homingRate = 300_000;
        y.seekAcceleration = 40.0f;
        packet.setAxis(1, y);

        SetParametersCommand.Axis z = new SetParametersCommand.Axis();
        z.enabled = true;
        z.stepsPerMilli = 800.0f;
        z.lowLimit = HardwareSettings.getCirqoidSettings().getAxes()[2].getLowLimit();
        z.highLimit = HardwareSettings.getCirqoidSettings().getAxes()[2].getHighLimit();
        z.seekrate = 1_000_000;
        z.inverted = true;
        z.homingDirection = false;
        z.homingRate = 300_000;
        z.seekAcceleration = 50.0f;
        packet.setAxis(2, z);

        SetParametersCommand.Axis a = new SetParametersCommand.Axis();
        a.enabled = true;
        a.stepsPerMilli = 1600.0f / 360.0f * 100.0f;
        a.lowLimit = HardwareSettings.getCirqoidSettings().getAxes()[3].getLowLimit();
        a.highLimit =  HardwareSettings.getCirqoidSettings().getAxes()[3].getHighLimit();
        a.seekrate = 500_000;
        a.inverted = false;
        a.seekAcceleration = 50.0f;
        packet.setAxis(3, a);

        SetParametersCommand.Motor m0 = new SetParametersCommand.Motor();
        m0.axis = 0;
        m0.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
        m0.invertFeedback = false;
        m0.homingSensor = SetParametersCommand.HomingSensor.AS5311;
        m0.invertSensor = false;
        packet.setMotor(0, m0);

        SetParametersCommand.Motor m1 = new SetParametersCommand.Motor();
        m1.axis = 1;
        m1.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
        m1.invertFeedback = true;
        m1.homingSensor = SetParametersCommand.HomingSensor.AS5311;
        m1.invertSensor = false;
        packet.setMotor(1, m1);

        SetParametersCommand.Motor m2 = new SetParametersCommand.Motor();
        m2.axis = 1;
        m2.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
        m2.invertFeedback = true;
        m2.homingSensor = SetParametersCommand.HomingSensor.AS5311;
        m2.invertSensor = true;
        packet.setMotor(2, m2);

        if (hardwareVersion == 0)
        {
            SetParametersCommand.Motor m3 = new SetParametersCommand.Motor();
            m3.axis = 2;
            m3.feedbackProvider = SetParametersCommand.FeedbackProvider.OPEN_LOOP;
            m3.invertFeedback = false;
            m3.homingSensor = SetParametersCommand.HomingSensor.GPIO_HACK;
            m3.invertSensor = false;
            packet.setMotor(3, m3);

            z.seekAcceleration = 50.0f;
        }
        else
        {
            SetParametersCommand.Motor m3 = new SetParametersCommand.Motor();
            m3.axis = 2;
            m3.feedbackProvider = SetParametersCommand.FeedbackProvider.AS5311;
            m3.invertFeedback = false;
            m3.homingSensor = SetParametersCommand.HomingSensor.AS5311;
            m3.invertSensor = false;
            packet.setMotor(3, m3);

            z.seekAcceleration = 100.0f;
        }
        SetParametersCommand.Motor m4 = new SetParametersCommand.Motor();
        m4.axis = 3;
        m4.feedbackProvider = SetParametersCommand.FeedbackProvider.OPEN_LOOP;
        m4.invertFeedback = false;
        m4.homingSensor = SetParametersCommand.HomingSensor.NONE;
        m4.invertSensor = false;
        m4.onDemand = true;
        packet.setMotor(4, m4);

        serialInterface.send(packet);
    }
}
