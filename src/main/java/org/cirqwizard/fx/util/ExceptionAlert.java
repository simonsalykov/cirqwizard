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
package org.cirqwizard.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionAlert extends Alert
{
    public ExceptionAlert(String title, String headerText, String contentText, Exception exception)
    {
        super(AlertType.ERROR);
        setTitle(title);
        setHeaderText(headerText);
        setContentText(contentText);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        TextArea stackTraceArea = new TextArea(stringWriter.toString());
        stackTraceArea.setEditable(false);
        stackTraceArea.setWrapText(true);
        stackTraceArea.setMaxWidth(Double.MAX_VALUE);
        stackTraceArea.setMaxHeight(Double.MAX_VALUE);
        getDialogPane().setExpandableContent(stackTraceArea);
    }
}
