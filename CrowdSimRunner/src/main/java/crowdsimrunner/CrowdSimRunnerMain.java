package crowdsimrunner;

import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.ui.CrowdSimulation;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 * Custom class to run the Crowd Simulator
 */
public class CrowdSimRunnerMain extends CrowdSimulation {

    enum DATA {
        BERLIN,
        DRESDEN
    }

    // DATA simData = DATA.BERLIN;
    DATA simData = DATA.DRESDEN;

    public CrowdSimRunnerMain() {
        super();
    }



    public static void main(String[] args) {
        {
            System.out.println("Starting Crowd Sim Runner");
            // set system look and feel (adapts the look of java to the systems default look)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                System.err.println("Could not set Look and Feel.");
                e.printStackTrace();
            }
            CrowdSimRunnerMain csrm = new CrowdSimRunnerMain();
            //@SuppressWarnings("unused")
            //CrowdSimulation ps = new CrowdSimulation();
        }
    }

    @Override
    protected void loadInitialData() {
        try
        {
            System.out.println("Loading initial data using overridden method");
            switch (simData) {
                case BERLIN:
                    loadCrowdAndRoute(new File("src/main/resources/data/berlin/crowd1.shp"),
                            new File("src/main/resources/data/berlin/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("src/main/resources/data/berlin/crowd2.shp"),
                            new File("src/main/resources/data/berlin/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("src/main/resources/data/berlin/boundariesGK5.shp"), false);
                case DRESDEN:
                    loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd1.shp"),
                            new File("src/main/resources/data/dresden/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd2.shp"),
                            new File("src/main/resources/data/dresden/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("src/main/resources/data/dresden/boundaries.shp"), false);

            }


        }
        catch (CrowdSimulatorNotValidException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            //logger.debug("CrowdSimulation.loadInitialData(), ", e);
            System.err.println("Error: CrowdSimulation.loadInitialData()");
        }    }
}
