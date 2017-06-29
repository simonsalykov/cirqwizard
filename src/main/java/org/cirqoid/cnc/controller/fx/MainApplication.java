package org.cirqoid.cnc.controller.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cirqoid.cnc.controller.serial.SerialException;
import org.cirqoid.cnc.controller.serial.SerialInterface;
import org.cirqoid.cnc.controller.serial.SerialInterfaceFactory;

/**
 * Created by simon on 13.06.17.
 */
public class MainApplication extends Application
{
    private SerialInterface serialInterface;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        loader.load();
        MainViewController controller = loader.getController();
        try
        {
            serialInterface = SerialInterfaceFactory.autodetect();
            controller.setSerialInterface(serialInterface);
        }
        catch (SerialException e)
        {
            e.printStackTrace();
        }
        Scene scene = new Scene(controller.getView(), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        serialInterface.close();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
