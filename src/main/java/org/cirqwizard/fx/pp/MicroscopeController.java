package org.cirqwizard.fx.pp;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.SettingsFactory;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Created by simon on 19.05.17.
 */
// TODO: fast click through screens = exception
public class MicroscopeController
{
    private SimpleBooleanProperty isRunning = new SimpleBooleanProperty();
    private Webcam webCam;
    private ImageView imageView;

    public void setImageView(ImageView imageView)
    {
        this.imageView = imageView;
    }

    public void startThread()
    {
        if (isRunning.get())
            return;

        isRunning.set(true);
        Thread thread = new Thread(() ->
        {
            Optional<Webcam> webcamOptional = Webcam.getWebcams().stream().
                    filter(i -> i.getName().equals(SettingsFactory.getPpSettings().getUsbCamera().getValue())).findFirst();
            if (!webcamOptional.isPresent())
            {
                isRunning.set(false);
                return;
            }

            webCam = webcamOptional.get();
            if (!webCam.isOpen())
            {
                webCam.setCustomViewSizes(new Dimension[]{WebcamResolution.UXGA.getSize()});
                webCam.setViewSize(WebcamResolution.UXGA.getSize());
                webCam.open(true);
            }

            while (isRunning.get())
            {
                try
                {
                    if (imageView == null)
                    {
                        Thread.sleep(20);
                        continue;
                    }

                    WritableImage i = new WritableImage(1600, 1200);
                    ByteBuffer imageBytes = webCam.getImageBytes();
                    if (imageBytes != null)
                    {
                        i.getPixelWriter().setPixels(0, 0, 1600, 1200, PixelFormat.getByteRgbInstance(), imageBytes, 4800);
                        if (imageView != null)
                            Platform.runLater(() -> imageView.setImage(i));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    LoggerFactory.logException("Exception caught in USB camera thread", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread()
    {
        isRunning.set(false);
        new Thread(() ->
        {
            if (webCam != null)
                webCam.close();
        }).start();
    }

    public boolean isRunning()
    {
        return isRunning.get();
    }

    public SimpleBooleanProperty isRunningProperty()
    {
        return isRunning;
    }
}
