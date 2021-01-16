// This code borrows off Battlecode 2021's examplefuncsplayer and Jerry Mao's RobotPlayer from the pathfinding lecture.

package battleMind;
import battlecode.common.*;
import java.util.ArrayList;

class Variables {
        //int[] DefenseCoordsX = {ECCords.x - 4, ECCords.x, ECCords.x + 4, ECCords.x + 4, ECCords.x + 4, ECCords.x, ECCords.x - 4, ECCords.x - 4, ECCords.x - 2, ECCords.x + 2, ECCords.x + 2, ECCords.x - 2};
        //int[] DefenseCoordsY = {EcCords.y + 4, EcCords.y + 4, EcCords.y + 4, EcCords.y, EcCords.y - 4, EcCords.y - 4, EcCords.y - 4, EcCords.y, EcCords.y + 2, EcCords.y + 2, EcCords.y - 2, EcCords.y - 2};
        static int turnCount = 0;
        static int outerDefenseCoded = 0;
        static int innerDefenseCoded = 0;
        // static int[] outerDefenseStep = {0, 0, 0, 0, 0, 0, 0, 0};
        // static int[] innerDefenseStep = {0, 0, 0, 0};
        // static int[] DefenseFlags = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        static int[] slandererPosition = {0, 1, 2, 4, 5, 6};
        static int mDefenderInitSpawn = 0;
        static int pDefenseUnits = 0;

