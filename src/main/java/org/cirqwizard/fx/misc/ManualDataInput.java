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

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.interpreter.Context;
import org.cirqoid.cnc.controller.interpreter.Interpreter;
import org.cirqoid.cnc.controller.interpreter.ParsingException;
import org.cirqwizard.fx.ScreenController;
import org.cirqwizard.fx.services.SerialInterfaceCommandsService;
import org.cirqwizard.fx.services.SerialInterfaceService;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class ManualDataInput extends ScreenController
{
    @FXML private Region veil;
    @FXML private Button executeGCodeButton;
    @FXML private TextArea gCodeInputTextArea;

    @FXML private VBox executionPane;
    @FXML private ProgressBar executionProgressBar;
    @FXML private Label timeElapsedLabel;
    @FXML private Label errorMessageLabel;

    private SerialInterfaceCommandsService serialService;

    @Override
    protected String getFxmlName()
    {
        return "/org/cirqwizard/fx/misc/ManualDataInput.fxml";
    }

    @Override
    protected String getName()
    {
        return "Direct GCode";
    }

    @Override
    public void refresh()
    {
        boolean noMachineConnected = getMainApplication().getCNCController() == null;
        executeGCodeButton.setDisable(noMachineConnected);

        serialService = new SerialInterfaceCommandsService(getMainApplication());
        executionProgressBar.progressProperty().bind(serialService.progressProperty());
        timeElapsedLabel.textProperty().bind(serialService.executionTimeProperty());
        executionPane.visibleProperty().bind(serialService.runningProperty());
        veil.visibleProperty().bind(serialService.runningProperty());
        setError(null);
    }

    public void executeGCode()
    {
        setError(null);
        Interpreter interpreter = getMainApplication().getCNCController().getInterpreter();
        Context contextBackup = (Context) interpreter.getContext().clone();

        LineNumberReader reader = new LineNumberReader(new StringReader(gCodeInputTextArea.getText()));
        List<Command> result = new ArrayList<>();
        String str;
        int position = 0;
        int l = 0;
        try
        {
            while ((str = reader.readLine()) != null)
            {
                l = str.length() + 1;
                result.addAll(interpreter.interpretBlock(str));
                position += str.length() + 1;
            }

            serialService.setCommands(result);
            serialService.restart();
        }
        catch (IOException e) {}
        catch (ParsingException e)
        {
            setError(e.getMessage());
            gCodeInputTextArea.selectRange(position, position + l);
            interpreter.setContext(contextBackup);
        }
    }

    private void setError(String error)
    {
        if (error == null)
        {
            errorMessageLabel.setManaged(false);
            errorMessageLabel.setVisible(false);
        }
        else
        {
            errorMessageLabel.setVisible(true);
            errorMessageLabel.setManaged(true);
            errorMessageLabel.setText(error);
        }
    }

    public void stopExecution()
    {
        serialService.cancel();
    }
}
