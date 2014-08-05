package org.cirqwizard.settings;

import java.util.logging.Level;

/**
 * Created by simon on 05/08/14.
 */
public class ApplicationSettings extends SettingsGroup
{
    @PersistentPreference
    private UserPreference<String> serialPort = new UserPreference<>("Serial port", null, "", PreferenceType.SERIAL_PORT);

    @PersistentPreference
    private UserPreference<Level> logLevel = new UserPreference<>("Log level", Level.INFO, "").setItems(Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
            Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL).setInstantiator(Level::parse);

    @PersistentPreference
    private UserPreference<Integer> excellonIntegerPlaces = new UserPreference<>("Excellon integer places", 2, "", PreferenceType.INTEGER);

    @PersistentPreference
    private UserPreference<Integer> excellonDecimalPlaces = new UserPreference<>("Excellon decimal places", 4, "", PreferenceType.INTEGER);

    @PersistentPreference
    private UserPreference<DistanceUnit> excellonUnits = new UserPreference<>("Excellon units", DistanceUnit.INCHES, "").setItems(DistanceUnit.values()).
            setInstantiator(DistanceUnit::valueOf);

    @PersistentPreference
    private UserPreference<String> centroidFileFormat = new UserPreference<>("Centroid file format", "(?<name>\\S+)\\s+(?<x>\\d+.?\\d*)\\s+(?<y>\\d+.?\\d*)\\s+(?<angle>\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?", "");

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

    public UserPreference<Integer> getExcellonIntegerPlaces()
    {
        return excellonIntegerPlaces;
    }

    public void setExcellonIntegerPlaces(UserPreference<Integer> excellonIntegerPlaces)
    {
        this.excellonIntegerPlaces = excellonIntegerPlaces;
    }

    public UserPreference<Integer> getExcellonDecimalPlaces()
    {
        return excellonDecimalPlaces;
    }

    public void setExcellonDecimalPlaces(UserPreference<Integer> excellonDecimalPlaces)
    {
        this.excellonDecimalPlaces = excellonDecimalPlaces;
    }

    public UserPreference<DistanceUnit> getExcellonUnits()
    {
        return excellonUnits;
    }

    public void setExcellonUnits(UserPreference<DistanceUnit> excellonUnits)
    {
        this.excellonUnits = excellonUnits;
    }

    public UserPreference<String> getCentroidFileFormat()
    {
        return centroidFileFormat;
    }

    public void setCentroidFileFormat(UserPreference<String> centroidFileFormat)
    {
        this.centroidFileFormat = centroidFileFormat;
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
