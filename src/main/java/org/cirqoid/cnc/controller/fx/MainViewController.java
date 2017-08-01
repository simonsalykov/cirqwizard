package org.cirqoid.cnc.controller.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.HomeCommand;
import org.cirqoid.cnc.controller.commands.RapidMotionCommand;
import org.cirqoid.cnc.controller.interpreter.Interpreter;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqoid.cnc.controller.serial.CirqoidInitializer;
import org.cirqoid.cnc.controller.serial.SerialException;
import org.cirqoid.cnc.controller.serial.SerialInterface;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by simon on 13.06.17.
 */
public class MainViewController implements Initializable
{
    @FXML private VBox view;

    @FXML private TextField xCoordinate;
    @FXML private TextField yCoordinate;
    @FXML private TextField zCoordinate;
    @FXML private TextField aCoordinate;
    @FXML private TextArea gcodeTextArea;

    private SerialInterface serialInterface;
    private int yDiff = -1039;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        xCoordinate.setOnAction(event -> rapid());
        yCoordinate.setOnAction(event -> rapid());
        zCoordinate.setOnAction(event -> rapid());
        aCoordinate.setOnAction(event -> rapid());
    }

    public VBox getView()
    {
        return view;
    }

    public void setSerialInterface(SerialInterface serialInterface)
    {
        this.serialInterface = serialInterface;
    }

    public void rapid()
    {
        RapidMotionCommand packet = new RapidMotionCommand(new int[] {Integer.valueOf(xCoordinate.getText()),
                Integer.valueOf(yCoordinate.getText()),
                        Integer.valueOf(zCoordinate.getText()), Integer.valueOf(aCoordinate.getText())});
        packet.setId(serialInterface.getPacketId());
        try
        {
            serialInterface.send(packet);
        }
        catch (SerialException e)
        {
            e.printStackTrace();
        }

    }

    public void home()
    {
        HomeCommand packet = new HomeCommand(new int[]{0, yDiff, 0, 0});
        packet.setId(serialInterface.getPacketId());
        try
        {
            serialInterface.send(packet);
        }
        catch (SerialException e)
        {
            e.printStackTrace();
        }
    }

    public void send()
    {
        LineNumberReader reader = new LineNumberReader(new StringReader(gcodeTextArea.getText()));
        String str;
        Interpreter interpreter = new Interpreter();
        try
        {
            while ((str = reader.readLine()) != null)
            {
                List<Command> packets = interpreter.interpretBlock(str);
                for (Command p : packets)
                {
                    p.setId(serialInterface.getPacketId());
                    serialInterface.send(p);
                }
            }
        }
        catch (IOException e)
        {
        }
        catch (ParsingException e)
        {
            e.printStackTrace();
        }
        catch (SerialException e)
        {
            e.printStackTrace();
        }
    }


}
