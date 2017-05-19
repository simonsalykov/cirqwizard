package org.cirqwizard.fx.pp;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.cirqwizard.settings.SettingsFactory;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Created by simon on 19.05.17.
 */
public class MicroscopeController
{
    private boolean microscopeStreamRunning;
    private Webcam webCam;
    private ImageView imageView;

    public MicroscopeController(ImageView imageView)
    {
        this.imageView = imageView;
    }

    public void startThread()
    {
        if (microscopeStreamRunning)
            return;

        microscopeStreamRunning = true;
        Thread thread = new Thread(() ->
        {
            Optional<Webcam> webcamOptional = Webcam.getWebcams().stream().
                    filter(i -> i.getName().equals(SettingsFactory.getPpSettings().getUsbCamera().getValue())).findFirst();
            if (!webcamOptional.isPresent())
            {
                microscopeStreamRunning = false;
                return;
            }

            webCam = webcamOptional.get();
            if (!webCam.isOpen())
            {
                webCam.setCustomViewSizes(new Dimension[]{WebcamResolution.UXGA.getSize()});
                webCam.setViewSize(WebcamResolution.UXGA.getSize());
                webCam.open();
            }

            while (microscopeStreamRunning)
            {
                try
                {
                    if (!imageView.isVisible())
                    {
                        Thread.sleep(20);
                        continue;
                    }

                    WritableImage i = new WritableImage(1600, 1200);
                    ByteBuffer imageBytes = webCam.getImageBytes();
                    if (imageBytes != null)
                    {
                        i.getPixelWriter().setPixels(0, 0, 1600, 1200, PixelFormat.getByteRgbInstance(), imageBytes, 4800);
                        Platform.runLater(() -> imageView.setImage(i));
//                        Platform.runLater(() -> microscopeImageProperty.set(i));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread()
    {
        microscopeStreamRunning = false;
        new Thread(() -> webCam.close()).start();
    }
}
