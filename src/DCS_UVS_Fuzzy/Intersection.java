
package DCS_UVS_Fuzzy;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFuzzy;
import DataObjects.DataTransfer;
import DataOnly.*;
import Enumerations.FZ;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

import java.util.ArrayList;

public class Intersection {
    public static FLRS flrs_plus() {
        return new FLRS(
                new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR),
                new FV(FZ.NL), new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM),
                new FV(FZ.NL), new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL),
                new FV(FZ.NM), new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL),
                new FV(FZ.ZR), new FV(FZ.PM), new FV(FZ.PL), new FV(FZ.PL), new FV(FZ.PL)
        );
    }

    public static PetriNet createIntersection(int intersectionPort, String exitLaneIp, int[] exitLanePorts) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Intersection";
        pn.NetworkPort = intersectionPort;

        // Input Places
        for (int i = 1; i <= 4; i++) {
            DataFuzzy p_in = new DataFuzzy();
            p_in.SetName("P_in" + i);
            pn.PlaceList.add(p_in);
        }

        // Intermediate Adder Places
        DataFuzzy p_add1 = new DataFuzzy();
        p_add1.SetName("P_add1");
        pn.PlaceList.add(p_add1);

        DataFuzzy p_add2 = new DataFuzzy();
        p_add2.SetName("P_add2");
        pn.PlaceList.add(p_add2);

        // Central Intersection Place
        DataFuzzy p_i = new DataFuzzy();
        p_i.SetName("P_i");
        pn.PlaceList.add(p_i);

        // Constant Weight Places for Splitting
        float[] weights = {0.25f, 0.50f, 0.12f, 0.13f};
        for (int j = 0; j < weights.length; j++) {
            DataFuzzy weight = new DataFuzzy();
            weight.SetName("Weight" + (j + 1));
            weight.SetValue(new Fuzzy(weights[j]));
            pn.ConstantPlaceList.add(weight);
        }

        // Post-Split Intermediate Places and Output DataTransfer Places
        for (int k = 1; k <= 4; k++) {
            DataFuzzy p_o = new DataFuzzy();
            p_o.SetName("P_o" + k);
            pn.PlaceList.add(p_o);

            DataTransfer p_out = new DataTransfer();
            p_out.SetName("P_out" + k);
            p_out.Value = new TransferOperation(exitLaneIp, Integer.toString(exitLanePorts[k - 1]), "P_o" + k);
            pn.PlaceList.add(p_out);
        }

        // Transitions
        // Merging/Summing Stage (t1, t2, t3)
        PetriTransition t1 = createAdderTransition(pn, "t1", "P_in1", "P_in2", "P_add1");
        pn.Transitions.add(t1);

        PetriTransition t2 = createAdderTransition(pn, "t2", "P_in3", "P_in4", "P_add2");
        pn.Transitions.add(t2);

        PetriTransition t3 = createAdderTransition(pn, "t3", "P_add1", "P_add2", "P_i");
        pn.Transitions.add(t3);

        // Splitting Stage (ts)
        PetriTransition t_s = new PetriTransition(pn);
        t_s.TransitionName = "t_s";
        t_s.InputPlaceName.add("P_i");
        for (int j = 1; j <= 4; j++) {
            t_s.InputPlaceName.add("Weight" + j);
        }

        Condition ts_c1 = new Condition(t_s, "P_i", TransitionCondition.NotNull);
        GuardMapping ts_gm = new GuardMapping();
        ts_gm.condition = ts_c1;

        for (int j = 1; j <= 4; j++) {
            ArrayList<PlaceNameWithWeight> ts_input = new ArrayList<>();
            ts_input.add(new PlaceNameWithWeight("P_i", 1.0f));
            ts_input.add(new PlaceNameWithWeight("Weight" + j, 1.0f));
            ts_gm.Activations.add(new Activation(ts_input, TransitionOperation.Prod_Fuzzy, "P_o" + j, t_s));
        }
        t_s.GuardMappingList.add(ts_gm);
        pn.Transitions.add(t_s);

        // Output Transfer Stage (tout1, tout2, tout3, tout4)
        for (int k = 1; k <= 4; k++) {
            PetriTransition t_out = new PetriTransition(pn);
            t_out.TransitionName = "t_out" + k;
            t_out.InputPlaceName.add("P_o" + k);

            Condition tout_c1 = new Condition(t_out, "P_o" + k, TransitionCondition.NotNull);
            GuardMapping tout_gm = new GuardMapping();
            tout_gm.condition = tout_c1;

            ArrayList<PlaceNameWithWeight> tout_input = new ArrayList<>();
            tout_input.add(new PlaceNameWithWeight("P_o" + k, 1.0f));
            ArrayList<String> tout_output = new ArrayList<>();
            tout_output.add("P_out" + k);

            tout_gm.Activations.add(new Activation(t_out, Lane.flrs_move(), tout_input, TransitionOperation.FLRS, tout_output));
            t_out.GuardMappingList.add(tout_gm);
            pn.Transitions.add(t_out);
        }

        return pn;
    }

    private static PetriTransition createAdderTransition(PetriNet pn, String name, String in1, String in2, String out) {
        PetriTransition t = new PetriTransition(pn);
        t.TransitionName = name;
        t.InputPlaceName.add(in1);
        t.InputPlaceName.add(in2);

        Condition c1 = new Condition(t, in1, TransitionCondition.NotNull);
        Condition c2 = new Condition(t, in2, TransitionCondition.NotNull);
        c1.SetNextCondition(LogicConnector.AND, c2);

        GuardMapping gm = new GuardMapping();
        gm.condition = c1;

        ArrayList<PlaceNameWithWeight> input = new ArrayList<>();
        input.add(new PlaceNameWithWeight(in1, 1.0f));
        input.add(new PlaceNameWithWeight(in2, 1.0f));
        ArrayList<String> output = new ArrayList<>();
        output.add(out);

        gm.Activations.add(new Activation(t, flrs_plus(), input, TransitionOperation.FLRS, output));
        t.GuardMappingList.add(gm);
        return t;
    }
}
