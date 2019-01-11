import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.ui.CrowdSimulation;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 * Custom class to run the Crowd Simulator
 */
public class Main extends CrowdSimulation {

    public static void main(String[] args) {
        {
            // set system look and feel (adapts the look of java to the systems default look)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                System.err.println("Could not set Look and Feel.");
                e.printStackTrace();
            }
            @SuppressWarnings("unused")
            CrowdSimulation ps = new CrowdSimulation();
        }
    }

    @Override
    protected void loadInitialData() {
        try
        {

            // test data dresden prager strasse
            loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd1.shp"),
                    new File("src/main/resources/data/dresden/waypoints1.shp"), Color.BLUE, false);
            loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd2.shp"),
                    new File("src/main/resources/data/dresden/waypoints2.shp"), Color.RED, false);
            loadBoundaries(new File("src/main/resources/data/dresden/boundaries.shp"), false);

        }
        catch (CrowdSimulatorNotValidException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            logger.debug("CrowdSimulation.loadInitialData(), ", e);
        }    }
}
