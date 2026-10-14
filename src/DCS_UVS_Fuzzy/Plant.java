
package DCS_UVS_Fuzzy;

import Components.PetriNet;
import Components.PetriNetWindow;
import DataObjects.DataFuzzy;
import DataOnly.Fuzzy;

public class Plant {
    public static void main(String[] args) {
        String supervisorIp = "localhost";
        int supervisorPort = 1070;

        String intersectionIp = "localhost";
        int intersectionPort = 1080;

        String exitLaneIp = "localhost";
        int[] exitLanePorts = {1101, 1102, 1103, 1104};

        // Create Intersection
        PetriNet intersectionPN = Intersection.createIntersection(intersectionPort, exitLaneIp, exitLanePorts);
        // Add initial value to P_in1 for testing
        // DataFuzzy pIn1 = (DataFuzzy) intersectionPN.PlaceList.stream().filter(p -> p.GetName().equals("P_in1")).findFirst().get();
        // pIn1.SetValue(new Fuzzy(0.5f));
        PetriNetWindow intersectionFrame = new PetriNetWindow(false);
        intersectionFrame.petriNet = intersectionPN;
        intersectionFrame.setTitle("Intersection Petri Net");
        intersectionFrame.setVisible(true);

        // Create Lanes
        for (int i = 1; i <= 4; i++) {
            final int laneIndex = i;
            PetriNet lanePN = Lane.createLane(i, supervisorIp, supervisorPort, intersectionIp, intersectionPort, "P_in" + i);

            // Add initial value to P_i for testing
            DataFuzzy p_i = (DataFuzzy) lanePN.PlaceList.stream().filter(p -> p.GetName().equals("P_i" + laneIndex)).findFirst().get();
            p_i.SetValue(new Fuzzy(0.7f));

            // Add initial green light for testing
            DataFuzzy p_tl = (DataFuzzy) lanePN.PlaceList.stream().filter(p -> p.GetName().equals("P_TL" + laneIndex)).findFirst().get();
            p_tl.SetValue(new Fuzzy(0.0f)); // Green signal (ZR)

            PetriNetWindow laneFrame = new PetriNetWindow(false);
            laneFrame.petriNet = lanePN;
            laneFrame.setTitle("Lane " + i + " Petri Net");
            laneFrame.setVisible(true);
        }

        // Create Exit Lanes
        for (int i = 1; i <= 4; i++) {
            PetriNet exitLanePN = ExitLane.createExitLane(i, exitLanePorts[i - 1]);
            PetriNetWindow exitLaneFrame = new PetriNetWindow(false);
            exitLaneFrame.petriNet = exitLanePN;
            exitLaneFrame.setTitle("Exit Lane " + i + " Petri Net");
            exitLaneFrame.setVisible(true);
        }
    }
}
