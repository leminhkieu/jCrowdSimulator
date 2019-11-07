package crowdsimrunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
import de.fhg.ivi.crowdsimulation.crowd.Crowd;
import de.fhg.ivi.crowdsimulation.crowd.NewPedestrian;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
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
import java.io.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;


/**
 * Custom class to run the Crowd Simulator. It creates a rectangular area H meters high by W wide. Agents are
 * spawned on the left and through waypoints to the other end.
 */
public class CrowdSimRunnerMain extends CrowdSimulation {

    // What the current status of the model is
    public enum STATUS {
        RUNNING, // The model is running, don't create any more agents
        FINISHING;
    }
    public static STATUS status = STATUS.RUNNING; // Model starts in initialisation mode

    private static double agentCreateRate = 3; // Number of agents to create each spawn time   - 2, 4, 6
    private static final int createInterval = 1 ; // Interval between creating agents (in seconds)
    private static final long runTime = 300; // Run time, in seconds
    //private static final int numIntervals = 5000; // Number of times to spawn new agents

    private static final int SPEED_UP_FACTOR = 1; // Speed up by x times (if 1 then run in real time)

    // Write out aggregate info (like the the mean velocity) every second
    private static BufferedWriter aggregateWriter = null;

    private static final int WIDTH = 115;
    private static final int HEIGHT= 30;

    private static Route route = null; // All agents will follw the same route

    private static final GeometryFactory geomFac = new GeometryFactory();

    enum DATA {
        BERLIN,
        DRESDEN,
        OPENBOUNDARY // Used for a custom scenario (i.e. not reading input data)
    }

    //static DATA simData = DATA.DRESDEN;
    static DATA simData = DATA.OPENBOUNDARY;

    public CrowdSimRunnerMain() {
        super();
    }

    /**
     * Control the simulation programatically once it has started running.
     * @throws CrowdSimulatorNotValidException
     */
    public void fireActions() throws CrowdSimulatorNotValidException {
        /* ********* Add other controls here ********* */

        CrowdSimulator.DEFAULT_MEAN_NORMAL_DESIRED_VELOCITY = 1.3f;
        CrowdSimulator.DEFAULT_STANDARD_DEVIATION_OF_NORMAL_DESIRED_VELOCITY = 0.3f;

        // Start the simulation
        System.out.print("Starting the simulation ... ");
        System.out.println("Status: "+CrowdSimRunnerMain.status);
        StartSimulation starter = new StartSimulation(this);
        // Any old ActionEvent can be fired to kick off the StarSimulation action:
        starter.actionPerformed(new ActionEvent(this, 1, "none"));

        // Speed up the simulation
        this.crowdSimulator.setFastForwardFactor(SPEED_UP_FACTOR);

        // Open the results file for aggreagate results (i.e. not for individual agents)
        /*
        try {
            CrowdSimRunnerMain.aggregateWriter = new BufferedWriter(new FileWriter(
                    "./results/r-aggregate-" + System.currentTimeMillis() + ".csv"));
            System.out.println("Opened aggregate file write: "+CrowdSimRunnerMain.aggregateWriter.toString());

            CrowdSimRunnerMain.aggregateWriter.write("Time,MeanVelocity,MeanForce,CreateRate\n"); // An
        } catch (IOException e) {
            e.printStackTrace();
        }

        */

        System.out.println("... simulation started");


        // Create agents at a partiular rate


        while (CrowdSimRunnerMain.status== STATUS.RUNNING) {
        // Every 10 seconds, add more people, and do this 10 times
        //for (int i = 0; i<CrowdSimRunnerMain.numIntervals ; i++) {
        //    if (i % 100 == 0) {
        //        System.out.println("Interval "+i+" / "+CrowdSimRunnerMain.numIntervals);
        //    }
            try {
                //System.out.print("\tSleeping for "+createInterval+" seconds...");
                Thread.sleep(( createInterval* 1000 ) / SPEED_UP_FACTOR);
                //System.out.println("... woken up");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CrowdSimRunnerMain.simData == CrowdSimRunnerMain.DATA.OPENBOUNDARY) {

                    //System.out.println("Adding more people to the simulation");
                this.addPedestrians();

                if ( (this.crowdSimulator.getSimulatedTimeSpan()/1000) > runTime) {
                    CrowdSimRunnerMain.status = STATUS.FINISHING;
                    System.out.println("Run time "+runTime+" reached; finished simulation");
                    System.out.println("Status: "+CrowdSimRunnerMain.status);
                }
                this.writeAggregate(); // Write the aggregate information

            }
        }

    } // fireActions


