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

package org.cirqwizard.settings;

import java.util.logging.Level;

public class ApplicationSettings extends SettingsGroup
{
    @PersistentPreference
    private UserPreference<String> serialPort = new UserPreference<>("Serial port", null, "", PreferenceType.SERIAL_PORT);

    @PersistentPreference
    private UserPreference<Level> logLevel = new UserPreference<>("Log level", Level.INFO, "").setItems(Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
            Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL).setInstantiator(Level::parse);

    @PersistentPreference
    private UserPreference<Integer> processingThreads = new UserPreference<>("Processing threads", Runtime.getRuntime().availableProcessors(), "", PreferenceType.INTEGER);

    @Override
    public String getName()
    {
        return "Application";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "application";
    }

    public UserPreference<String> getSerialPort()
    {
        return serialPort;
    }

    public void setSerialPort(UserPreference<String> serialPort)
    {
        this.serialPort = serialPort;
    }

    public UserPreference<Level> getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(UserPreference<Level> logLevel)
    {
        this.logLevel = logLevel;
    }

    public UserPreference<Integer> getProcessingThreads()
    {
        return processingThreads;
    }

    public void setProcessingThreads(UserPreference<Integer> processingThreads)
    {
        this.processingThreads = processingThreads;
    }
}
