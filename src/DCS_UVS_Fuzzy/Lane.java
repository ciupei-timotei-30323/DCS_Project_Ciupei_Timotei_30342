
package DCS_UVS_Fuzzy;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.FLRS;
import DataOnly.FV;
import DataOnly.Fuzzy;
import DataOnly.TransferOperation;
import Enumerations.FZ;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

import java.util.ArrayList;
import DataOnly.PlaceNameWithWeight;

public class Lane {
    public static FLRS flrs_move() {
        return new FLRS(
            new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL)
        );
    }

    public static PetriNet createLane(int laneId, String supervisorIp, int supervisorPort, String intersectionIp, int intersectionPort, String intersectionInputPlace) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Lane " + laneId;
        pn.NetworkPort = 1090 + laneId;

        // Places
        DataFuzzy p_i = new DataFuzzy();
        p_i.SetName("P_i" + laneId);
        pn.PlaceList.add(p_i);

        DataFuzzy p_l = new DataFuzzy();
        p_l.SetName("P_l" + laneId);
        pn.PlaceList.add(p_l);

        DataFuzzy p_l2 = new DataFuzzy();
        p_l2.SetName("P_l2" + laneId);
        pn.PlaceList.add(p_l2);

        DataFuzzy p_tl = new DataFuzzy();
        p_tl.SetName("P_TL" + laneId);
        pn.PlaceList.add(p_tl);

        // Constant Green Place
        DataFuzzy green = new DataFuzzy();
        green.SetName("ConstantGreen");
        green.SetValue(new Fuzzy(0.0f)); // Green signal (ZR)
        pn.ConstantPlaceList.add(green);
        
        // Supervisor Output Port
        DataTransfer p_sup_out = new DataTransfer();
        p_sup_out.SetName("P_sup_out" + laneId);
        p_sup_out.Value = new TransferOperation(supervisorIp, Integer.toString(supervisorPort), "Inp_1");
        pn.PlaceList.add(p_sup_out);

        // Intersection Output Port
        DataTransfer p_out = new DataTransfer();
        p_out.SetName("P_out" + laneId);
        p_out.Value = new TransferOperation(intersectionIp, Integer.toString(intersectionPort), intersectionInputPlace);
        pn.PlaceList.add(p_out);

        // Transitions
        // t_in
        PetriTransition t_in = new PetriTransition(pn);
        t_in.TransitionName = "t_in" + laneId;
        t_in.InputPlaceName.add("P_i" + laneId);

        Condition tin_c1 = new Condition(t_in, "P_i" + laneId, TransitionCondition.NotNull);
        GuardMapping tin_gm = new GuardMapping();
        tin_gm.condition = tin_c1;

        ArrayList<PlaceNameWithWeight> tin_input = new ArrayList<>();
        tin_input.add(new PlaceNameWithWeight("P_i" + laneId, 1.0f));
        ArrayList<String> tin_output = new ArrayList<>();
        tin_output.add("P_l" + laneId);

        tin_gm.Activations.add(new Activation(t_in, flrs_move(), tin_input, TransitionOperation.FLRS, tin_output));
        t_in.GuardMappingList.add(tin_gm);
        pn.Transitions.add(t_in);

        // t_send
        PetriTransition t_send = new PetriTransition(pn);
        t_send.TransitionName = "t_send" + laneId;
        t_send.InputPlaceName.add("P_l" + laneId);

        Condition tsend_c1 = new Condition(t_send, "P_l" + laneId, TransitionCondition.NotNull);
        GuardMapping tsend_gm = new GuardMapping();
        tsend_gm.condition = tsend_c1;

        ArrayList<PlaceNameWithWeight> tsend_input = new ArrayList<>();
        tsend_input.add(new PlaceNameWithWeight("P_l" + laneId, 1.0f));
        ArrayList<String> tsend_output1 = new ArrayList<>();
        tsend_output1.add("P_l2" + laneId);
        
        ArrayList<String> tsend_output2 = new ArrayList<>();
        tsend_output2.add("P_sup_out" + laneId);

        tsend_gm.Activations.add(new Activation(t_send, flrs_move(), tsend_input, TransitionOperation.FLRS, tsend_output1));
        tsend_gm.Activations.add(new Activation(t_send, flrs_move(), tsend_input, TransitionOperation.FLRS, tsend_output2));
        t_send.GuardMappingList.add(tsend_gm);
        pn.Transitions.add(t_send);

        // t_out
        PetriTransition t_out = new PetriTransition(pn);
        t_out.TransitionName = "t_out" + laneId;
        t_out.InputPlaceName.add("P_l2" + laneId);
        t_out.InputPlaceName.add("P_TL" + laneId);
        t_out.InputPlaceName.add("ConstantGreen");

        Condition tout_c1 = new Condition(t_out, "P_l2" + laneId, TransitionCondition.NotNull);
        Condition tout_c2 = new Condition(t_out, "P_TL" + laneId, TransitionCondition.Equal, "ConstantGreen");
        tout_c1.SetNextCondition(LogicConnector.AND, tout_c2);

        GuardMapping tout_gm = new GuardMapping();
        tout_gm.condition = tout_c1;
        
        ArrayList<PlaceNameWithWeight> tout_input = new ArrayList<>();
        tout_input.add(new PlaceNameWithWeight("P_l2" + laneId, 1.0f));
        ArrayList<String> tout_output = new ArrayList<>();
        tout_output.add("P_out" + laneId);

        tout_gm.Activations.add(new Activation(t_out, flrs_move(), tout_input, TransitionOperation.FLRS, tout_output));
        t_out.GuardMappingList.add(tout_gm);
        t_out.Delay=5;
        pn.Transitions.add(t_out);

        return pn;
    }
}
