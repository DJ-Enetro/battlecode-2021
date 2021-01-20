//Version 2.2

// NOTE! The politicians do not work as intended; for some reason they try to move to a point that's not on the map...

package battleMindTwo;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;

public strictfp class RobotPlayer {

    //For debugging purposes unless otherwise needed:
    static Clock time;

    static RobotController rc;
    static ArrayList<Integer> xyCoords = new ArrayList<>();
    static ArrayList<Double> passability = new ArrayList<>();
    static int explorerMuckrakers;
    static int moveOrder;
    static int swerveAroundCardinal = 0;
    static int swerveAroundDiagonal = 0;
    static int spawnedSlanderers = 0;

    //Arrays for and the number of each type of robot, so at the beginning of every round the enlightenment center can check to see if its bots have any new info
    static int politicianSpawned = 0;
    static int slandererSpawned = 0;
    static int muckrakerSpawned = 0;
    static ArrayList <Integer> politicianIDs = new ArrayList <Integer>();
    static ArrayList <Integer> slandererIDs = new ArrayList <Integer>();
    static ArrayList <Integer> muckrakerIDs = new ArrayList <Integer>();
    static ArrayList <MapLocation> neutralBuildingLocations = new ArrayList <MapLocation>();
    static MapLocation myCoords;
    static int myID;
    static int myFlag;

    //Integers for Politicians/Slanderers/Muckrakers
    static int ecID;

    static ArrayList <Direction> diagonals = new ArrayList <Direction>();
    static ArrayList <Direction> cardinals = new ArrayList <Direction>();

    // static Integer[] slandererX = {1, 2};
    // static Integer[] slandererY = {2, 1};

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

        diagonals.add(Direction.NORTHEAST);
        diagonals.add(Direction.SOUTHEAST);
        diagonals.add(Direction.SOUTHWEST);
        diagonals.add(Direction.NORTHWEST);

        cardinals.add(Direction.NORTH);
        cardinals.add(Direction.EAST);
        cardinals.add(Direction.SOUTH);
        cardinals.add(Direction.WEST);

        System.out.println("I'm a " + rc.getType() + " and I just got created!");

        // This gets the ID of the adjacent enlightenment center, if not an EC itself

        if ((rc.getType()) != RobotType.ENLIGHTENMENT_CENTER) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    System.out.println("The ID of the enlightenment center that created me is: " + ecID);
                    break;
                }
            }
        }

        while (true) {
            turnCount += 1;
            myCoords = rc.getLocation();

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

        int losli = (checkMostOccupiedMuckrakerSpawnLocationIndex() + 1);
        int muckrakerID = 0;

        for (Integer id : muckrakerIDs) {

            // Remove this bot's ID from the ArrayList of muckraker IDs as this bot is probably dead
            if (! rc.canGetFlag(id)) {
                muckrakerIDs.remove(id);
                continue;
            }

            // Check if the first bit of the 24 sequence is 1, meaning that it's a message for the EC that created it
            int muckrakerFlag = rc.getFlag(id);

            if (muckrakerFlag > (1 << 23)) {
                MapLocation detectedNeutralEC = getDestinationFromFlag(muckrakerFlag);
                neutralBuildingLocations.add(detectedNeutralEC);
                rc.setFlag(muckrakerFlag + 2);
                while (rc.getFlag(id) == muckrakerFlag) {
                    if (rc.getFlag(id) == (muckrakerFlag + 2)) {
                        break;
                    }
                }
            }
        }
        for (Integer id : politicianIDs) {

            // Remove this bot's ID from the ArrayList of muckraker IDs as this bot is probably dead
            if (! rc.canGetFlag(id)) {
                muckrakerIDs.remove(id);
                continue;
            }
            // Check if (flag > 2^23)?

        }

        if (losli < 8) {
            // First 3 bits of moveOrder are a direction from 0-7 in the directions[] array
            // the last 2 bits are SYN and ACK, respectively.

            // Gives an order to the muckraker to move in a certain direction
            if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[losli], 1)) {
                rc.buildRobot(RobotType.MUCKRAKER, directions[losli], 1);
                moveOrder = (losli << 2) + 2;
                rc.setFlag(moveOrder);
                muckrakerID = rc.senseRobotAtLocation(myCoords.add(directions[checkMostOccupiedMuckrakerSpawnLocationIndex()])).getID();
                muckrakerIDs.add(muckrakerID);
                muckrakerSpawned += 1;

                // Set the muckraker's command while its flag is 0
                while (rc.getFlag(muckrakerID) == 0) {
                    if (rc.getFlag(muckrakerID) == (moveOrder + 1)) {
                        // When the muckraker has both syn and ack flags on (last two bits are 1) set the ack flag of the EC to 1
                        moveOrder += 1;
                        rc.setFlag(moveOrder);
                        break;
                    } else {
                        Clock.yield();
                    }
                }
            }
        }


        int slandererID = 0;

        if (losli == 14) {
            // Tentative: this is where the slanderer that generates income stays for now
            // Also only 1 slanderer per EC

            MapLocation slandererSpot = myCoords.translate(1, 2);
            Direction selectedDirection = randomDirection();

            if (slandererSpawned != 1) {

                if (rc.canBuildRobot(spawnableRobot[1], selectedDirection, 107)) {
                    rc.buildRobot(spawnableRobot[1], selectedDirection, 107);
                    slandererID = rc.senseRobotAtLocation(myCoords.add(selectedDirection)).getID();
                    slandererIDs.add(slandererID);
                    slandererSpawned += 1;

                    int xDestination = slandererSpot.x % 128;
                    int yDestination = slandererSpot.y % 128;

                    int encodedFlag = ((128 * xDestination + yDestination) << 5) + 1;

                    rc.setFlag(encodedFlag);

                    //  While the slanderer's flag is not yet set...
                    while (rc.getFlag(slandererID) == 0) {
                        // Continually check to see if the slanderer's flag is SYN-ACK (last two bits are both 1)
                        if (rc.getFlag(slandererID) == encodedFlag + 2) {
                            // Set encodedFlag to have the ACK flag turned on and set the EC's flag to it
                            encodedFlag += 2;
                            rc.setFlag(encodedFlag);
                            break;
                        } else {
                            Clock.yield();
                        }
                    }
                }
            }
        }

        if (rc.getRoundNum() > 30) {
            for (MapLocation loc : neutralBuildingLocations) {

                // "If the flag of the EC is not 0 then that means it's not neutral, so remove it from the list"
                // But this array is of MapLocation not int id

                /* if ((rc.canGetFlag(id)) != 0) {
                *      neutralBuildingIDs.remove(loc);
                *      continue;
                *  }
                *
                */

                Direction chosenDirection = randomDirection();
                if ((rc.getRoundNum() % 15) >= 0) {
                    if (rc.canBuildRobot(spawnableRobot[0], chosenDirection, 85)) {
                        rc.buildRobot(spawnableRobot[0], chosenDirection, 85);

                        int politicianID = (rc.senseRobotAtLocation(myCoords.add(chosenDirection))).getID();

                        MapLocation destination = loc.translate(-2, -2);
                        int moveOrder = ((128 * destination.x) + (destination.y)) << 5;

                        // Send moveOrder with SYN flag
                        rc.setFlag(moveOrder + 1);

                        while (rc.getFlag(politicianID) == 0) {
                            if (rc.getFlag(politicianID) == (moveOrder + 3)) {
                                // When the politician has both syn and ack flags on (last two bits are 1) set the ack flag of the EC to 1
                                moveOrder += 3;
                                rc.setFlag(moveOrder);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // EC bids 2 influence for a vote 1/3 of the time, otherwise 1 influence

        int normalBid = (int) Math.floor(1 * Math.floor((int) (rc.getRoundNum() / 300)));
        int chanceBid = (int) Math.floor(2 * Math.floor((int) (rc.getRoundNum() / 300)));



        if (Math.floor(Math.random() * 30) > 19) {
            if (rc.canBid(chanceBid)) {
                rc.bid(chanceBid);
                System.out.println("Bidding " + chanceBid + " influence for this round's vote...");
            }
        } else {
            if (rc.canBid(normalBid)) {
                rc.bid(normalBid);
                System.out.println("Bidding " + normalBid + " influence for this round's vote...");
            }
        }
    }

    static void runPolitician() throws GameActionException {
        // Tentative: politicians only spawn to take control of neutral ECs
        MapLocation myCoords = rc.getLocation();
        // Newly created bots will always have a flag of 0
        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        MapLocation destination = getDestinationFromFlag(myFlag);
        RobotInfo[] surroundings = rc.senseNearbyRobots();
        int alliedPoliticians = 0;

        if (myFlag == 0) {

            // The SYN-ACK part of the transition
            if (rc.canGetFlag(ecID)) {
                // Set this politician's own flag to SYN-ACK
                rc.setFlag(rc.getFlag(ecID) + 2);
            }
        // If the robot can't move, if it's not on the edge, if there's a robot in its way...
        } else {
            /*
            *if ((myCoords.directionTo(destination)) == Direction.CENTER) {
            *    if (rc.canEmpower(5)) {
            *        rc.empower(5);
            *        System.out.println("empowered!");
            *    }
            *}
            *
            *
            *for (RobotInfo info : surroundings) {
            *    if ((info.getType() == spawnableRobot[0]) && (info.getTeam() == rc.getTeam())) {
            *        alliedPoliticians += 1;
            *    }
            *}
            */
            // This prevents politician crowding; a huge mass of politicians can be wiped out with a chain reaction
            // from only one

            if (rc.canSenseLocation(destination)) {
                if (myCoords.distanceSquaredTo(destination) <= 5) {
                    int empowerRadius = myCoords.distanceSquaredTo(destination);
                    if (rc.canEmpower(empowerRadius)) {
                        rc.empower(empowerRadius);
                        System.out.println("empowered!");
                    }
                } else {
                    tryMove(myCoords.directionTo(destination));
                }
            } else {
                tryMove(myCoords.directionTo(destination));
                System.out.println("Destination: " + getDestinationFromFlag(myFlag));
            }
            if (alliedPoliticians >= 3) {
                for (Direction dir : directions) {
                    tryMove(dir);
                }
            }

        }


        // If you can't move in the direction you planned to...
        if (! (rc.canMove(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
            // If this robot is not on the map's edge...
            if (rc.onTheMap(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                //
                if (rc.canSenseLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                    if (!(rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))) == null)) {
                        Team detected = rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))).getTeam();
                        System.out.println("Obstacle of " + detected + " detected!");
                        if (cardinals.contains(myCoords.directionTo(getDestinationFromFlag(myFlag)))) {
                            swerveAroundCardinal();
                        } else {
                            swerveAroundDiagonal();
                        }
                    }
                }
            }
        }
    }

    static void runSlanderer() throws GameActionException {
        MapLocation myCoords = rc.getLocation();
        //Newly created bots will always have a flag of 0
        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        RobotInfo[] surroundings = rc.senseNearbyRobots();

        if (myFlag == 0) {

            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    // This gets the ID of the adjacent enlightenment center
                    break;
                }
            }
            // The SYN-ACK part of the transition
            if (rc.canGetFlag(ecID)) {
                //Set this slanderer's flag to the EC's flag + the ACK bit
                rc.setFlag(rc.getFlag(ecID) + 2);
            }
        // If the robot can't move, if it's not on the edge, if there's a robot in its way...
        } else if (! (rc.canMove(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
            if (rc.onTheMap(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                if (rc.canSenseLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                    if (! (rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))) == null)) {
                        Team detected = rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))).getTeam();
                        System.out.println("Obstacle of " + detected + " detected!");
                        if (cardinals.contains(myCoords.directionTo(getDestinationFromFlag(myFlag)))) {
                            swerveAroundCardinal();
                        } else {
                            swerveAroundDiagonal();
                        }
                    }
                }
            }
        } else {
            tryMove(myCoords.directionTo(getDestinationFromFlag(myFlag)));
            System.out.println("Destination: " + getDestinationFromFlag(myFlag));
        }
    }

    static void runMuckraker() throws GameActionException {

        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        MapLocation myCoords = rc.getLocation();
        RobotInfo[] surroundings = rc.senseNearbyRobots();

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



        for (RobotInfo info : surroundings) {
            // Since the only neutral buildings are neutral ECs...

            if (info.getTeam() == Team.NEUTRAL) {
                System.out.println("Neutral EC detected!");

                MapLocation location = info.getLocation();
                int oldFlag = rc.getFlag(myID);
                int x = location.x % 128;
                int y = location.y % 128;
                //Stores the old flag in an integer while a new flag is created to send back to the EC
                int transmittedData = ((1 << 23) + (((x << 7) + y) << 5) + 1);
                rc.setFlag(transmittedData);
                while ((rc.getFlag(myID)) == transmittedData) {
                    if (rc.getFlag(ecID) == (transmittedData + 2)) {
                        rc.setFlag(ecID);
                        break;
                    }
                }
                rc.setFlag(oldFlag);
            }
        }


        // Newly created muckrakers will have a flag of 0
        if (myFlag == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    // This gets the ID of the adjacent enlightenment center
                    break;
                }
            }
            // The SYN-ACK part of the transition
            if (rc.canGetFlag(ecID)) {
                rc.setFlag(rc.getFlag(ecID) + 1);
            }
        } else if (! (rc.canMove(directions[(myFlag - 3) >> 2]))) {
            if (rc.onTheMap(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                if (rc.canSenseLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                    if (!(rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))) == null)) {
                        Team detected = rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))).getTeam();
                        System.out.println("Obstacle of " + detected + " detected!");
                        if (((myFlag - 3) >> 2) % 2 == 0) {
                            swerveAroundCardinal();
                        } else {
                            swerveAroundDiagonal();
                        }
                    }
                }
            }
        } else {
            tryMove(myCoords.directionTo(getDestinationFromFlag(myFlag)));
            System.out.println("Destination: " + getDestinationFromFlag(myFlag));
        }
        Clock.yield();

    }

    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;


    /* This method is a bit of a misnomer; while this is supposed to get the location of coordinates a bot is moving to,
    *  it also allows for if a robot is moving in one specific direction or if a robot found something at
    *  a specific location
    */

    static MapLocation getDestinationFromFlag(int flag) {

        // Going in one direction
        if (flag < 32) {
            MapLocation myCoords = rc.getLocation();
            return myCoords.add(directions[(flag >>> 2)]);

        // Transmitting coordinates of another point to the EC
        } else if (flag > (1 << 23)) {

            int y = (flag >> 5) & BITMASK;
            int x = (flag >> 12) & BITMASK;

            MapLocation currentLocation = rc.getLocation();
            int offsetX128 = currentLocation.x >> NBITS;
            int offsetY128 = currentLocation.y >> NBITS;
            MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);

            // You can probably code this in a neater way, but it works
            MapLocation alternative = actualLocation.translate(-(1 << NBITS), 0);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(1 << NBITS, 0);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(0, -(1 << NBITS));
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(0, 1 << NBITS);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            return actualLocation;


        } else {

            // If a robot is heading to a specific coordinate point

            int y = (flag >> 5) & BITMASK;
            int x = (flag >> 12) & BITMASK;
            // int extraInformation = flag >> (2*NBITS);

            MapLocation currentLocation = rc.getLocation();
            int offsetX128 = currentLocation.x >> NBITS;
            int offsetY128 = currentLocation.y >> NBITS;
            MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);

            // You can probably code this in a neater way, but it works
            MapLocation alternative = actualLocation.translate(-(1 << NBITS), 0);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(1 << NBITS, 0);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(0, -(1 << NBITS));
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            alternative = actualLocation.translate(0, 1 << NBITS);
            if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
                actualLocation = alternative;
            }
            return actualLocation;
        }
    }

    static int checkMostOccupiedMuckrakerSpawnLocationIndex() throws GameActionException {
        MapLocation myCoords = rc.getLocation();
        int highestDirectionIndex = -1;
        for (int x : directionIndexes) {
            if (rc.isLocationOccupied(myCoords.add(directions[x]))) {
                highestDirectionIndex = x;
            }
        }

        if (rc.getRoundNum() > (int) (Math.floor(7 * (2 / rc.sensePassability(myCoords) + 1)) + 1)) {
            return 13;
        } else if (highestDirectionIndex == -1) {
            return -1;
        } else {
            return highestDirectionIndex;
        }
    }

    static void swerveAroundCardinal() throws GameActionException {
        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        int step = 1;
        int watchdogTimer = 0;

        MapLocation myCoords = rc.getLocation();

        Direction currentDirection = myCoords.directionTo(getDestinationFromFlag(myFlag));

        while (step != 0) {
            while (step == 1) {
                if (! rc.canMove((currentDirection).rotateRight())) {
                    watchdogTimer += 1;
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir));
                        watchdogTimer = 0;
                        System.out.println("Timed out, moving " + dir);
                        break;
                    }
                } else {
                    tryMove((currentDirection).rotateRight());
                    step += 1;
                }
            }
            while (step == 2) {
                if (! rc.canMove((currentDirection).rotateLeft())) {
                    watchdogTimer += 1;
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir));
                        watchdogTimer = 0;
                        System.out.println("Timed out, moving " + dir);
                        break;
                    }
                } else {
                    tryMove((currentDirection).rotateLeft());
                    step = 0;
                    Clock.yield();
                }
            }
        }
    }

    static void swerveAroundDiagonal() throws GameActionException {
        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        int step = 1;
        MapLocation myCoords = rc.getLocation();
        Direction currentDirection = myCoords.directionTo(getDestinationFromFlag(myFlag));
        int watchdogTimer = 0;

        while (step != 0) {
            while (step == 1) {
                if (!rc.canMove((currentDirection).rotateRight())) {
                    watchdogTimer += 1;
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir));
                        watchdogTimer = 0;
                        System.out.println("Timed out, moving " + dir);
                        break;
                    }
                } else {
                    tryMove((currentDirection).rotateRight());
                    step += 1;
                }
            }

            while (step == 2) {
                if (!rc.canMove(currentDirection)) {
                    watchdogTimer += 1;
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir));
                        watchdogTimer = 0;
                        System.out.println("Timed out, moving " + dir);
                        break;
                    }
                } else {
                    tryMove(currentDirection);
                    step += 1;
                }
            }

            while (step == 3) {
                if (!rc.canMove((currentDirection).rotateLeft())) {
                    watchdogTimer += 1;
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir)) {
                            watchdogTimer = 0;
                            System.out.println("Timed out, moving " + dir);
                            break;
                        }
                    }
                } else {
                    tryMove((currentDirection).rotateLeft());
                    step = 0;
                    Clock.yield();
                }
            }
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
