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

import javafx.scene.control.TableCell;
import org.cirqwizard.settings.ApplicationConstants;

public class RealNumberTextFieldTableCell<S> extends TableCell<S, Integer>
{
    private RealNumberTextField textField = new RealNumberTextField();

    public RealNumberTextFieldTableCell()
    {
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setOnAction(event -> commitEdit(textField.getIntegerValue()));
        setText("");
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
        setText(null);
        setGraphic(textField);
        textField.requestFocus();
    }

    @Override
    public void cancelEdit()
    {
        super.cancelEdit();
        setGraphic(null);
        setText(textField.getText());
    }

    @Override
    public void commitEdit(Integer newValue)
    {
        super.commitEdit(newValue);
        setGraphic(null);
        setText(textField.getText());
    }

    @Override
    protected void updateItem(Integer item, boolean empty)
    {
        super.updateItem(item, empty);
        if (empty || item == null)
        {
            setGraphic(null);
            setText(null);
        }
        textField.setIntegerValue(item);
        if (isEditing())
        {
            setGraphic(textField);
        }
        else
        {
            setGraphic(null);
            setText(empty || item == null ? null : ApplicationConstants.formatInteger(item));
        }
    }
}
