
package DCS_UVS_Fuzzy;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataOnly.PlaceNameWithWeight;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

import java.util.ArrayList;

public class ExitLane {
    public static PetriNet createExitLane(int laneId, int port) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "ExitLane " + laneId;
        pn.NetworkPort = port;

        // Places
        DataFuzzy p_ok = new DataFuzzy();
        p_ok.SetName("P_o" + laneId);
        pn.PlaceList.add(p_ok);

        DataFuzzy p_exitk = new DataFuzzy();
        p_exitk.SetName("P_exit" + laneId);
        pn.PlaceList.add(p_exitk);

        // Transitions
        PetriTransition t_ok = new PetriTransition(pn);
        t_ok.TransitionName = "t_o" + laneId;
        t_ok.InputPlaceName.add("P_o" + laneId);

        Condition tok_c1 = new Condition(t_ok, "P_o" + laneId, TransitionCondition.NotNull);
        GuardMapping tok_gm = new GuardMapping();
        tok_gm.condition = tok_c1;

        ArrayList<PlaceNameWithWeight> tok_input = new ArrayList<>();
        tok_input.add(new PlaceNameWithWeight("P_o" + laneId, 1.0f));
        ArrayList<String> tok_output = new ArrayList<>();
        tok_output.add("P_exit" + laneId);

        tok_gm.Activations.add(new Activation(t_ok, Lane.flrs_move(), tok_input, TransitionOperation.FLRS, tok_output));
        t_ok.GuardMappingList.add(tok_gm);
        pn.Transitions.add(t_ok);

        return pn;
    }
}
