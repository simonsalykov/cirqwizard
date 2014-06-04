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


public enum SceneEnum
{
    Welcome("welcome.fxml"),
    Orientation("orientation.fxml"),
    Homing("homing.fxml"),
    JobSelection("job-selection.fxml"),
    PCBPlacement("PCBPlacement.fxml"),
    Message("Message.fxml"),
    ZOffset("ZOffset.fxml"),
    XYOffsets("XYOffsets.fxml"),
    Machining("machining/Machining.fxml"),
    Settings("Settings.fxml"),
    Firmware("Firmware.fxml"),
    BleedingSyringe("dispensing/SyringeBleeding.fxml"),
    PlacingOverview("pp/PlacingOverview.fxml"),
    FeederSelection("pp/FeederSelection.fxml"),
    ComponentPlacement("pp/ComponentPlacement.fxml"),
    About("About.fxml"),
    ManualMovement("ManualMovement.fxml"),
    ManualDataInput("ManualDataInput.fxml");

    private String name;

    private SceneEnum(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
