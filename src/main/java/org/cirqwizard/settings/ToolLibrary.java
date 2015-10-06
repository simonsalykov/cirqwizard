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
    private ToolSettings[] toolSettings = new ToolSettings[] {getDefaultTool()};

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