        ArrayList<RobotType> types = new ArrayList<RobotType>();
        ArrayList<MapLocation> locations = new ArrayList<MapLocation>();
        ArrayList<MapLocation> destination = new ArrayList<MapLocation>();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        // static boolean[] slandererOccupied = {false, false, false, false, false, false}
}


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


    public static void run(RobotController rc) throws GameActionException {
        // turnCount = 0;
        RobotPlayer.rc = rc;
        while (true) {
            // turnCount +=1;
            try {
                System.out.println("This " + rc.getType() + " at " + rc.getLocation() + " currently has " + rc.getConviction() + " conviction. (ID #" + rc.getID() + ")");
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        runCodeEC();
                        break;
                    case POLITICIAN:
                        runCodeP();
                        break;
                    case SLANDERER:
                        runCodeS();
                        break;
                    case MUCKRAKER:
                        runCodeM();
                        break;
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Code for Enlightenment Centers
    static void runCodeEC() throws GameActionException {
        // int influence = 20;
        // getRobotCount();

        // Get the coordinates of the Enlightenment Center, get that coordinates' passability

        MapLocation ECCords = rc.getLocation();

        //Sets up the coordinates to where the muckrakers will form around the Enlightenment Center

        int[] xMDefenders = {-4, -4, -4, -4, -3, -3, -2, -1, 0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4};
        int[] yMDefenders = {0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4, -4, -4, -4, -4, -3, -3, -2, -1};
        int[] xPDefenders = {-8, 0, 8, 8, 8, 0, -8, -8};
        int[] yPDefenders = {8, 8, 8, 0, -8, -8, -8, 0};

        boolean sBuilt = true;
        // then spawn a slanderer every 50 or so rounds depending on the passability
        Direction selectedDirection = randomDirection();
        int spawnRobot = (int) Math.floor((rc.getRoundNum() - 1) / rc.sensePassability(ECCords));

// Spawning initial slanderer

        if ((rc.getRoundNum()) == 1) {
            if (rc.sensePassability(ECCords) < 0.3) {
                if (rc.canBuildRobot(RobotType.SLANDERER, selectedDirection, 130)) {
                    rc.buildRobot(RobotType.SLANDERER, selectedDirection, 130);
                    System.out.println("A slanderer has spawned!");
                    // Clock.yield();
                }
            } else if (rc.sensePassability(ECCords) >= 0.3 && rc.sensePassability(ECCords) < 0.7) {
                if (rc.canBuildRobot(RobotType.SLANDERER, selectedDirection, 107)) {
                    rc.buildRobot(RobotType.SLANDERER, selectedDirection, 107);
                    System.out.println("A slanderer has spawned!");
                    // Clock.yield();
                }
            } else {
                if (rc.canBuildRobot(RobotType.SLANDERER, selectedDirection, 85)) {
                    rc.buildRobot(RobotType.SLANDERER, selectedDirection, 85);
                    System.out.println("A slanderer has spawned!");
                    // Clock.yield();
                }
            }
        }

        // Spawns a new slanderer every 50-60 rounds depending on passability

        if ((rc.canBuildRobot(RobotType.MUCKRAKER, selectedDirection, 1)) && (spawnRobot % 15 == 1)) {
            sBuilt = false;

            if (rc.canBuildRobot(RobotType.SLANDERER, selectedDirection, 950)) {
                rc.buildRobot(RobotType.SLANDERER, selectedDirection, 950);
                System.out.println("A slanderer has spawned!");
            } else {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.SLANDERER, dir, 950)) {
                        rc.buildRobot(RobotType.SLANDERER, dir, 950);
                        System.out.println("A slanderer has spawned!");
                        sBuilt = true;
                        break;
                    }
                }
            }
        }



        if ((Variables.pDefenseUnits < 8) && (spawnRobot % 3 == 1)) {
            MapLocation xyPDefenders = new MapLocation(xPDefenders[Variables.pDefenseUnits] + ECCords.x, yPDefenders[Variables.pDefenseUnits] + ECCords.y);

            if (rc.canBuildRobot(RobotType.POLITICIAN, randomDirection(), 10)) {
                rc.buildRobot(RobotType.POLITICIAN, randomDirection(), 10);
                System.out.println("A politician has spawned!");
                sendLocation(xyPDefenders);
                Variables.pDefenseUnits += 1;

            } else {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 10)) {
                        rc.buildRobot(RobotType.POLITICIAN, dir, 10);
                        System.out.println("A politician has spawned!");
                        sendLocation(xyPDefenders);
                        Variables.pDefenseUnits += 1;
                        break;
                    }
                }
            }
        }

        if ((spawnRobot % 4 == 0) && (Variables.mDefenderInitSpawn < 32)) {
            MapLocation xyMDefenders = new MapLocation(xMDefenders[Variables.mDefenderInitSpawn] + ECCords.x, yMDefenders[Variables.mDefenderInitSpawn] + ECCords.y);

            if (rc.canBuildRobot(RobotType.MUCKRAKER, selectedDirection, 1)) {

                rc.buildRobot(RobotType.MUCKRAKER, selectedDirection, 1);
                System.out.println("A muckraker has spawned!");
                sendLocation(xyMDefenders);
                Variables.mDefenderInitSpawn += 1;

            } else {

                for (Direction dir : directions) {

                    if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1)) {

                        rc.buildRobot(RobotType.MUCKRAKER, dir, 1);
                        System.out.println("A muckraker has spawned!");
                        sendLocation(xyMDefenders);
                        Variables.mDefenderInitSpawn += 1;
                        break;

                    }
                }
            }
        }

        // Code that makes the bot bid 2 influence for a vote 1/3 of the time
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
        // implement else code here?
    }


    static void sendLocation(MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = x * 128 + y;
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
            Clock.yield();
        }
    }

    static MapLocation getLocationFromFlag(int flag) {
        int x = (int) Math.floor((flag / 128));
        int y = flag % 128;

        MapLocation actualLocation = new MapLocation (x, y);
        MapLocation alternative = actualLocation.translate(-64, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(64, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, 64);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, -64);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        return actualLocation;
    }


    // Code for Politicians
    static void runCodeP() throws GameActionException {
        int thisID = -1;
        int ecID = -1;
        MapLocation ECCords = null;
        int roundOfCreation = -1;
        MapLocation pMovementDestination = null;
        MapLocation here = null;

        if (here == null) {
            here = rc.getLocation();
            System.out.println("Location set!");
        }

        // This code accounts for the initial action cooldown of 10
        int initialDelay = 10;
        if (initialDelay >= 1) {
            initialDelay -= 1;
        }

        if (ecID == -1) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    ECCords = robot.location;
                }
            }
        } else {
            if (ECCords.isAdjacentTo(ECCords)) {
                tryMove((here.directionTo(ECCords)).opposite());
            }
        }

        if (roundOfCreation == -1) {
            roundOfCreation = rc.getRoundNum();
        }

        if (rc.canGetFlag(ecID)) {
            pMovementDestination = getLocationFromFlag(rc.getFlag(ecID));
        }



        if (pMovementDestination != null) {
            basicBugMovement(pMovementDestination);
        }


        /*
        myFlag = this.getFlag();
        if (rc.getFlag(getID()) < 9) {
            while (outerDefenseStep[myFlag] < 4) {
                rc.move(Direction[outerDefenseStep[myFlag]]);
                outerDefenseStep[myFlag] += 1;
            }
        } else if (rc.getFlag(getID()) < 12) {
            while (innerDefenseStep[myFlag] < 2) {
                rc.move(Direction[(innerDefenseStep[myFlag] * 2 + 1)]);
                innerDefenseStep[rc.getFlag()] += 1;
            }
        }
        */
    }

    // Code for Slanderers
    static void runCodeS() throws GameActionException {
        MapLocation ECCords = null;
        int ecID = -1;
        MapLocation pMovementDestination = null;
        MapLocation here = null;

        if (here == null) {
            here = rc.getLocation();
        }

        if (ecID == -1) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    ECCords = robot.location;
                }
            }
        } else {

            if (ECCords.isAdjacentTo(ECCords)) {
                tryMove((here.directionTo(ECCords)).opposite());
            }
        }


        if (rc.canGetFlag(ecID)) {
            pMovementDestination = getLocationFromFlag(rc.getFlag(ecID));
        }
        if (pMovementDestination != null) {
            basicBugMovement(pMovementDestination);
        }
    }


    // Code for Muckrakers
    static void runCodeM() throws GameActionException {
        int initialDelay = 10;
        if (initialDelay >= 1) {
            initialDelay -= 1;
            Clock.yield();
        }

        int ecID = -1;
        MapLocation pMovementDestination = null;
        MapLocation ECCords = null;
        MapLocation here = null;

        if (here == null) {
            here = rc.getLocation();
        }

        if (ecID == -1) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    ECCords = robot.location;
                }
            }
        } else {
            // If adjacent to an Enlightenment center, will move one space from it to make room for more robots to spawn
            if (here.isAdjacentTo(ECCords)) {
                tryMove((here.directionTo(ECCords)).opposite());
            }
        }


        if (rc.canGetFlag(ecID)) {
            pMovementDestination = getLocationFromFlag(rc.getFlag(ecID));
        }
        if (pMovementDestination != null) {
            basicBugMovement(pMovementDestination);
        }
        if (rc.isReady() == false) {
            Clock.yield();
        }
    }

