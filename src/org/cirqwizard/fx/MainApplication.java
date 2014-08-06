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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.cirqwizard.fx.common.XYOffsets;
import org.cirqwizard.fx.traces.InsertTool;
import org.cirqwizard.fx.traces.ZOffset;
import org.cirqwizard.fx.traces.bottom.BottomTraceMilling;
import org.cirqwizard.fx.traces.top.PCBPlacement;
import org.cirqwizard.fx.traces.top.TopTraceMilling;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.serial.*;
import org.cirqwizard.settings.Settings;
import org.cirqwizard.settings.SettingsFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


public class MainApplication extends Application
{
    private Stage primaryStage;
    private Scene scene;
    private Stage dialogStage;
    private Scene dialogScene;

    private Context context;
    private SerialInterface serialInterface;
    private CNCController cncController;

    private MainViewController mainView = (MainViewController) new MainViewController().setMainApplication(this);

    private ScreenController root = new Welcome().setMainApplication(this).
            addChild(new Orientation().setMainApplication(this)).
            addChild(new Homing().setMainApplication(this)).
            addChild(new DummyController("Top traces").
                    addChild(new PCBPlacement().setMainApplication(this)).
                    addChild(new InsertTool().setMainApplication(this)).
                    addChild(new ZOffset().setMainApplication(this)).
                    addChild(new XYOffsets().setMainApplication(this)).
                    addChild(new TopTraceMilling().setMainApplication(this))).
            addChild(new DummyController("Bottom traces").
                    addChild(new org.cirqwizard.fx.traces.bottom.PCBPlacement().setMainApplication(this)).
                    addChild(new InsertTool().setMainApplication(this)).
                    addChild(new ZOffset().setMainApplication(this)).
                    addChild(new XYOffsets().setMainApplication(this)).
                    addChild(new BottomTraceMilling().setMainApplication(this)));

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        new Settings(Preferences.userRoot().node("org.cirqwizard")).export();
        LoggerFactory.getApplicationLogger().setLevel(SettingsFactory.getApplicationSettings().getLogLevel().getValue());
        context = new Context();
        connectSerialPort(SettingsFactory.getApplicationSettings().getSerialPort().getValue());

        this.primaryStage = primaryStage;
        scene = new Scene(mainView.getView(), 800, 600);
        scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard.css");
        if(System.getProperty("os.name").startsWith("Linux"))
            scene.getStylesheets().add("org/cirqwizard/fx/cirqwizard-linux.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("cirQWizard");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/application.png")));
        mainView.setScreen(root);
        primaryStage.show();

        dialogStage = new Stage(StageStyle.UNDECORATED);
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
    }

    public ScreenController getScreen(Class clazz)
    {
        return getScreen(root, clazz);
    }

    private ScreenController getScreen(ScreenController root, Class clazz)
    {
        if (clazz.equals(root.getClass()))
            return root;
        if (root.getChildren() != null)
        {
            for (ScreenController ctrl : root.getChildren())
            {
                ScreenController c = getScreen(ctrl, clazz);
                if (c != null)
                    return c;
            }
        }
        return null;
    }

    public ScreenController getCurrentScreen()
    {
        return mainView.getCurrentScreen();
    }

    public void setCurrentScreen(ScreenController screen)
    {
        mainView.setScreen(screen);
    }

    public MainViewController getMainView()
    {
        return mainView;
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

    public Context getContext()
    {
        return context;
    }

    public SerialInterface getSerialInterface()
    {
        return serialInterface;
    }

    public CNCController getCNCController()
    {
        return cncController;
    }


    public List<ScreenController> getPath(ScreenController scene)
    {
        ArrayList<ScreenController> path = new ArrayList<>();
        for (; scene != null; scene = scene.getParent())
            path.add(0, scene);
        return path;
    }


    public List<ScreenController> getSiblings(ScreenController scene)
    {
        return scene.getParent() == null ? null : scene.getParent().getChildren();
    }

    public ScreenController getNext(ScreenController scene)
    {
        int index = getSiblings(scene).indexOf(scene);
        ScreenController next;
        if (index < getSiblings(scene).size() - 1)
            next = getSiblings(scene).get(index + 1);
        else
            next = getNext(scene.getParent());
        return getVisibleChild(next);
    }

    public ScreenController getVisibleChild(ScreenController scene)
    {
        if (scene.getView() != null)
            return scene;
        for (ScreenController s : scene.getChildren())
            if (getVisibleChild(s) != null)
                return getVisibleChild(s);
        return null;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
