package battleMind;
import battlecode.common.*;

public strictfp class Directions {
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

    //
    static int[] DefenseFlags = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    //int[] DefenseCoordsX = {ECCords.x - 4, ECCords.x, ECCords.x + 4, ECCords.x + 4, ECCords.x + 4, ECCords.x, ECCords.x - 4, ECCords.x - 4, ECCords.x - 2, ECCords.x + 2, ECCords.x + 2, ECCords.x - 2};
    //int[] DefenseCoordsY = {EcCords.y + 4, EcCords.y + 4, EcCords.y + 4, EcCords.y, EcCords.y - 4, EcCords.y - 4, EcCords.y - 4, EcCords.y, EcCords.y + 2, EcCords.y + 2, EcCords.y - 2, EcCords.y - 2};
    static int turnCount;
    static int defenseUnits = 0;
    static int outerDefenseCoded = 0;
    static int innerDefenseCoded = 0;
    static int[] outerDefenseStep = {0, 0, 0, 0, 0, 0, 0, 0};
    static int[] innerDefenseStep = {0, 0, 0, 0};
    public static void run(RobotController rc) throws GameActionException {

        // turnCount = 0;

        while (true) {
            // turnCount +=1;
            try {
                System.out.println("This" + rc.getType() + " at " + rc.getLocation() + "currently has " + rc.getInfluence() + " influence.");
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
        rc.getLocation();
        MapLocation ECCoords = new MapLocation(rc.getLocation);

        if (rc.getRoundNum() % 50 == 1) {
            if (rc.sensePassability(ECCoords) < 0.3) {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 150)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 150);
                    // Clock.yield();
                }
            } else if (rc.sensePassability(ECCoords) < 0.7) {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 100)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 100);
                    // Clock.yield();
                }
            } else {
                if (rc.canBuildRobot(RobotType.SLANDERER, randomDirection(), 50)) {
                    rc.buildRobot(RobotType.SLANDERER, randomDirection(), 50);
                    // Clock.yield();
                }
            }
        }
/*
        while (defenseUnits < 12) {
                static Direction selectedDirection() {
                    return randomDirection();
            }

            while (rc.isLocationOccupied(rc.adjacentLocation(selectedDirection())) != true) {
                selectedDirection();
            }
*/
            if (defenseUnits < 8) {
                if (rc.canBuildRobot(RobotType.POLITICIAN, directions[defenseUnits], 10)) {
                    rc.buildRobot(RobotType.POLITICIAN, directions[defenseUnits], 10);
                    defenseUnits += 1;

            } else if (rc.canBuildRobot(RobotType.POLITICIAN, directions[(int) (defenseUnits - 8)], 20)) {
                rc.buildRobot(RobotType.POLITICIAN, directions[(int) (defenseUnits - 8)], 20);
                defenseUnits += 1;
                }
            }

        if (rc.canBid(1)) {
            rc.bid(1);
        }
        // implement else code here?
    }
    // Code for Politicians
    static void runCodeP() throws GameActionException {

        if (outerDefenseCoded < 8) {
            rc.setFlag(DefenseFlags[outerDefenseCoded]);
            if (outerDefenseCoded < defenseUnits) {
                outerDefenseCoded += 1;
            }
        } else if (innerDefenseCoded < 4) {
            rc.setFlag(DefenseFlags[innerDefenseCoded] + 8);
            if (innerDefenseCoded + 4 < defenseUnits) {
                innerDefenseCoded += 1;
            }
        }

        if (rc.getFlag() < 8) {
            for (int loop = 0; loop < 4; loop++) {
                if (rc.isReady() == false) {
                    Clock.yield();
                } else {
                    rc.move(directions[outerDefenseStep[(int) (rc.getFlag())]]);
                }
            }
        } else if (rc.getFlag() < 12) {
            for (int loop = 0; loop < 4; loop++) {
                if (rc.isReady() == false) {
                    Clock.yield();
                } else {
                    rc.move(directions[innerDefenseStep[(int) (rc.getFlag() * 2 + 1)]]);
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
    // Code for Muckrakers
    static void runCodeM() throws GameActionException {
        if (rc.isReady() == false) {
            Clock.yield();
        }
    }

    static Direction randomDirection() {
        return directions[(Math.random() * spawnableRobot.length)];
    }
    static Boolean tryBuild(Direction dir) throws GameActionException {
        rc.detectNearbyRobots(2);
    }
    // if getTeamVotes() > 1500
}


/* Current Problems: selectedDirection and myFlag

how to define these variables in a way the code does not return errors?
Lines 67, 87/90, 127
 */
