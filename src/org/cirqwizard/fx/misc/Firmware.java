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

package org.cirqwizard.fx.misc;

import javafx.scene.layout.VBox;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.SerialInterface;
import org.cirqwizard.stm32.STM32BootLoaderInterface;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Firmware extends ScreenController implements Initializable
{
    @FXML private TextField selectedFile;
    @FXML private ProgressBar flashProgress;
    @FXML private Button flashButton;
    @FXML private CheckBox emergencyReflashCheckbox;
    @FXML private Label flashStatusLabel;
    @FXML private Label firmwareVersion;
    @FXML private VBox fileSelectionBox;
    @FXML private VBox progressBarBox;

    private static final String ERROR_CLASS_NAME = "error-message";

    @Override
    protected String getFxmlName()
    {
        return "Firmware.fxml";
    }

    @Override
    protected String getName()
    {
        return "Firmware";
    }

    @Override
    public void refresh()
    {
        selectedFile.setText("");
        firmwareVersion.setText("");
        flashButton.setDisable(true);
        flashStatusLabel.textProperty().unbind();
        flashProgress.progressProperty().unbind();
        flashProgress.setProgress(0);
        fileSelectionBox.setVisible(true);
        progressBarBox.setVisible(false);

        String currentFirmware = null;
        if (getMainApplication().getCNCController() != null)
            currentFirmware = getMainApplication().getCNCController().getFirmwareVersion();
        if (currentFirmware != null)
            firmwareVersion.setText("Current firmware: " + currentFirmware);
        else
            firmwareVersion.setText("Current firmware: could not get firmware version.");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        selectedFile.textProperty().addListener((v, oldV, newV) -> flashButton.setDisable(newV == null || newV.trim().isEmpty()));
    }

    public void selectFile()
    {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Firmware files", "*.bin");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(null);
        if (file != null)
            selectedFile.setText(file.getAbsolutePath());

    }

    private void setErrorMessage(String message)
    {
        if (!flashStatusLabel.getStyleClass().contains(ERROR_CLASS_NAME))
            flashStatusLabel.getStyleClass().add(ERROR_CLASS_NAME);
        flashStatusLabel.setText(message);
    }

    private void setInfoMessage(String message)
    {
        if (flashStatusLabel.getStyleClass().contains(ERROR_CLASS_NAME))
            flashStatusLabel.getStyleClass().remove(ERROR_CLASS_NAME);
        flashStatusLabel.setText(message);
    }


    public void flash()
    {
        flashProgress.setVisible(true);
        fileSelectionBox.setVisible(false);
        progressBarBox.setVisible(true);
        flashStatusLabel.setText("");
        if (selectedFile.getText().isEmpty())
        {
            setErrorMessage("Error! No file selected");
            return;
        }
        try
        {
            FileInputStream inputStream;
            inputStream = new FileInputStream(selectedFile.getText());
            byte[] bin = new byte[inputStream.available()];
            inputStream.read(bin);
            setInfoMessage("File opened successfully");
            SerialInterface serialInterface = getMainApplication().getSerialInterface();
            STM32BootLoaderInterface bootloader = new STM32BootLoaderInterface(serialInterface);
            if (!emergencyReflashCheckbox.isSelected())
                bootloader.reset();
            SerialWriter serialWriter = new SerialWriter(bootloader,bin);
            serialWriter.start();
            flashProgress.progressProperty().bind(serialWriter.progressProperty());
        }
        catch (FileNotFoundException e)
        {
            flashStatusLabel.setText("Error! File not found.");
        }
        catch (IOException e)
        {
            flashStatusLabel.setText("Error! Can't read file.");
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not flash firmware", e);
            flashStatusLabel.setText("Error during flashing. Could not flash firmware.");
            flashProgress.setVisible(false);
        }
    }

    public class SerialWriter extends Service
    {
        private byte[] bin;
        private STM32BootLoaderInterface bootloader;

        public SerialWriter(STM32BootLoaderInterface bootloader, byte[] bin)
        {
            this.bootloader = bootloader;
            this.bin = bin;
        }

        @Override
        protected Task createTask()
        {
            flashProgress.setVisible(true);
            return new SerialWriterTask();
        }

        private void setStatus(final String msg)
        {
            Platform.runLater(() -> setInfoMessage(msg));
        }

        public class SerialWriterTask extends Task
        {
            @Override
            protected Object call() throws Exception
            {
                try
                {
                    bootloader.initBootloader();
                    setStatus("Erasing flash...");
                    bootloader.eraseFlash();
                    setStatus("Erased. Writing data...");
                    flashButton.setDisable(false);
                    for (int i = 0; i < bin.length; i += 256)
                    {
                        bootloader.writeSector(bin, i);
                        updateProgress(i , bin.length);
                    }
                    bootloader.restartController();
                    bootloader.switchOffBootloader();
                    setStatus("Flash OK.");
                }
                catch (Exception e)
                {
                    Platform.runLater(() ->
                    {
                        setErrorMessage("Communication error");
                        flashProgress.setVisible(false);
                    });
                    LoggerFactory.logException("Communication error:", e);
                }
                return null;
            }
        }
    }
}