// Pathfinding Implementation in progress...

    static boolean tryMove(Direction dir) throws GameActionException {
       System.out.println("Attempting to move " + dir + "; Action cooldown: " + rc.getCooldownTurns());
       if (rc.canMove(dir)) {
           rc.move(dir);
           System.out.println("Move successful");
           return true;
       } else return false;
   }



    static final double idealPassability = 1.0;
    static Direction bugDirection = null;

    static void basicBugMovement(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)) {

        } else if (rc.isReady()) {
            if (rc.sensePassability(rc.getLocation().add(d)) >= idealPassability) {
                System.out.println("Attempting to move " + d + "; Action cooldown: " + rc.getCooldownTurns());

                if (rc.canMove(d)) {

                    rc.move(d);
                    System.out.println("Move successful");
                }

                bugDirection = null;
            } else {
                if (bugDirection == null) {
                    bugDirection = d;
                }
                for (int i = 0; i < 8; i++) {
                    if (rc.sensePassability(rc.getLocation().add(bugDirection)) >= idealPassability) {
                        System.out.println("Attempting to move " + bugDirection + "; Action cooldown: " + rc.getCooldownTurns());
                        if (rc.canMove(bugDirection)) {
                            rc.move(bugDirection);
                            System.out.println("Move successful");
                            bugDirection = bugDirection.rotateRight();
                            break;
                        }
                        bugDirection = bugDirection.rotateLeft();
                    }
                }
            }
        }
    }
    static Direction randomDirection() {
        return directions[(int) (Math.random() * spawnableRobot.length)];
    }
    /* static Boolean tryBuild(Direction dir) throws GameActionException {
        rc.detectNearbyRobots(2);
    }

     */
    // if getTeamVotes() > 1500
}


/* Current Problems:
 */
