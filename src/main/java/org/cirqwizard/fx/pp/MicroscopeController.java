package org.cirqwizard.fx.pp;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.SettingsFactory;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by simon on 19.05.17.
 */
public class MicroscopeController
{
    private SimpleBooleanProperty isRunning = new SimpleBooleanProperty();
    private ImageView imageView;
    private OpenCVFrameGrabber grabber;

    public void setImageView(ImageView imageView)
    {
        this.imageView = imageView;
    }

    public void startThread()
    {
        if (isRunning.get())
            return;

        Thread thread = new Thread(() ->
        {
            int selectedCam = -1;
            List<String> webcams = VideoCapture.getVideoDevices().stream().map(Device::getNameStr).collect(Collectors.toList());
            for (int i = 0; i < webcams.size(); i++)
                if (webcams.get(i).equals(SettingsFactory.getPpSettings().getUsbCamera().getValue()))
                    selectedCam = i;

            if (selectedCam < 0)
                return;

            isRunning.set(true);

            grabber = new OpenCVFrameGrabber(selectedCam);
            grabber.setImageWidth(1280);
            grabber.setImageHeight(720);

            try
            {
                grabber.start();
            }
            catch (FrameGrabber.Exception e)
            {
                isRunning.set(false);
                LoggerFactory.logException("Exception caught starting USB camera thread", e);
                return;
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

                    org.bytedeco.javacv.Frame f = grabber.grab();
                    BufferedImage bi = new BufferedImage(f.imageWidth, f.imageHeight, BufferedImage.TYPE_3BYTE_BGR);
                    Java2DFrameConverter.copy(f, bi);
                    WritableImage i = new WritableImage(bi.getWidth(), bi.getHeight());
                    SwingFXUtils.toFXImage(bi, i);
                    if (imageView != null)
                        Platform.runLater(() -> imageView.setImage(i));
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

    public boolean isRunning()
    {
        return isRunning.get();
    }

    public SimpleBooleanProperty isRunningProperty()
    {
        return isRunning;
    }
}
