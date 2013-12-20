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

package org.cirqwizard.fx.controls;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import org.cirqwizard.settings.Settings;

import java.text.DecimalFormat;


public class RealNumberTextField extends TextField
{
    private StringProperty realNumberTextProperty = new SimpleStringProperty();
    private ObjectProperty<Integer> realNumberIntegerProperty = new SimpleObjectProperty<>();

    private DecimalFormat format = new DecimalFormat("0.0");

    public final String getRealNumberText()
    {
        return realNumberTextProperty.get();
    }

    public StringProperty realNumberTextProperty()
    {
        return realNumberTextProperty;
    }

    public Integer getIntegerValue()
    {
        return realNumberIntegerProperty.getValue();
    }

    public ObjectProperty<Integer> realNumberIntegerProperty()
    {
        return realNumberIntegerProperty;
    }

    public void setIntegerValue(Integer value)
    {
        if (value == null)
            setText(null);
        else
            setText(format.format((double) value / Settings.RESOLUTION));
    }

    public RealNumberTextField()
    {
        super();
        this.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue)
            {
                getStyleClass().removeAll("validation-error");
                if (newValue == null || newValue.trim().isEmpty())
                    realNumberTextProperty.setValue(null);
                else
                {
                    try
                    {
                        newValue = newValue.trim().replace(",", ".");
                        realNumberIntegerProperty.setValue((int)(Double.parseDouble(newValue) * Settings.RESOLUTION));
                        realNumberTextProperty.setValue(newValue);
                    }
                    catch (Exception e)
                    {
                        getStyleClass().add("validation-error");
                        realNumberTextProperty.setValue(null);
                        realNumberIntegerProperty.setValue(null);
                    }
                }
            }
        });
    }
}