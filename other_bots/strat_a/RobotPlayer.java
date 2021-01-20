package stratA;
import battlecode.common.*;

// Code adapted from ./examplefuncsplayer/RobotPlayer.
// This is Strategy A of team Sip of Java Spice


public strictfp class RobotPlayer {
    static RobotController rc;

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
    static boolean enemySpotted;
    static final int wave1threshold = 1;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;

        turnCount = 0;
        enemySpotted = false;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            try {
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        // PHASE 1: Send out politicians to scout the location of the enemy
        // Default set to move 1, we can change wave1threshold variable to optimize
        if (turnCount == wave1threshold) {
            int influence = 1;
            for (Direction dir: directions) {
                if (rc.canBuildRobot(RobotType.POLITICIAN, dir, influence)) {
                    rc.buildRobot(RobotType.POLITICIAN, dir, influence);
                }
            }
        }
        // PHASE 2: Spawn muckrakers + some politicians (use random variable)
        // PHASE 2.5: Spawn slanderers when it's safe to do so/when base influence is low
        // We need to figure out how to spawn the muckrakers so they get absorbed into the "wall"
        // The tricky part about expanding the wall is that everyone needs to communicate with each other
        else {
            if (canSpawnSlanderer()) {
                int influence = setSlandererInfluence();
                Direction dir = idealSpawningDirection(RobotType.SLANDERER);
                if (rc.canBuildRobot(RobotType.SLANDERER, dir, influence)) {
                    rc.buildRobot(RobotType.SLANDERER, dir, influence);
                }
            }

            // TODO: figure out how to spawn muckrakers in an organized fashion
        }
    }

    static boolean canSpawnSlanderer() {
        //TODO: determine whether its safe to spawn slanderers
        return false;
    }

    static Direction idealSpawningDirection(RobotType r) {
        //TODO: determine the best direction to spawn any given type of bot given game situation
        return Direction.NORTH;
    }

    static int setSlandererInfluence() {
        //TODO: determine how high to set slanderer influence given game situation
        return 1;
    }

    static void runPolitician() throws GameActionException {
        // very similar to previous code. difference being that
        // politician is in charge of setting the enemySpotted variable
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            enemySpotted=true;
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
    }

    static void runSlanderer() throws GameActionException {
        //TODO: figure out slanderer moving scheme - if we add a slanderer,
        //TODO: how do we move the cluster of slanderers?
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        //TODO: FIGURE OUT MOVING SCHEME
    }
}