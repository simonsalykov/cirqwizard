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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.io.IOException;


public class PCBPane extends Region
{
    private static final double DEFAULT_SCALE = 0.005;

    private Group group = new Group();
    private Property<ObservableList<Shape>> items = new SimpleListProperty<>();
    private Property<Double> scaleProperty = new SimpleObjectProperty<>(DEFAULT_SCALE);

    private Translate translateTransform = new Translate(0, 0);
    private Scale scaleTransform = new Scale(scaleProperty().getValue(), -scaleProperty().getValue());

    private double boardWidth;
    private double boardHeight;

    public PCBPane()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PcbPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
            getChildren().add(group);
            group.getTransforms().add(translateTransform);
            group.getTransforms().add(scaleTransform);
            layoutBoundsProperty().addListener(new ChangeListener<Bounds>()
                {
                    @Override
                    public void changed(ObservableValue<? extends Bounds> observableValue, Bounds bounds, Bounds bounds2)
                    {
                bestFit();
                }
            });
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        scaleProperty.addListener(new ChangeListener<Double>()
        {
            @Override
            public void changed(ObservableValue<? extends Double> observableValue, Double aDouble, Double aDouble2)
            {
                rescale();
            }
        });
        items.addListener(new ChangeListener<ObservableList<Shape>>()
        {
            @Override
            public void changed(ObservableValue<? extends ObservableList<Shape>> observableValue, ObservableList<Shape> shapes, ObservableList<Shape> shapes2)
            {
                group.getChildren().clear();
                if (shapes2 != null && !shapes2.isEmpty())
                    group.getChildren().addAll(shapes2);
                boardWidth = group.getLayoutBounds().getWidth();
                boardHeight = group.getLayoutBounds().getHeight();
                setPrefSize(boardWidth * scaleProperty.getValue(), boardHeight * scaleProperty.getValue());
                bestFit();
                rescale();
            }
        });
    }

    public Property<ObservableList<Shape>> itemsProperty()
    {
        return items;
    }

    public Property<Double> scaleProperty()
    {
        return scaleProperty;
    }

    public void bestFit()
    {
        double scale = Math.min(getWidth() / group.getLayoutBounds().getWidth(), getHeight() / group.getLayoutBounds().getHeight());
        scaleProperty.setValue(scale);
    }

    private void rescale()
    {
        scaleTransform.setX(scaleProperty.getValue());
        scaleTransform.setY(-scaleProperty.getValue());
        translateTransform.setY(boardHeight * scaleProperty.getValue());
        setPrefSize(boardWidth * scaleProperty.getValue(), boardHeight * scaleProperty.getValue());
    }

    public double getUnscaledWidth()
    {
        return group.getLayoutBounds().getWidth();
    }

    public double getUnscaledHeight()
    {
        return group.getLayoutBounds().getHeight();
    }

}
