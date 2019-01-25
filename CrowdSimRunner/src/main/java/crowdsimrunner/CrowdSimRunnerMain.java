package crowdsimrunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.crowd.wayfindingmodel.route.Route;
import de.fhg.ivi.crowdsimulation.ui.CrowdSimulation;
import de.fhg.ivi.crowdsimulation.ui.extension.VisualCrowd;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Custom class to run the Crowd Simulator
 */
public class CrowdSimRunnerMain extends CrowdSimulation {

    int NumAgents = 100; // Number of agents to create when not reading data
    int NumWaypoints = 5; // Number of waypoints

    private static final GeometryFactory geomFac = new GeometryFactory();

    enum DATA {
        BERLIN,
        DRESDEN,
        CUSTOM // Used for a custom scenario (i.e. not reading input data)
    }

    //static DATA simData = DATA.DRESDEN;
    static DATA simData = DATA.CUSTOM;

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
        try {
            System.out.print("Loading initial data for " + CrowdSimRunnerMain.simData +
                    " using overridden method...");
            switch (CrowdSimRunnerMain.simData) {
                case BERLIN:
                    loadCrowdAndRoute(new File("src/main/resources/data/berlin/crowd1.shp"),
                            new File("src/main/resources/data/berlin/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("src/main/resources/data/berlin/crowd2.shp"),
                            new File("src/main/resources/data/berlin/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("src/main/resources/data/berlin/boundariesGK5.shp"), false);
                    break;
                case DRESDEN:
                    loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd1.shp"),
                            new File("src/main/resources/data/dresden/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("src/main/resources/data/dresden/crowd2.shp"),
                            new File("src/main/resources/data/dresden/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("src/main/resources/data/dresden/boundaries.shp"), false);
                    break;
                case CUSTOM:
                    // Need a CRS
                    CoordinateReferenceSystem crs = CRS.decode("EPSG:27700");

                    // Load pedestrians
                    // create a list of Geometry objects to represent the people
                    List<Geometry> people = new ArrayList<Geometry>();
                    for (int i = 0; i < NumAgents; i++) {
                        people.add(geomFac.createPoint(new Coordinate(1.0, 1.0)));
                    }

                    // create crowd object
                    VisualCrowd crowd = super.crowdSimulator.createVisualCrowd(people, false, Color.BLUE);

                    // Load waypoints
                    List<Geometry> waypoints = new ArrayList<Geometry>();
                    for (int i = 0; i < NumWaypoints; i++) {
                        people.add(geomFac.createPoint(new Coordinate(1.0 + i, 1.0 + i)));
                    }

                    Route route = super.crowdSimulator.getRouteFactory()
                            .createRouteFromGeometries(waypoints);
                    crowd.setRoute(route, super.crowdSimulator.getFastForwardClock().currentTimeMillis(),
                            false);

                    // set crs
                    super.crowdSimulator.setCrs(crs);

                    // add crowd
                    super.crowdSimulator.addCrowd(crowd, false);

                    // expand bounding box by crowd
                    Envelope e = crowd.getBoundingBox();
                    super.crowdSimulator.expandBoundingBox(e);

                    // expand bounding box by route of crowd
                    if (crowd.getRoute() != null) {
                        super.crowdSimulator.expandBoundingBox(crowd.getRoute().getBoundingBox());
                    }


                    // LOAD BOUNDARIES



                    break;
            }
        } catch (CrowdSimulatorNotValidException e)  {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            //logger.debug("CrowdSimulation.loadInitialData(), ", e);
            System.err.println("Error: CrowdSimulation.loadInitialData()");
        } catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            System.err.println("Error: CrowdSimulation.loadInitialData()");
            e.printStackTrace();
        }

        System.out.println("...Finished loading data");
    }
}
