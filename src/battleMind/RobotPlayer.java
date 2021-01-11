// This code borrows off Battlecode 2021's examplefuncsplayer and Jerry Mao's RobotPlayer from the pathfinding lecture.

package battleMind;
import battlecode.common.*;

class Variables {
        //int[] DefenseCoordsX = {ECCords.x - 4, ECCords.x, ECCords.x + 4, ECCords.x + 4, ECCords.x + 4, ECCords.x, ECCords.x - 4, ECCords.x - 4, ECCords.x - 2, ECCords.x + 2, ECCords.x + 2, ECCords.x - 2};
        //int[] DefenseCoordsY = {EcCords.y + 4, EcCords.y + 4, EcCords.y + 4, EcCords.y, EcCords.y - 4, EcCords.y - 4, EcCords.y - 4, EcCords.y, EcCords.y + 2, EcCords.y + 2, EcCords.y - 2, EcCords.y - 2};
        static int turnCount = 0;
        static int defenseUnits = 0;
        static int outerDefenseCoded = 0;
        static int innerDefenseCoded = 0;
        static int[] outerDefenseStep = {0, 0, 0, 0, 0, 0, 0, 0};
        static int[] innerDefenseStep = {0, 0, 0, 0};
        static int[] DefenseFlags = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        static int[] slandererPosition = {0, 1, 2, 4, 5, 6};
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

    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("Attempting to move " + dir + "; Action cooldown: " + rc.getCooldownTurns());
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("Move successful");
            return true;
        } else return false;
    }

    public static void run(RobotController rc) throws GameActionException {
        System.out.println("A " + rc.getType() + " has spawned!");
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
        //MapLocation ECCoordsX = rc.getLocation.x;
        //MapLocation ECCoordsY = rc.getLocation.y;
        MapLocation ECCords = rc.getLocation();

        //Sets up the coordinates to where the muckrakers will form around the Enlightenment Center

        int xMDefenders = {-4, -4, -4, -4, -3, -3, -2, -1, 0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4};
        int yMDefenders = {0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4, -4, -4, -4, -4, -3, -3, -2, -1};
        int mDefenderInitSpawn = 0;
        // then spawn a slanderer every 50 or so rounds depending on the passability

        if ((rc.getRoundNum()) % 50 == 0) {

            if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 950)) {

                rc.buildRobot(RobotType.SLANDERER, randomDirection(), 950);

            } else if (rc.sensePassability(ECCords) < 0.3) {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 130)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 130);
                    // Clock.yield();
                }
            } else if (rc.sensePassability(ECCords) >= 0.3 && rc.sensePassability(ECCords) < 0.7) {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 107)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 107);
                    // Clock.yield();
                }
            } else {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 85)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 85);
                    // Clock.yield();
                }
            }
        }

    /***
        while (defenseUnits < 12) {
                static Direction selectedDirection() {
                    return randomDirection();
            }

            while (rc.isLocationOccupied(rc.adjacentLocation(selectedDirection())) != true) {
                selectedDirection();
            }
    ***/

            if (Variables.defenseUnits < 8) {
                if (rc.canBuildRobot(RobotType.POLITICIAN, directions[Variables.defenseUnits], 10)) {
                    rc.buildRobot(RobotType.POLITICIAN, directions[Variables.defenseUnits], 10);
                    Variables.defenseUnits += 1;
                    return;
                }
            } else if (Variables.defenseUnits >= 8 && (Variables.defenseUnits < 12)) {
                    if (rc.canBuildRobot(RobotType.POLITICIAN, directions[(int) (Variables.defenseUnits - 8)], 20)) {
                rc.buildRobot(RobotType.POLITICIAN, directions[(int) (Variables.defenseUnits - 8)], 20);
                    Variables.defenseUnits += 1;
                }
            }
        // Code that makes the bot bid 2 influence for a vote 1/3 of the time
        if (Math.random() > (2/3)) {
            if (rc.canBid(2)) {
                rc.bid(2);
            }
        } else {
            if (rc.canBid(1)) {
                rc.bid(1);
            }
        }
        // implement else code here?
    }

    static void sendLocation(MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = x * 128 + y;
        if (rc.canSetFlag(encodedLocation)) {
            rc.setFlag(encodedLocation);
        }
    }

    static MapLocation getLocationFromFlag(int flag) {
        int x = Math.floor((flag / 128).intValue());
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
        int thisID = rc.getID();

        if (Variables.outerDefenseCoded < 8) {
            rc.setFlag(Variables.DefenseFlags[Variables.outerDefenseCoded]);
            if (Variables.outerDefenseCoded < Variables.defenseUnits) {
                Variables.outerDefenseCoded += 1;
            }

        } else if (Variables.innerDefenseCoded < 4) {
            rc.setFlag(Variables.DefenseFlags[Variables.innerDefenseCoded] + 8);
            if (Variables.innerDefenseCoded + 4 < Variables.defenseUnits) {
                Variables.innerDefenseCoded += 1;
            }
        }

        if (rc.getFlag(thisID) < 8) {
            for (int loop = 0; loop < 4; loop++) {
                if (rc.isReady() == false) {
                    Clock.yield();
                } else {
                    if (rc.canMove(directions[Variables.outerDefenseStep[rc.getFlag(thisID)]])) {
                        rc.move(directions[Variables.outerDefenseStep[rc.getFlag(thisID)]]);
                    }
                }
            }
        } else if (rc.getFlag(thisID) < 12) {
            for (int loop = 0; loop < 4; loop++) {
                if (rc.isReady() == false) {
                    Clock.yield();
                } else {
                    if (rc.canMove(directions[Variables.innerDefenseStep[rc.getFlag(thisID)]])) {
                        rc.move(directions[Variables.innerDefenseStep[(rc.getFlag(thisID) * 2 + 1)]]);
                    }
                }
            }
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
        if (rc.isReady() == false) {
            Clock.yield();
        }
    }

    static int ecID = -1;
    static MapLocation pMovementDestination = null;

    // Code for Muckrakers
    static void runCodeM() throws GameActionException {
        if (ecID == -1) {
            for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                }
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


    static final double idealPassability = 1.0;
    static Direction bugDirection = null;

    static void basicBugMovement(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)) {

        } else if (rc.isReady()) {
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= idealPassability) {
                rc.move(d);
                bugDirection = null;
            } else {
                if (bugDirection == null) {
                    bugDirection = d;
                }
                for (int i = 0; i < 8; i++) {
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= idealPassability) {
                        rc.move(bugDirection);
                        bugDirection = bugDirection.rotateRight();
                        break;
                    }
                    bugDirection = bugDirection.rotateLeft();
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
