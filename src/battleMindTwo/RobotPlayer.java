//version 2.1

package battleMindTwo;
import battlecode.common.*;
import java.util.ArrayList;

public strictfp class RobotPlayer {
    static RobotController rc;
    static ArrayList<Integer> xyCoords = new ArrayList<Integer>();
    static ArrayList<Double> passability = new ArrayList<Double>();
    static int explorerMuckrakers;
    static int moveOrder;

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
    static final Integer[] directionIndexes = {0, 1, 2, 3, 4, 5, 6, 7};


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
                System.out.println("This " + rc.getType() + " at " + rc.getLocation() + " currently has " + rc.getConviction() + " conviction. (ID #" + rc.getID() + ")");
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
        // Sets the EC coordinates and muckraker IDs to variables
        MapLocation myCoords = rc.getLocation();

        int losli = checkMostOccupiedSpawnLocationIndex() + 1;
        int muckrakerID = 0;


        if (rc.getRobotCount() < 9) {
        // First 3 bits of moveOrder are a direction from 0-7 in the directions[] array
        // the last 2 bits are SYN and ACK, respectively.

        // Gives an order to the muckraker to move in a certain direction
            if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[losli], 1)) {
                rc.buildRobot(RobotType.MUCKRAKER, directions[losli], 1);
                moveOrder = (losli << 2) + 2;
                rc.setFlag(moveOrder);
                muckrakerID = rc.senseRobotAtLocation(myCoords.add(directions[checkMostOccupiedSpawnLocationIndex()])).getID();

                while (rc.getFlag(muckrakerID) == 0) {
                    if (rc.getFlag(muckrakerID) == (moveOrder + 1)) {
                        // When the muckraker has both syn and ack flags on (last two bits are 1) set the ack flag of the EC to 1
                        moveOrder += 1;
                        rc.setFlag(moveOrder);
                        break;
                    }
                }
            }
        }


        // Code that makes the bot bid 2 influence for a vote 1/3 of the time, otherwise 1 influence
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

    }

    static void runSlanderer() throws GameActionException {

    }

    static void runMuckraker() throws GameActionException {
        int myID = rc.getID();
        int ecID = -1;
        int myFlag = rc.getFlag(myID);

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


        if (myFlag == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                }
            }
            if (rc.canGetFlag(ecID)) {
                rc.setFlag(rc.getFlag(ecID) + 1);
            }
        } else {
            tryMove(directions[(myFlag - 3) >> 2]);
        }


    }

    static int checkMostOccupiedSpawnLocationIndex() throws GameActionException {
        MapLocation myCoords = rc.getLocation();
        int highestDirectionIndex = -1;
        for (int x : directionIndexes) {
            if (rc.isLocationOccupied(myCoords.add(directions[x]))) {
                highestDirectionIndex = x;
            }
        }
        if (highestDirectionIndex == -1) {
            return -1;
        } else {
            return highestDirectionIndex;
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
        System.out.println("Attempting to move " + dir + "; Action cooldown: " + rc.getCooldownTurns());
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("Move successful");
            return true;
        } else return false;
    }
}
