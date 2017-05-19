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
package org.cirqwizard.fx.panel;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.Panel;
import org.cirqwizard.layers.PanelBoard;
import org.cirqwizard.logging.LoggerFactory;
import org.cirqwizard.settings.ApplicationConstants;
import org.cirqwizard.settings.SettingsFactory;

import java.io.File;
import java.io.IOException;

public class PanelValidator
{
    private Panel panel;
    private VBox errorBox;
    private CheckBox ignoreErrorCheckBox;
    private Runnable saveAndRefresh;

    public PanelValidator(Panel panel, VBox errorBox, CheckBox ignoreErrorCheckbox, Runnable saveAndRefresh)
    {
        this.panel = panel;
        this.errorBox = errorBox;
        this.ignoreErrorCheckBox = ignoreErrorCheckbox;
        this.saveAndRefresh = saveAndRefresh;
    }

    public void validateBoards()
    {
        errorBox.getChildren().clear();
        panel.getBoards().stream().forEach(b ->
        {
            if (!validateFit(panel, b))
                errorBox.getChildren().add(createErrorLabel("Board " + trimBoardName(b.getFilename()) +
                        " does not fit in the panel"));
            if (!validatePinClearance(panel, b))
                errorBox.getChildren().add(createErrorLabel("Board " + trimBoardName(b.getFilename()) +
                        " overlaps with registration pins"));
            if (!b.getBoard().hasLayers())
            {
                Text text = new Text("Board " + trimBoardName(b.getFilename()) +
                        " could not be found. Perhaps the files were moved?");
                text.getStyleClass().add("label");
                Hyperlink hyperlink = new Hyperlink("Locate files");
                hyperlink.setOnAction(event -> locateMissingFiles(b));
                TextFlow flow = new TextFlow(text, hyperlink);
                flow.getStyleClass().add("error-box");
                errorBox.getChildren().add(flow);
            }
        });
        for (int i = 0; i < panel.getBoards().size(); i++)
        {
            for (int j = i + 1; j < panel.getBoards().size(); j++)
            {
                PanelBoard b1 = panel.getBoards().get(i);
                PanelBoard b2 = panel.getBoards().get(j);
                if (!validateBoardsOverlap(b1, b2))
                    errorBox.getChildren().add(createErrorLabel("Boards " + trimBoardName(b1.getFilename()) + " and " +
                            trimBoardName(b2.getFilename()) + " overlap"));
            }
        }
        if (!errorBox.getChildren().isEmpty() && errorBox.getChildren().stream().noneMatch(n -> n instanceof TextFlow))
            errorBox.getChildren().add(ignoreErrorCheckBox);
        errorBox.setVisible(!errorBox.getChildren().isEmpty());
        errorBox.setManaged(errorBox.isVisible());
        ignoreErrorCheckBox.setSelected(false);
        errorBox.getParent().layout();
        errorBox.getParent().layout();
        errorBox.getParent().layout();
    }

    private String trimBoardName(String fullName)
    {
        return fullName.substring(fullName.lastIndexOf(File.separatorChar) + 1, fullName.length());
    }

    private void locateMissingFiles(PanelBoard board)
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All supported files", "*.sol", "*.cmp"));
        File file = chooser.showOpenDialog(null);
        if (file != null)
        {
            try
            {
                board.setFilename(file.getAbsolutePath().substring(0,
                        file.getAbsolutePath().lastIndexOf('.')));
                board.loadBoard();
                if (board.getBoard().hasLayers())
                {
                    validateBoards();
                    saveAndRefresh.run();
                }
            }
            catch (IOException e)
            {
                LoggerFactory.logException("Could not load board data", e);
            }
        }
    }

    private Label createErrorLabel(String message)
    {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("error-box");
        label.setPrefWidth(1000);
        label.setMinSize(200, Label.USE_PREF_SIZE);
        return label;
    }

    private boolean validateFit(Panel panel, PanelBoard board)
    {
        return !(board.getX() < 0 || board.getY() < 0 ||
                board.getX() + board.getBoard().getWidth() > panel.getSize().getWidth() ||
                board.getY() + board.getBoard().getHeight() > panel.getSize().getHeight());
    }

    private boolean validatePinClearance(Panel panel, PanelBoard board)
    {
        for (Point p : panel.getPinLocations())
            if (boardContainsPoint(board, p, ApplicationConstants.REGISTRATION_PIN_RADIUS))
                return false;
        return true;
    }

    private Point[] getBoardExtremePoints(PanelBoard board)
    {
        int offset = board.isGenerateOutline() ?
                SettingsFactory.getContourMillingSettings().getGenerationToolDiameter().getValue() : 0;
        return new Point[]{
                new Point(board.getX() - offset, board.getY() - offset),
                new Point(board.getX() - offset, board.getY() + board.getBoard().getHeight() + offset),
                new Point(board.getX() + board.getBoard().getWidth() + offset,
                        board.getY() + board.getBoard().getHeight() + offset),
                new Point(board.getX() + board.getBoard().getWidth() + offset, board.getY() - offset)
        };
    }

    private boolean boardContainsPoint(PanelBoard board, Point point, int radius)
    {
        Point[] extremePoints = getBoardExtremePoints(board);
        if (point.getX() >= extremePoints[0].getX() && point.getX() <= extremePoints[3].getX() &&
                point.getY() >= extremePoints[0].getY() && point.getY() <= extremePoints[1].getY())
            return true;
        if (lineIntersectsCircle(extremePoints[0], extremePoints[1], point, radius))
            return true;
        if (lineIntersectsCircle(extremePoints[1], extremePoints[2], point, radius))
            return true;
        if (lineIntersectsCircle(extremePoints[2], extremePoints[3], point, radius))
            return true;
        if (lineIntersectsCircle(extremePoints[3], extremePoints[0], point, radius))
            return true;
        return false;
    }

    private boolean lineIntersectsCircle(Point lineFrom, Point lineTo, Point circleCenter, int radius)
    {
        double lineLength = lineFrom.distanceTo(lineTo);
        Point directionVector = lineTo.subtract(lineFrom);
        double dx = directionVector.getX() / lineLength;
        double dy = directionVector.getY() / lineLength;
        Point tt = circleCenter.subtract(lineFrom);
        double t = dx * tt.getX() + dy * tt.getY();
        Point circleCenterProjection = new Point((int)(t * dx * lineFrom.getX()), (int)(t * dy * lineFrom.getY()));
        return circleCenterProjection.distanceTo(circleCenter) <= radius;
    }

    private boolean validateBoardsOverlap(PanelBoard board1, PanelBoard board2)
    {
        for (Point p : getBoardExtremePoints(board2))
            if (boardContainsPoint(board1, p, 0))
                return false;
        return true;
    }

}
