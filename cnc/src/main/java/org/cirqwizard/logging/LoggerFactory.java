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

package org.cirqwizard.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class LoggerFactory
{
    private static Logger applicationLogger;
    private static Logger serialLogger;

    static
    {
        applicationLogger = Logger.getLogger("cirqwizard-application");
        serialLogger = Logger.getLogger("cirqwizard-serial");
        try
        {
            FileHandler applicationFileHandler = new FileHandler("%t/cirqwizard-application.log", true);
            applicationFileHandler.setFormatter(new SimpleFormatter());
            applicationLogger.addHandler(applicationFileHandler);
        }
        catch (IOException e)
        {
            applicationLogger.log(Level.SEVERE, "Could not create file handler for application logger", e);
        }
        try
        {
            FileHandler serialFileHandler = new FileHandler("%t/cirqwizard-serial.log", true);
            serialFileHandler.setFormatter(new SerialLogFormatter());
            serialLogger.setLevel(Level.ALL);
            serialLogger.addHandler(serialFileHandler);
        }
        catch (IOException e)
        {
            applicationLogger.log(Level.SEVERE, "Could not create file handler for serial logger");
        }
    }

    public static Logger getApplicationLogger()
    {
        return applicationLogger;
    }

    public static Logger getSerialLogger()
    {
        return serialLogger;
    }

    public static void logException(String message, Throwable t)
    {
        applicationLogger.log(Level.SEVERE, message,t);
    }

}
