package battleMindTwo;
import battlecode.common.*;
import java.util.ArrayList;



public strictfp class RobotPlayer {

    static RobotController rc;
    static int robotsSpawned = 0;
    static ArrayList<Integer> locationSeekerIDs = new ArrayList<Integer>();

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {

        MapLocation ECCords = rc.getLocation();
        if (robotsSpawned < 8) {
            if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[robotsSpawned], 1)) {
                rc.buildRobot(RobotType.MUCKRAKER, directions[robotsSpawned], 1);
                locationSeekerIDs.add(rc.senseRobotAtLocation(rc.adjacentLocation(directions[robotsSpawned])).getID());
                robotsSpawned += 1;
            }
        }

    /*   RobotType toBuild = randomSpawnableRobotType();
    *
    *    int influence = 50;
    *    for (Direction dir : directions) {
    *        if (rc.canBuildRobot(toBuild, dir, influence)) {
    *            rc.buildRobot(toBuild, dir, influence);
    *        } else {
    *            break;
    *        }
    *    }
    */
        if (Math.floor(Math.random() * 30) > 19) {
            if (rc.canBid(2)) {
                rc.bid(2);
                System.out.println("Bidding 2 influence for this round's vote...");
            }
        } else {
            if (rc.canBid(1)) {
                rc.bid(1);
                System.out.println("Bidding 1 influence for this round's vote...");
            }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        tryMove(randomDirection());
    }

    static void runSlanderer() throws GameActionException {
        tryMove(randomDirection());
    }

    static void runMuckraker() throws GameActionException {
    /*  Team enemy = rc.getTeam().opponent();
    *   int actionRadius = rc.getType().actionRadiusSquared;
    *   for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
    *       if (robot.type.canBeExposed()) {
    *           // It's a slanderer... go get them!
    *           if (rc.canExpose(robot.location)) {
    *               System.out.println("e x p o s e d");
    *               rc.expose(robot.location);
    *               return;
    *           }
    *       }
    *   }
    */
        int myID = rc.getID();
        if ((locationSeekerIDs.indexOf(myID)) != -1) {
            tryMove(directions[locationSeekerIDs.indexOf(rc.getID())]);
        }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("Attempting to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
            return true;
        } else return false;
    }
}
