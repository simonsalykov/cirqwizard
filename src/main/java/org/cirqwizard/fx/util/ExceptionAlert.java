package org.cirqwizard.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by simon on 24.09.15.
 */
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
