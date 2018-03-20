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

import org.cirqwizard.logging.LoggerFactory;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Root
public class ToolLibrary
{
    @ElementArray
    private ToolSettings[] toolSettings = new ToolSettings[]
    {
        new ToolSettings("15 deg. 0.10mm end mill", 100, 1390, 75_000, 50_000, 30, 0, 0, 50, false),
        new ToolSettings("15 deg. 0.13mm end mill", 130, 1390, 150_000, 75_000, 30, 0, 0, 50, false),
        new ToolSettings("15 deg. 0.18mm end mill", 180, 1390, 400_000, 150_000, 30, 0, 0, 50, false),
        new ToolSettings("15 deg. 0.254mm end mill", 254, 1390, 1000_000, 300_000, 30, 0, 0, 50, false),
        new ToolSettings("V tool 0.1-0.15mm", 120, 1390, 300_000, 100_000, 30, 0, 0, 50, false),
        new ToolSettings("V tool 0.2-0.5mm", 300, 1390, 800_000, 300_000, 30, 0, 0, 50, false),
        new ToolSettings("0.4mm rub out end mill", 400, 1390, 800_000, 250_000, 30, 0, 0, 50, false),
        new ToolSettings("0.8mm rub out end mill", 800, 1390, 1000_000, 50_000, 30, 0, 0, 50, false),
        new ToolSettings("1.0mm rub out end mill", 1_000, 1390, 1000_000, 300_000, 30, 0, 0, 50, false),
        new ToolSettings("1.5mm rub out end mill", 1_500, 1390, 1000_000, 300_000, 30, 0, 0, 50, false),
        new ToolSettings("2.0mm rub out end mill", 2_000, 1390, 1000_000, 300_000, 30, 0, 0, 50, false),
        new ToolSettings("1.0mm contour end mill", 1_000, 1390, 300_000, 100_000, 50, 0, 0, 50, false),
        new ToolSettings("2.0mm contour end mill", 2_000, 1390, 300_000, 100_000, 50, 0, 0, 50, false)
    };

    public ToolSettings[] getToolSettings()
    {
        return toolSettings;
    }

    public void setToolSettings(ToolSettings[] toolSettings)
    {
        this.toolSettings = toolSettings;
    }

    public static ToolLibrary load() throws Exception
    {
        Path file = Paths.get(System.getProperty("user.home"), ".cirqwizard", "tool table.xml");
        if (!Files.exists(file))
            return new ToolLibrary();
        return (new Persister()).read(ToolLibrary.class, file.toFile());
    }

    public static void reset()
    {
        try
        {
            Path file = Paths.get(System.getProperty("user.home"), ".cirqwizard", "tool table.xml");
            if (Files.exists(file))
                Files.delete(file);
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not delete tool table", e);
        }
    }

    public void save()
    {
        try
        {
            Path directory = Paths.get(System.getProperty("user.home"), ".cirqwizard");
            if (!Files.exists(directory))
                Files.createDirectory(directory);
            Path file = Paths.get(directory.toString(), "tool table.xml");
            if (!Files.exists(file))
                Files.createFile(file);
            (new Persister()).write(this, file.toFile());
        }
        catch (Exception e)
        {
            LoggerFactory.logException("Could not save tool table", e);
        }
    }

    public static ToolSettings getDefaultTool()
    {
        ToolSettings tool = new ToolSettings();
        tool.setName("Default tool");
        tool.setDiameter(300);
        tool.setSpeed(1390);
        tool.setFeedXY(300_000);
        tool.setFeedZ(300_000);
        tool.setArcs(100);
        tool.setAdditionalPasses(0);
        tool.setAdditionalPassesOverlap(50);
        tool.setAdditionalPassesPadsOnly(false);
        return tool;
    }
}
