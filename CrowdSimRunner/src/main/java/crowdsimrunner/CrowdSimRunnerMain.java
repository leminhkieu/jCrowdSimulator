package crowdsimrunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.fhg.ivi.crowdsimulation.CrowdSimulatorNotValidException;
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
 * Custom class to run the Crowd Simulator
 */
public class CrowdSimRunnerMain extends CrowdSimulation {

    private static int NumAgents = 100; // Number of agents to create when not reading data
    private static int NumWaypoints = 5; // Number of waypoints

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

        /* ********* Add other controls here ********* */

        // Start the simulation
        System.out.print("Starting the simulation ... ");
        StartSimulation starter = new StartSimulation(this);
        // Any old ActionEvent can be fired to kick off the StarSimulation action:
        starter.actionPerformed(new ActionEvent(this, 1, "none"));
        System.out.println("... simulation started");
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
                    //CoordinateReferenceSystem crs = CRS.decode("EPSG:27700");
                    CoordinateReferenceSystem crs = CRS.parseWKT("PROJCS[\"OSGB 1936 / British National Grid\",GEOGCS[\"OSGB 1936\",DATUM[\"OSGB_1936\",SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]],AUTHORITY[\"EPSG\",\"6277\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4277\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.9996012717],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000],AUTHORITY[\"EPSG\",\"27700\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]");

                    // Load pedestrians
                    // create a list of Geometry objects to represent the people
                    List<Geometry> people = new ArrayList<>();
                    for (int i = 0; i < CrowdSimRunnerMain.NumAgents; i++) {
                        people.add(geomFac.createPoint(new Coordinate(1.0, 1.0+(i*0.1))));
                    }

                    // create crowd object
                    VisualCrowd crowd = super.crowdSimulator.createVisualCrowd(people, false, Color.BLUE);

                    // Load waypoints
                    List<Geometry> waypoints = new ArrayList<>();
                    //for (int i = 0; i < CrowdSimRunnerMain.NumWaypoints; i++) {
                    waypoints.add(geomFac.createPoint(new Coordinate(20.0, 10.0 )));
                    waypoints.add(geomFac.createPoint(new Coordinate(50.0, 10.0 )));
                    //}

                    Route route = super.crowdSimulator.getRouteFactory().createRouteFromGeometries(waypoints);
                    crowd.setRoute(route, super.crowdSimulator.getFastForwardClock().currentTimeMillis(), false);

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


                    // LOAD BOUNDARIES. Just one big polygon for now
                   /* List<Geometry> boundaries = new ArrayList<Geometry>();
                    boundaries.add(geomFac.createPolygon(new Coordinate[]{
                            new Coordinate(0,0),
                            new Coordinate(60,0),
                            new Coordinate(60,30),
                            new Coordinate(0,30),
                            new Coordinate(0,0)
                    }));
                    crowdSimulator.addBoundaries(boundaries, false);

                    // set bounding box
                    crowdSimulator.expandBoundingBox(GeometryTools.getEnvelope(boundaries));
*/

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
}
