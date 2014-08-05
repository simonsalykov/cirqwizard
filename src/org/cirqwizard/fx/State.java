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


public enum State
{
    WELCOME(SceneEnum.Welcome)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return null;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    SETTINGS(SceneEnum.SettingsEditor)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return WELCOME;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    CHECK_SETTINGS(SceneEnum.SettingsEditor)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    ABOUT(SceneEnum.About)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return WELCOME;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    MANUAL_MOVEMENT(SceneEnum.ManualMovement)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return WELCOME;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    FIRMWARE(SceneEnum.Firmware)
            {
                @Override
                public State getNextState(Context context)
                {
                    return null;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return WELCOME;
                }
            },
    ORIENTATION(SceneEnum.Orientation)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return WELCOME;
                }

                @Override
                public State getNextState(Context context)
                {
                    return HOMING;
                }
            },
    HOMING(SceneEnum.Homing)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return ORIENTATION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return JOB_SELECTION;
                }
            },
    JOB_SELECTION(SceneEnum.JobSelection)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return HOMING;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isTopTracesSelected())
                        return PCB_PLACEMENT_FOR_TOP_TRACES;
                    if (context.isBottomTracesSelected())
                        return PCB_PLACEMENT_FOR_BOTTOM_TRACES;
                    if (context.isDrillingSelected())
                        return PCB_PLACEMENT_FOR_DRILLING;
                    if (context.isContourSelected())
                        return PCB_PLACEMENT_FOR_CONTOUR;
                    if (context.isPasteSelected())
                        return PCB_PLACEMENT_FOR_DISPENSING;
                    if (context.isPlacingSelected())
                        return PCB_PLACEMENT_FOR_PLACING;
                    return TERMINAL;
                }
            },


    PCB_PLACEMENT_FOR_TOP_TRACES(SceneEnum.PCBPlacement)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return INSERTING_CUTTER_FOR_TOP_TRACES;
                }
            },
    INSERTING_CUTTER_FOR_TOP_TRACES(SceneEnum.Message)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return PCB_PLACEMENT_FOR_TOP_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    return ESTABLISHING_G54_Z_FOR_TOP_TRACES;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setzOffsetEstablished(false);
                }
            },
    ESTABLISHING_G54_Z_FOR_TOP_TRACES(SceneEnum.ZOffset)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return INSERTING_CUTTER_FOR_TOP_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    return ESTABLISHING_G54_XY_FOR_TOP_TRACES;
                }
            },
    ESTABLISHING_G54_XY_FOR_TOP_TRACES(SceneEnum.XYOffsets)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return ESTABLISHING_G54_Z_FOR_TOP_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    return MILLING_TOP_INSULATION;
                }
            },
    MILLING_TOP_INSULATION(SceneEnum.Machining)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return ESTABLISHING_G54_XY_FOR_TOP_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isBottomTracesSelected())
                        return PCB_PLACEMENT_FOR_BOTTOM_TRACES;
                    if (context.isDrillingSelected())
                        return PCB_PLACEMENT_FOR_DRILLING;
                    if (context.isContourSelected())
                        return PCB_PLACEMENT_FOR_CONTOUR;
                    if (context.isPasteSelected())
                        return PCB_PLACEMENT_FOR_DISPENSING;
                    return TERMINAL;
                }
            },

    PCB_PLACEMENT_FOR_BOTTOM_TRACES(SceneEnum.PCBPlacement)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.isTopTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_TOP_TRACES : MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isTopTracesSelected())
                        return ESTABLISHING_G54_Z_FOR_BOTTOM_TRACES;
                    return INSERTING_CUTTER_FOR_BOTTOM_TRACES;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setzOffsetEstablished(false);
                }
            },
    INSERTING_CUTTER_FOR_BOTTOM_TRACES(SceneEnum.Message)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return PCB_PLACEMENT_FOR_BOTTOM_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    return ESTABLISHING_G54_Z_FOR_BOTTOM_TRACES;
                }
            },
    ESTABLISHING_G54_Z_FOR_BOTTOM_TRACES(SceneEnum.ZOffset)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.isTopTracesSelected())
                        return PCB_PLACEMENT_FOR_BOTTOM_TRACES;
                    return INSERTING_CUTTER_FOR_BOTTOM_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isTopTracesSelected())
                        return MILLING_BOTTOM_INSULATION;
                    return ESTABLISHING_G54_XY_FOR_BOTTOM_TRACES;
                }
            },
    ESTABLISHING_G54_XY_FOR_BOTTOM_TRACES(SceneEnum.XYOffsets)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return ESTABLISHING_G54_Z_FOR_BOTTOM_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    return MILLING_BOTTOM_INSULATION;
                }
            },
    MILLING_BOTTOM_INSULATION(SceneEnum.Machining)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return ESTABLISHING_G54_XY_FOR_BOTTOM_TRACES;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isDrillingSelected())
                        return PCB_PLACEMENT_FOR_DRILLING;
                    if (context.isContourSelected())
                        return PCB_PLACEMENT_FOR_CONTOUR;
                    if (context.isPasteSelected())
                        return PCB_PLACEMENT_FOR_DISPENSING;
                    return TERMINAL;
                }
            },

    PCB_PLACEMENT_FOR_DRILLING(SceneEnum.PCBPlacement)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.isBottomTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_BOTTOM_TRACES : MILLING_BOTTOM_INSULATION;
                    if (context.isTopTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_TOP_TRACES : MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return INSERTING_DRILL;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setCurrentDrill(0);
                }
            },

    INSERTING_DRILL(SceneEnum.Message)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.getCurrentDrill() > 0)
                    {
                        context.setCurrentDrill(context.getCurrentDrill() - 1);
                        return DRILLING;
                    }
                    return PCB_PLACEMENT_FOR_DRILLING;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.getCurrentDrill() > 0 || context.isTopTracesSelected() || context.isBottomTracesSelected())
                        return DRILLING;
                    return ESTABLISHING_G54_FOR_DRILLING;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setzOffsetEstablished(false);
                }
            },
    ESTABLISHING_G54_FOR_DRILLING(SceneEnum.XYOffsets)
            {
                @Override
                public State getNextState(Context context)
                {
                    return DRILLING;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return INSERTING_DRILL;
                }
            },

    DRILLING(SceneEnum.Machining)
            {
                @Override
                public State getPrevState(Context context)
                {
                    context.setG54Z(null);
                    if (context.getCurrentDrill() > 0)
                        return INSERTING_DRILL;
                    return ESTABLISHING_G54_FOR_DRILLING;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.getCurrentDrill() < context.getDrillDiameters().size() - 1)
                    {
                        context.setCurrentDrill(context.getCurrentDrill() + 1);
                        return INSERTING_DRILL;
                    }
                    if (context.isContourSelected())
                        return INSERTING_CUTTER_FOR_CONTOUR;
                    if (context.isPasteSelected())
                        return PCB_PLACEMENT_FOR_DISPENSING;
                    return TERMINAL;
                }
            },

    PCB_PLACEMENT_FOR_CONTOUR(SceneEnum.PCBPlacement)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.isDrillingSelected())
                        return DRILLING;
                    if (context.isBottomTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_BOTTOM_TRACES : MILLING_BOTTOM_INSULATION;
                    if (context.isTopTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_TOP_TRACES : MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return INSERTING_CUTTER_FOR_CONTOUR;
                }
            },
    INSERTING_CUTTER_FOR_CONTOUR(SceneEnum.Message)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return PCB_PLACEMENT_FOR_CONTOUR;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected())
                        return MILLING_CONTOUR;
                    return ESTABLISHING_G54_XY_CONTOUR;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setzOffsetEstablished(false);
                }
            },
    ESTABLISHING_G54_XY_CONTOUR(SceneEnum.XYOffsets)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return INSERTING_CUTTER_FOR_CONTOUR;
                }

                @Override
                public State getNextState(Context context)
                {
                    return MILLING_CONTOUR;
                }
            },
    MILLING_CONTOUR(SceneEnum.Machining)
            {
                @Override
                public State getPrevState(Context context)
                {
                    context.setG54Z(null);
                    return ESTABLISHING_G54_XY_CONTOUR;
                }

                @Override
                public State getNextState(Context context)
                {
                    if (context.isPasteSelected())
                        return PCB_PLACEMENT_FOR_DISPENSING;
                    return TERMINAL;
                }
            },

    PCB_PLACEMENT_FOR_DISPENSING(SceneEnum.PCBPlacement)
            {
                @Override
                public State getNextState(Context context)
                {
                    return INSERTING_SYRINGE;
                }

                @Override
                public State getPrevState(Context context)
                {
                    if (context.isContourSelected())
                        return MILLING_CONTOUR;
                    if (context.isDrillingSelected())
                        return DRILLING;
                    if (context.isBottomTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_BOTTOM_TRACES : MILLING_BOTTOM_INSULATION;
                    if (context.isTopTracesSelected())
                        return context.getG54Z() == null ? INSERTING_CUTTER_FOR_TOP_TRACES : MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }
            },
    INSERTING_SYRINGE(SceneEnum.Message)
            {
                @Override
                public State getNextState(Context context)
                {
                    return BLEEDING_SYRINGE;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return PCB_PLACEMENT_FOR_DISPENSING;
                }
            },
    BLEEDING_SYRINGE(SceneEnum.BleedingSyringe)
            {
                @Override
                public State getNextState(Context context)
                {
                    return ESTABLISHING_G54_XY_FOR_DISPENSING;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return INSERTING_SYRINGE;
                }
            },
    ESTABLISHING_G54_XY_FOR_DISPENSING(SceneEnum.XYOffsets)
            {
                @Override
                public State getNextState(Context context)
                {
                    return DISPENSING;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return BLEEDING_SYRINGE;
                }
            },
    DISPENSING(SceneEnum.Machining)
            {
                @Override
                public State getNextState(Context context)
                {
                    if (context.isPlacingSelected())
                        return PCB_PLACEMENT_FOR_PLACING;
                    return TERMINAL;
                }

                @Override
                public State getPrevState(Context context)
                {
                    if (context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected() ||
                            context.isContourSelected())
                        return BLEEDING_SYRINGE;
                    return ESTABLISHING_G54_XY_FOR_DISPENSING;
                }
            },

    PCB_PLACEMENT_FOR_PLACING(SceneEnum.PCBPlacement)
            {
                @Override
                public State getNextState(Context context)
                {
                    return INSERTING_PP_HEAD;
                }

                @Override
                public State getPrevState(Context context)
                {
                    if (context.isPasteSelected())
                        return DISPENSING;
                    if (context.isDrillingSelected())
                        return DRILLING;
                    if (context.isContourSelected())
                        return MILLING_CONTOUR;
                    if (context.isBottomTracesSelected())
                        return MILLING_BOTTOM_INSULATION;
                    if (context.isTopTracesSelected())
                        return MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }

                @Override
                public void onActivation(Context context)
                {
                    context.setCurrentComponent(0);
                }
            },
    INSERTING_PP_HEAD(SceneEnum.Message)
            {
                @Override
                public State getNextState(Context context)
                {
                    if (context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected() ||
                            context.isContourSelected() || context.isPasteSelected())
                        return PLACING_OVERVIEW;
                    return ESTABLISHING_G54_XY_FOR_PLACING;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return PCB_PLACEMENT_FOR_PLACING;
                }
            },
    ESTABLISHING_G54_XY_FOR_PLACING(SceneEnum.XYOffsets)
            {
                @Override
                public State getNextState(Context context)
                {
                    return PLACING_OVERVIEW;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return INSERTING_PP_HEAD;
                }
            },
    PLACING_OVERVIEW(SceneEnum.PlacingOverview)
            {
                @Override
                public State getNextState(Context context)
                {
                    return PLACING_FEEDER_SELECTION;
                }

                @Override
                public State getPrevState(Context context)
                {

                    if (context.isTopTracesSelected() || context.isBottomTracesSelected() || context.isDrillingSelected() ||
                            context.isContourSelected() || context.isPasteSelected())
                        return INSERTING_PP_HEAD;
                    return ESTABLISHING_G54_XY_FOR_PLACING;
                }
            },
    PLACING_FEEDER_SELECTION(SceneEnum.FeederSelection)
            {
                @Override
                public State getNextState(Context context)
                {
                    return COMPONENT_PLACEMENT;
                }

                @Override
                public State getPrevState(Context context)
                {
                    if (context.getCurrentComponent() > 0)
                    {
                        context.setCurrentComponent(context.getCurrentComponent() - 1);
                        return COMPONENT_PLACEMENT;
                    }
                    return PLACING_OVERVIEW;
                }
            },
    COMPONENT_PLACEMENT(SceneEnum.ComponentPlacement)
            {
                @Override
                public State getNextState(Context context)
                {
                    if (context.getCurrentComponent() < context.getComponentIds().size() - 1)
                    {
                        context.setCurrentComponent(context.getCurrentComponent() + 1);
                        return PLACING_FEEDER_SELECTION;
                    }
                    return TERMINAL;
                }

                @Override
                public State getPrevState(Context context)
                {
                    return PLACING_FEEDER_SELECTION;
                }
            },
    TERMINAL(SceneEnum.Message)
            {
                @Override
                public State getPrevState(Context context)
                {
                    if (context.isPlacingSelected())
                        return COMPONENT_PLACEMENT;
                    if (context.isPasteSelected())
                        return DISPENSING;
                    if (context.isContourSelected())
                        return MILLING_CONTOUR;
                    if (context.isDrillingSelected())
                        return DRILLING;
                    if (context.isBottomTracesSelected())
                        return MILLING_BOTTOM_INSULATION;
                    if (context.isTopTracesSelected())
                        return MILLING_TOP_INSULATION;
                    return JOB_SELECTION;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            },
    MANUAL_DATA_INPUT(SceneEnum.ManualDataInput)
            {
                @Override
                public State getPrevState(Context context)
                {
                    return MANUAL_MOVEMENT;
                }

                @Override
                public State getNextState(Context context)
                {
                    return null;
                }
            };

    private SceneEnum scene;

    private State(SceneEnum scene)
    {
        this.scene = scene;
    }

    public SceneEnum getScene()
    {
        return scene;
    }

    public abstract State getNextState(Context context);
    public abstract State getPrevState(Context context);

    public void onActivation(Context context)
    {
    }

}
