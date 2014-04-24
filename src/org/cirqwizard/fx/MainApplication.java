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

import javafx.scene.image.Image;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.*;
import org.cirqwizard.settings.Settings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.prefs.Preferences;


public class MainApplication extends Application
{
    private Stage primaryStage;
    private Scene scene;
    private Stage dialogStage;
    private Scene dialogScene;

    private HashMap<SceneEnum, SceneController> controllers = new HashMap<SceneEnum, SceneController>();
    private HashMap<Dialog, SceneController> dialogControllers = new HashMap<Dialog, SceneController>();

    private State state;
    private Context context;
    private Settings settings;
    private SerialInterface serialInterface;
    private CNCController cncController;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        settings = new Settings(Preferences.userRoot().node("org.cirqwizard"));
        LoggerFactory.getApplicationLogger().setLevel(Level.parse(settings.getLogLevel()));
        state = State.WELCOME;
        context = new Context(settings);
        connectSerialPort(settings.getSerialPort());
        for (SceneEnum s : SceneEnum.values())
            controllers.put(s, loadSceneController(s.getName()));
        for (Dialog d : Dialog.values())
            dialogControllers.put(d, loadSceneController(d.getName()));
        this.primaryStage = primaryStage;
        scene = new Scene(controllers.get(SceneEnum.Welcome).getView(), 800, 600);
        scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard.css");
        if(System.getProperty("os.name").startsWith("Linux"))
            scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard-linux.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("cirQWizard");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/application.png")));
        showScene(SceneEnum.Welcome);
        primaryStage.show();

        dialogStage = new Stage(StageStyle.UNDECORATED);
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
    }

    public void connectSerialPort(String port)
    {
        try
        {
            if (serialInterface != null)
                serialInterface.close();
            if (port != null && port.length() > 0)
                serialInterface = new SerialInterfaceImpl(port, 38400);
            else
                serialInterface = SerialInterfaceFactory.autodetect();
        }
        catch (SerialException e)
        {
            LoggerFactory.logException("Can't connect to selected serial port - " + port, e);
            try
            {
                serialInterface = SerialInterfaceFactory.autodetect();
            }
            catch (SerialException e1)
            {
                LoggerFactory.logException("Can't connect to any serial port", e);
                serialInterface = null;
            }
        }

        if (serialInterface == null)
            cncController = null;
        else
            cncController = new CNCController(serialInterface, this);
    }

    @Override
    public void stop() throws Exception
    {
        if (serialInterface != null)
            serialInterface.close();
        super.stop();
    }

    private SceneController loadSceneController(String name) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
        Parent root = (Parent) loader.load();

        SceneController controller = (SceneController) loader.getController();
        if (controller != null)
            controller.setMainApplication(this);
        return controller;
    }

    public Context getContext()
    {
        return context;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public SceneController getSceneController(SceneEnum scene)
    {
        return controllers.get(scene);
    }

    public void showScene(SceneEnum scene)
    {
        SceneController controller = controllers.get(scene);
        controller.refresh();
        this.scene.setRoot(controller.getView());
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
        state.onActivation(context);
        showScene(state.getScene());
    }

    public void prevState()
    {
        setState(state.getPrevState(context));
    }

    public void nextState()
    {
        setState(state.getNextState(context));
    }

    public SerialInterface getSerialInterface()
    {
        return serialInterface;
    }

    public CNCController getCNCController()
    {
        return cncController;
    }


    public void showInfoDialog(String header, String info)
    {
        InfoDialogController controller = (InfoDialogController) dialogControllers.get(Dialog.INFO);
        controller.setHeaderText(header);
        controller.setInfoText(info);

        if (dialogScene == null)
        {
            dialogScene = new Scene(controller.getView());
            dialogScene.getStylesheets().add("org/cirqwizard/fx/cirqwizard.css");
            if(System.getProperty("os.name").startsWith("Linux"))
                dialogScene.getStylesheets().add("org/cirqwizard/fx/cirqwizard-linux.css");
        }
        else
            dialogScene.setRoot(controller.getView());

        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    public void hideInfoDialog()
    {
        dialogStage.hide();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
