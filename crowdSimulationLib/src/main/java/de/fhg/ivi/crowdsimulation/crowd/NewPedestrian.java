package de.fhg.ivi.crowdsimulation.crowd;

import com.vividsolutions.jts.math.Vector2D;
import de.fhg.ivi.crowdsimulation.CrowdSimulator;
import de.fhg.ivi.crowdsimulation.crowd.Pedestrian;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.ForceModel;
import de.fhg.ivi.crowdsimulation.crowd.forcemodel.numericintegration.NumericIntegrator;
import de.fhg.ivi.crowdsimulation.geom.Quadtree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class NewPedestrian extends Pedestrian {

    // A unique ID for each pedestrian
    private static int UniqueID = 0;

    // A lock that makes sure agents aren't added and removed at the same time
    public static final Lock agentLock = new ReentrantLock();

    // We need to when the first agent reaches the end of the corridor so that CrowdSimRunnerMain
    // can stop creating agents
    public static boolean reachedEndOfCorridor = false;

    private static final double XLIM = 110; // the point at which pedestrians leave the simulation
    private static final int HEIGHT = 30;
    // (should be part of CrowdSimRUnnerMain but caused a circular dependency)

    public static BufferedWriter bw = null;

    // Useful to have a link back to the crowd simulator and crowd that this pedestrian belongs to
    private CrowdSimulator crowdSimulator;
    private Crowd crowd;

    private int numTimesThroughCorridor = 0;

    public NewPedestrian(double initialPositionX, double initialPositionY, float normalDesiredVelocity,
                         float maximumDesiredVelocity, ForceModel forceModel, NumericIntegrator numericIntegrator, Quadtree quadtree,
                         CrowdSimulator crowdSimulator, Crowd crowd)
    {
        super(NewPedestrian.getUniqueID(), initialPositionX, initialPositionY, normalDesiredVelocity, maximumDesiredVelocity,
                forceModel, numericIntegrator, quadtree);
        this.crowdSimulator = crowdSimulator;
        this.crowd = crowd;

        // Initialise the file writer
        if (NewPedestrian.bw == null) {
            try {
                NewPedestrian.bw = new BufferedWriter(new FileWriter(
                        "./results/HPP-cone-6ped-withspeed-"+System.currentTimeMillis()+".csv"));

                //bw.write("Time,Agent,Xpos,RelativeDelay,Velocity,Xforce,MaximumDesiredVelocity\n"); // Any others
                bw.write("Time,Agent,Xpos,Ypos,TotalForce,CurrentSpeed,MaxSpeed,NumThroughCorridor\n"); // Any others
            }
            catch (java.io.FileNotFoundException e) {
                System.err.println("FileNoteFoundException in NewPedestrian. Have you created a 'results' directory?");
                e.printStackTrace();
                System.exit(1);
            }
            catch (IOException e) {
                System.err.println("Error at NewPedestrian: "+this.getId());
                e.printStackTrace();
                System.exit(1);
            }
        }
        //System.out.println("Creating new pedestrian: "+this.getId());
    }

    /**
     * This moves the agent. Overridden from {@link Pedestrian) parent class so
     * that bespoke code can be called at every iteration.
     *
     * @param time the current time (simulated) time, given in milliseconds
     * @param simulationInterval the time between this method invocation and the last one, given in
     */
    @Override
    public void move(long time, double simulationInterval) {
        super.move(time,simulationInterval);


        // Add this pedestrian's data to the output file
        try {
            //NewPedestrian.bw.write(String.format("%s,%s,%.4f,%.4f,%.4f,%.4f,%.4f,%d\n",
            NewPedestrian.bw.write(String.format("%s,%s,%.4f,%.4f,%.4f,%.4f,%.4f,%d\n",
                    this.crowdSimulator.getSimulatedTimeSpan(),
                    this.getId(),
                    this.getCurrentPositionVector().getX(),
                    this.getCurrentPositionVector().getY(),
                    //Math.abs(this.getTotalForce().getX()),
                    //Math.abs(this.getTotalForce().getY()),
                    Math.sqrt(Math.pow(this.getForceInteractionWithPedestrians().getX(),2)+Math.pow(this.getForceInteractionWithPedestrians().getY(),2)),
                    //this.getForceInteractionWithPedestrians().getX(),
                    Math.abs(this.getCurrentVelocity()),
                    //this.getTotalForce().getX(),
                    //this.getCurrentVelocity(),
                    Math.abs(this.getCurrentNormalDesiredVelocity()),
                    this.numTimesThroughCorridor
                    ));
        } catch (IOException e) {
            System.out.println("Error at Move: "+this.getId());
            e.printStackTrace();
            System.exit(1);
        }

        // See if the agent has reached the end of the corridor; if so then move them back to the beginning
        /*
        if (this.getCurrentPositionVector().getX() > XLIM) {
            //this.setCurrentPositionVector(new Vector2D(0, this.getCurrentPositionCoordinate().y));
            double a = 1; // min value we want to scale to
            double b = HEIGHT-1; // max value we want to scale to
            //double y =  (b-a) * pos + a;
            double y = (b-a) * Math.random() +a;
            double x = 10 * Math.random();
            this.setCurrentPositionVector(new Vector2D(x, y));
            NewPedestrian.reachedEndOfCorridor = true;
            this.numTimesThroughCorridor += 1;
        }
        */


        // See if this agent needs to be removed. Remove them by removing their
        // group (they are the only member of their group) from the constituent crowd.
        if (this.getCurrentPositionVector().getX() > XLIM) {
            //System.out.println("Agent "+this.getId()+" needs to exit");
            agentLock.lock(); // Prevents other threads removing or adding agents at the same time
            Group groupToRemove = null;
            for (Group group : this.crowd.getGroups())
            {
                if (group.getPedestrians(false).contains(this)) {
                    groupToRemove = group;
                    break;
                }
            }
            if (groupToRemove==null) {
                System.err.println("Error in NewPedestrian.move(): groupToRemove is null so can't remove agent for some reason.S");
            }
            this.crowd.getGroups().remove(groupToRemove);
            agentLock.unlock();
        }

    }

    private static synchronized int getUniqueID() {
        return NewPedestrian.UniqueID++;
    }
}
