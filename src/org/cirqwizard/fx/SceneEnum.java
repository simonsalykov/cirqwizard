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

package org.cirqwizard.fx;


import org.cirqwizard.fx.traces.InsertTool;

import java.util.ArrayList;
import java.util.List;

public enum SceneEnum
{
    Welcome("Home", "welcome.fxml"),
    Orientation("Orientation", "orientation.fxml"),
    Homing("Homing", "homing.fxml"),
    JobSelection("Job selection", "job-selection.fxml"),
    PCBPlacement("Placement", null),
    Message("Tool", null),
    ZOffset("Z offset", "ZOffset.fxml"),
    XYOffsets("X and Y offsets", "XYOffsets.fxml"),
    Machining("Machining", "machining/Machining.fxml"),
    SettingsEditor("Settings", "SettingsEditor.fxml"),
    Firmware("Firmware", "Firmware.fxml"),
    BleedingSyringe("Bleeding", "dispensing/SyringeBleeding.fxml"),
    PlacingOverview("Overview", "pp/PlacingOverview.fxml"),
    FeederSelection("Panel", "pp/FeederSelection.fxml"),
    ComponentPlacement("Placement", "pp/ComponentPlacement.fxml"),
    About("About", "About.fxml"),
    ManualMovement("Manual control", "ManualMovement.fxml"),
    ManualDataInput("MDI", "ManualDataInput.fxml");

//    MainView(null, "MainView.fxml"),

//    TopTraces("Top traces", null),
//    TopTraces_PCBPlacement("Placement", null, new org.cirqwizard.fx.traces.top.PCBPlacement()),
//    TopTraces_InsertTool("Tool", null, new InsertTool());

    private String fxml;
    private String name;
    private ScreenController controller;
    private SceneEnum parent;
    private List<SceneEnum> children = new ArrayList<>();

    private SceneEnum(String name, String fxml)
    {
        this(name, fxml, null);
    }

    private SceneEnum(String name, String fxml, ScreenController controller)
    {
        this.name = name;
        this.fxml = fxml;
        this.controller = controller;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFxml()
    {
        return fxml;
    }

    public List<SceneEnum> getChildren()
    {
        return children;
    }

    public ScreenController getController()
    {
        return controller;
    }

    public SceneEnum addChild(SceneEnum child)
    {
        getChildren().add(child);
        child.setParent(this);
        return this;
    }

    public SceneEnum getParent()
    {
        return parent;
    }

    public void setParent(SceneEnum parent)
    {
        this.parent = parent;
    }
}
