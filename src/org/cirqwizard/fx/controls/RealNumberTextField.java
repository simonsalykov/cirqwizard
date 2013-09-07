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


public class RealNumberTextField extends TextField
{
    private StringProperty realNumberTextProperty = new SimpleStringProperty();

    public final String getRealNumberText()
    {
        return realNumberTextProperty.get();
    }

    public StringProperty realNumberTextProperty()
    {
        return realNumberTextProperty;
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
                        Double.parseDouble(newValue);
                        realNumberTextProperty.setValue(newValue);
                    }
                    catch (Exception e)
                    {
                        getStyleClass().add("validation-error");
                        realNumberTextProperty.setValue(null);
                    }
                }
            }
        });
    }
}