    public static void main(String[] args) {
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
                // Close the file and make it null so that if a new simulation starts it will reopen the writer
                NewPedestrian.bw.close();
                //NewPedestrian.bw = null;
            } catch (CrowdSimulatorNotValidException | IOException e) {
                e.printStackTrace();
            }
            
        System.out.println("Finished Simulation");
    }

    @Override
    protected void loadInitialData() {
        try {
            System.out.print("Loading initial data for " + CrowdSimRunnerMain.simData +
                    " using overridden method...");
            switch (CrowdSimRunnerMain.simData) {
                case BERLIN:
                    loadCrowdAndRoute(new File("crowdSimulationUserInterface/src/main/resources/data/berlin/crowd1.shp"),
                            new File("crowdSimulationUserInterface/src/main/resources/data/berlin/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("crowdSimulationUserInterface/src/main/resources/data/berlin/crowd2.shp"),
                            new File("crowdSimulationUserInterface/src/main/resources/data/berlin/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("crowdSimulationUserInterface/src/main/resources/data/berlin/boundariesGK5.shp"), false);
                    break;
                case DRESDEN:
                    loadCrowdAndRoute(new File("crowdSimulationUserInterface/src/main/resources/data/dresden/crowd1.shp"),
                            new File("crowdSimulationUserInterface/src/main/resources/data/dresden/waypoints1.shp"), Color.BLUE, false);
                    loadCrowdAndRoute(new File("crowdSimulationUserInterface/src/main/resources/data/dresden/crowd2.shp"),
                            new File("crowdSimulationUserInterface/src/main/resources/data/dresden/waypoints2.shp"), Color.RED, false);
                    loadBoundaries(new File("crowdSimulationUserInterface/src/main/resources/data/dresden/boundaries.shp"), false);
                    break;
                case OPENBOUNDARY:
                    // Need a CRS
                    CoordinateReferenceSystem crs = CRS.parseWKT("PROJCS[\"OSGB 1936 / British National Grid\",GEOGCS[\"OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]],AUTHORITY[\"EPSG\",\"6277\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4277\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],AUTHORITY[\"EPSG\",\"27700\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]");
                    super.crowdSimulator.setCrs(crs);

                    // Initialise the waypoints, these are used by all crowds.
                    // Make one waypoint near the beginning and another at the end of the corridor
                    List<Geometry> waypoints = new ArrayList<>();
                    waypoints.add(geomFac.createPoint(new Coordinate(Math.round(WIDTH-1), Math.round(17))));
                    waypoints.add(geomFac.createPoint(new Coordinate(Math.round(WIDTH), Math.round(17))));
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
                    // Bottom boundary
                    boundaries.add(geomFac.createLineString(new Coordinate[]{ // Line along the bottom
                            new Coordinate(0, 0), // from
                            //new Coordinate(WIDTH, 0) // to
                            new Coordinate(WIDTH, 15) // to
                    }));

                    // Left boundary
                    boundaries.add(geomFac.createLineString(new Coordinate[]{ // Line along the bottom
                            new Coordinate(0, 0), // from
                            new Coordinate(0, HEIGHT) // to
                    }));

                    // Top boundary
                    boundaries.add(geomFac.createLineString((new Coordinate[]{ // Line along the top
                            new Coordinate(0, HEIGHT),
                            //new Coordinate(WIDTH, HEIGHT)
                            new Coordinate(WIDTH,20)
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
     */
    private Crowd addPedestrians()  {

        // create a list of Geometry objects to represent the people
        List<Geometry> people = new ArrayList<>();
        // Create people spread evenly vertically within the corridor
        //CrowdSimRunnerMain.agentCreateRate*=1.0001;
        //System.out.println(CrowdSimRunnerMain.agentCreateRate);
        for (int i = 0; i < CrowdSimRunnerMain.agentCreateRate; i++) {
            // Complicated way to calculate agent position, scaling to within 1 m of the upper and lower boundaries
            // (from https://stackoverflow.com/questions/5294955/how-to-scale-down-a-range-of-numbers-with-a-known-min-and-max-value)
            //double pos = ((i+1)/(double)CrowdSimRunnerMain.agentCreateRate); // (called 'x' on the website above)
            //double pos = ((i+1)/(int)CrowdSimRunnerMain.agentCreateRate); // (called 'x' on the website above)
            double a = 2; // min value we want to scale to
            double b = HEIGHT-2; // max value we want to scale to
            //double y =  b;
            double y = (b-a) * Math.random() +a;
            //double x = 10 * Math.random();
            double x = 10;

            //System.out.println(y);

            people.add(geomFac.createPoint(new Coordinate(x, y )));
        }

        // create crowd object
        NewPedestrian.agentLock.lock(); // Make sure no agents are removed at the same time
        VisualCrowd crowd = null;
        boolean success = false;
        int counter = 0;
        while(success==false) { // Might need to try a couple of times to add agents if the new ones are in the same positon as existing ones
            counter++;
            if (counter>5) {
                System.err.println("Have tried too many times to add a new agent without success, exitting.");
                System.exit(1);
            }
            try {
                crowd = super.crowdSimulator.createVisualCrowd(people, false, Color.BLUE);
                crowd.setRoute(CrowdSimRunnerMain.route, super.crowdSimulator.getFastForwardClock().currentTimeMillis(), false);
                // add crowd
                super.crowdSimulator.addCrowd(crowd, false);
                success = true;
            } catch (CrowdSimulatorNotValidException | ConcurrentModificationException | NullPointerException e) {
                // First get the stack trace as a string so that everything can be printed at once
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                System.err.println("********************************************\n"+
                        "CrowdSimRunnerMain.addPedestrians caught an exception, will attempt to add crowd again (try no."+counter+").\n"+
                        "Message: "+e.getMessage()+"\n"+
                sw.toString()
                );
                //e.printStackTrace();
                System.err.println("********************************************");
            }
        }
        NewPedestrian.agentLock.unlock();
        return crowd;
    }

    private void writeAggregate() {
        // Write the mean information
        /*
        double totalVelocity = 0.0;
        double totalForce = 0.0;
        int numAgents = 0;
        for (Crowd c:this.crowdSimulator.getCrowds()) {
            for (Pedestrian p: c.getPedestrians()) {
                totalVelocity += p.getCurrentVelocity();
                try {
                    totalForce += p.getTotalForce().lengthSquared(); // Length squared avoids sqare root calculation
                }
                catch (NullPointerException e) {
                    System.err.println("Unable to calculate force for this agent, ignoring it.");
                }
                numAgents++;
            } // for pedestrians
        } // for crowds

        try {
            CrowdSimRunnerMain.aggregateWriter.write(String.format("%s,%.4f,%.4f,%.4f\n",
                    this.crowdSimulator.getSimulatedTimeSpan(),
                    totalVelocity/(double)numAgents,
                    totalForce/(double)numAgents,
                    CrowdSimRunnerMain.agentCreateRate
            ));
            CrowdSimRunnerMain.aggregateWriter.flush();
        } catch (IOException e) {
            System.err.println("Error writing aggreate info: "+e.getMessage());
            e.printStackTrace();
        }
    */
    }

}
