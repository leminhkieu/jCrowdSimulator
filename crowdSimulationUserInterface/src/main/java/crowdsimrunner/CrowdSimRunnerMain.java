package crowdsimrunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.crowd.Crowd;
import de.fhg.ivi.crowdsimulation.crowd.wayfindingmodel.route.Route;
import de.fhg.ivi.crowdsimulation.geom.GeometryTools;
import de.fhg.ivi.crowdsimulation.ui.CrowdSimulation;
import de.fhg.ivi.crowdsimulation.ui.extension.VisualCrowd;
import de.fhg.ivi.crowdsimulation.ui.gui.control.actions.simulation.StartSimulation;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Custom class to run the Crowd Simulator. It creates a rectangular area H meters high by W wide. Agents are
 * spawned on the left and through waypoints to the other end.
 */
public class CrowdSimRunnerMain extends CrowdSimulation {

    private static final int NumAgents = 10; // Number of agents to create each spawn time
    private static final int createInterval = 1; // Interval between creating agents (in seconds)
    private static final int numIntervals = 100; // Number of times to spawn new agents

    private static final int SPEED_UP_FACTOR = 10; // Speed up by x times

    private static final int WIDTH = 50;
    private static final int HEIGHT= 10;
    public static final int XLIM = WIDTH -10 ; // The point at which agents leave the simulation

    private static Route route = null; // All agents will follw the same route

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

    /**
     * Control the simulation programatically once it has started running.
     * @throws CrowdSimulatorNotValidException
     */
    public void fireActions() throws CrowdSimulatorNotValidException {
        /* ********* Add other controls here ********* */

        // Start the simulation
        System.out.print("Starting the simulation ... ");
        StartSimulation starter = new StartSimulation(this);
        // Any old ActionEvent can be fired to kick off the StarSimulation action:
        starter.actionPerformed(new ActionEvent(this, 1, "none"));

        // Speed up the simulation
        this.crowdSimulator.setFastForwardFactor(SPEED_UP_FACTOR);

        System.out.println("... simulation started");

        // Every 10 seconds, add more people, and do this 10 times
        for (int i = 0; i<CrowdSimRunnerMain.numIntervals ; i++) {
            try {
                //System.out.print("\tSleeping for "+createInterval+" seconds...");
                Thread.sleep(( createInterval* 1000 ) / SPEED_UP_FACTOR);
                //System.out.println("... woken up");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Adding more people to the simulation");
            this.addPedestrians();
        }
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
            try {
                csrm.fireActions();
            } catch (CrowdSimulatorNotValidException e) {
                e.printStackTrace();
            }
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
                    CoordinateReferenceSystem crs = CRS.parseWKT("PROJCS[\"OSGB 1936 / British National Grid\",GEOGCS[\"OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]],AUTHORITY[\"EPSG\",\"6277\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4277\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],AUTHORITY[\"EPSG\",\"27700\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]");
                    super.crowdSimulator.setCrs(crs);

                    // Initialise the waypoints, these are used by all crowds.
                    // Make one waypoint near the beginning and another at the end of the corridor
                    List<Geometry> waypoints = new ArrayList<>();
                    waypoints.add(geomFac.createPoint(new Coordinate(Math.round(WIDTH/3), Math.round(HEIGHT/2))));
                    waypoints.add(geomFac.createPoint(new Coordinate(Math.round(WIDTH-1), Math.round(HEIGHT/2))));
                    CrowdSimRunnerMain.route = super.crowdSimulator.getRouteFactory().createRouteFromGeometries(waypoints);


                    // Load initial pedestrians
                    Crowd firstCrowd = this.addPedestrians();

                    // expand bounding box by crowd
                    super.crowdSimulator.expandBoundingBox(firstCrowd.getBoundingBox());

                    // expand bounding box by route of crowd
                    if (firstCrowd.getRoute() != null) {
                        super.crowdSimulator.expandBoundingBox(firstCrowd.getRoute().getBoundingBox());
                    }


                    // LOAD BOUNDARIES. One big corridor now
                    List<Geometry> boundaries = new ArrayList<>();
                    boundaries.add(geomFac.createLineString(new Coordinate[]{ // Line along the bottom
                            new Coordinate(0, 0),
                            new Coordinate(WIDTH, 0)
                    }));
                    boundaries.add(geomFac.createLineString((new Coordinate[]{ // Line along the top
                            new Coordinate(0, HEIGHT),
                            new Coordinate(WIDTH, HEIGHT)
                    })));

                    // A rectangle
                    /*boundaries.add(geomFac.createPolygon(new Coordinate[] {
                            new Coordinate(0, 0),
                            new Coordinate(WIDTH, 0),
                            new Coordinate(WIDTH, HEIGHT),
                            new Coordinate(0, HEIGHT),
                            new Coordinate(0, 0)
                    }));*/

                    super.crowdSimulator.addBoundaries(boundaries, false);

                    // set bounding box
                    super.crowdSimulator.expandBoundingBox(GeometryTools.getEnvelope(boundaries));

                    // paint
                    if (map != null)
                    {
                        map.resetMapExtent();
                        map.repaint();
                    }


                    break;
            }
        } catch (CrowdSimulatorNotValidException e)  {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            //logger.debug("CrowdSimulation.loadInitialData(), ", e);
            System.err.println("Error: CrowdSimulation.loadInitialData()");
        } catch (NoSuchAuthorityCodeException e) {
            System.err.println("Error: CrowdSimulation.loadInitialData()");
            e.printStackTrace();
        } catch (FactoryException e) {
            System.err.println("Error: CrowdSimulation.loadInitialData()");
            e.printStackTrace();
        }

        System.out.println("...Finished loading data");

        System.out.println("Have created "+super.crowdSimulator.getCrowds().size()+" crowd.");
        //for (VisualCrowd c : super.crowdSimulator.getCrowds()) {
        //    System.out.println("\t"+c.toString());
        //    System.out.println("\tNumPedestrians: "+c.getPedestrians());
        //}

    }

    /**
     * Add pedestrians to the simulation.
     * @return The crowd that has been added (you don't need to actually do anything with the crowd,
     * it is added as a side effect).
     * @throws CrowdSimulatorNotValidException
     */
    private Crowd addPedestrians() throws CrowdSimulatorNotValidException {

        // create a list of Geometry objects to represent the people
        List<Geometry> people = new ArrayList<>();
        // Create people spread evenly vertically within the corridor
        for (int i = 0; i < CrowdSimRunnerMain.NumAgents; i++) {
            // Complicated way to calculate agent position, scaling to within 1 m of the upper and lower boundaries
            // (from https://stackoverflow.com/questions/5294955/how-to-scale-down-a-range-of-numbers-with-a-known-min-and-max-value)
            double pos = ((i+1)/(double)CrowdSimRunnerMain.NumAgents); // (called 'x' on the website above)
            double a = 1; // min value we want to scale to
            double b = HEIGHT-1; // max value we want to scale to
            double y = ( ((b-a)*(pos-0)) / (1-0) ) + a;
            //System.out.println(y);

            people.add(geomFac.createPoint(new Coordinate(5.0, y )));
        }

        // create crowd object
        VisualCrowd crowd = super.crowdSimulator.createVisualCrowd(people, false, Color.BLUE);
        crowd.setRoute(CrowdSimRunnerMain.route, super.crowdSimulator.getFastForwardClock().currentTimeMillis(), false);


        // add crowd
        super.crowdSimulator.addCrowd(crowd, false);
        return crowd;
    }
}