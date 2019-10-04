package crowdsimrunner;

import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.crowd.NewPedestrian;

import javax.swing.*;
import java.io.IOException;

public class CrowdSimRunnerBatch {

    static final int N = 20;

    public static void main(String args[]) {
        for (int i = 0; i < N; i++) {
            System.out.println("\n************* RUNNING BATCH: "+i+"\n");
            CrowdSimRunnerMain.main(args);
        }
    }
}
