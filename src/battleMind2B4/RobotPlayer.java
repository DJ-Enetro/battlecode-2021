//Version 2.2

// NOTE! The politicians do not work as intended; for some reason they try to move to a point that's not on the map...

package battleMind2B4;
import battlecode.common.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public strictfp class RobotPlayer {

    //For debugging purposes unless otherwise needed:
    static Clock time;

    static RobotController rc;
    static ArrayList<Integer> xyCoords = new ArrayList<>();
    static ArrayList<Double> passability = new ArrayList<>();
    static int explorerMuckrakers;
    static int moveOrder;
    static int spawnedSlanderers = 0;
    static int muckrakerRing = 0;
    static int slandererRound;
    static int numberOfAttacks;

    static Team enemy;
    static boolean useBugDirection;
    static boolean waitForCommand;

    //Arrays for and the number of each type of robot, so at the beginning of every round the enlightenment center can check to see if its bots have any new info
    static int politicianSpawned = 0;
    static int slandererSpawned = 0;

    static ArrayList <Integer> politicianIDs = new ArrayList <>();
    static ArrayList <Integer> slandererIDs = new ArrayList <>();
    static ArrayList <Integer> muckrakerIDs = new ArrayList <>();
    static ArrayList <MapLocation> neutralBuildingLocations = new ArrayList <>();
    static ArrayList <MapLocation> enemyBuildingLocations = new ArrayList<>();
    static MapLocation myCoords;
    static int myID;
    static int myFlag;
    static ArrayList <Direction> spawnableDirections = new ArrayList<>();
    static ArrayList <Direction> occupiedDirections = new ArrayList<>();


    //Integers for Politicians/Slanderers/Muckrakers
    static int ecID;

    static ArrayList <Direction> diagonals = new ArrayList <>();
    static ArrayList <Direction> cardinals = new ArrayList <>();
    static ArrayList <Direction> directionArray = new ArrayList <>();

    static Integer[] slandererX = {2, 4, 4, 2, -2, -4, -4, -2,};
    static Integer[] slandererY = {4, 2, -2, -4, -4, -2, 2, 4,};

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
        slandererRound = 0;

        diagonals.add(Direction.NORTHEAST);
        diagonals.add(Direction.SOUTHEAST);
        diagonals.add(Direction.SOUTHWEST);
        diagonals.add(Direction.NORTHWEST);

        cardinals.add(Direction.NORTH);
        cardinals.add(Direction.EAST);
        cardinals.add(Direction.SOUTH);
        cardinals.add(Direction.WEST);
        directionArray.add(Direction.NORTH);
        directionArray.add(Direction.NORTHEAST);
        directionArray.add(Direction.EAST);
        directionArray.add(Direction.SOUTHEAST);
        directionArray.add(Direction.SOUTH);
        directionArray.add(Direction.SOUTHWEST);
        directionArray.add(Direction.WEST);
        directionArray.add(Direction.NORTHWEST);

        enemy = rc.getTeam().opponent();

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        myID = rc.getID();

        // This gets the ID of the adjacent enlightenment center, if not an EC itself

        if ((rc.getType()) != RobotType.ENLIGHTENMENT_CENTER) {
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    ecID = robot.ID;
                    System.out.println("The ID of the enlightenment center that created me is: " + ecID);
                    break;
                }
            }
            if (Math.random() <= 0.5) {
                useBugDirection = false;
            } else {
                useBugDirection = true;
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
                    case ENLIGHTENMENT_CENTER:
                        runEnlightenmentCenter();
                        break;
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        runSlanderer();
                        break;
                    case MUCKRAKER:
                        runMuckraker();
                        break;
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

        // int losli = (checkMostOccupiedMuckrakerSpawnLocationIndex() + 1);
        int muckrakerID = 0;
        int phase = (int) (Math.floor((rc.getRoundNum() / 300))) + 1;

        // This if statement below gets the squares adjacent to the enlightenment center that are on the map
        // so the bot will not return an error if the EC is on the map's edge/corner

        if (turnCount == 1) {
            for (Direction dir : directions) {
                if (rc.onTheMap(myCoords.add(dir))) {
                    spawnableDirections.add(dir);
                }
            }
        }


        if (rc.getRoundNum() % 200 == 0) {
            muckrakerRing = 0;
            slandererRound += 1;
        }

        if (rc.getRoundNum() % 300 == 0) {
            slandererSpawned = 0;
        }

        if (slandererSpawned != 8) {
            MapLocation slandererSpot = myCoords.translate(slandererX[slandererSpawned], slandererY[slandererSpawned]);
            Direction selectedDirection = randomDirection();
            int slandererID = 0;

            if (! rc.onTheMap(slandererSpot)) {
                slandererSpawned += 1;
            } else if (rc.canBuildRobot(spawnableRobot[1], selectedDirection, 107)) {
                rc.buildRobot(spawnableRobot[1], selectedDirection, 107);
                slandererID = rc.senseRobotAtLocation(myCoords.add(selectedDirection)).getID();
                slandererIDs.add(slandererID);
                slandererSpawned += 1;

                int xDestination = slandererSpot.x % 128;
                int yDestination = slandererSpot.y % 128;

                int encodedFlag = ((128 * xDestination + yDestination) << 5) + 1;

                rc.setFlag(encodedFlag);

                //  While the slanderer's flag is not yet set...
                waitForCommand = false;
                while (rc.getFlag(slandererID) == 0) {
                    // Continually check to see if the slanderer's flag is SYN-ACK (last two bits are both 1)
                    if (rc.getFlag(slandererID) == encodedFlag + 2) {
                        // Set encodedFlag to have the ACK flag turned on and set the EC's flag to it
                        encodedFlag += 2;
                        rc.setFlag(encodedFlag);
                        break;
                    } else {
                        if (waitForCommand = false) {
                            System.out.println("Waiting for acknowledgement of flag setting...");
                            waitForCommand = true;
                        }
                    }
                }
            }
        }

        RobotInfo[] surroundings = rc.senseNearbyRobots();

        for (RobotInfo info : surroundings) {
            if (info.getType() == spawnableRobot[0] && (info.getInfluence() == 107)) {
                if (! politicianIDs.contains(info.getID())) {
                    politicianIDs.add(info.getID());
                }

                if (enemyBuildingLocations.size() != 0) {

                    int chosenTarget = (int) Math.floor(Math.random() * enemyBuildingLocations.size());

                    MapLocation attackPoint = enemyBuildingLocations.get(chosenTarget);
                    int codedFlag = ((attackPoint.x << 12) + (attackPoint.y << 5) + 1);
                    rc.setFlag(codedFlag);


                    if (rc.canGetFlag(info.getID())) {
                        while (rc.getFlag(info.getID()) != (codedFlag + 2))
                            if (rc.canGetFlag(info.getID())) {
                                if (rc.getFlag(info.getID()) == codedFlag + 2) {
                                    rc.setFlag(codedFlag + 2);
                                    break;
                                }
                            }
                            if (! rc.canGetFlag(info.getID())) {
                                System.out.println("Unable to obtain ID " + info.getID() + ", perhaps the bot was destroyed");
                                break;
                            }
                    }
                }

            }
        }



        if (rc.getRoundNum() % 20 == 0) {
            numberOfAttacks = enemyBuildingLocations.size();
        }

        for (MapLocation loc : enemyBuildingLocations) {
            if (numberOfAttacks > 0) {
                Direction chosenDirection = randomDirection();
                if (rc.canBuildRobot(spawnableRobot[0], chosenDirection, (15 * phase))) {
                    rc.buildRobot(spawnableRobot[0], chosenDirection, (15 * phase));
                    int politicianID = (rc.senseRobotAtLocation(myCoords.add(chosenDirection))).getID();

                    MapLocation destination = loc.translate(-1, -1);
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
                    numberOfAttacks -= 1;
                }
            }
        }


        // do the bots continue to exist or have an important location they identified for the EC to take note of?
        // This code handles those situations

        for (Integer id : muckrakerIDs) {

            // Remove this bot's ID from the ArrayList of muckraker IDs as this bot is probably dead
            if (! rc.canGetFlag(id)) {
                muckrakerIDs.remove(id);
                continue;
            }

            // Check if the first bit of the 24 sequence is 1, meaning that it's a message for the EC that created it
            int muckrakerFlag = rc.getFlag(id);

            if (muckrakerFlag > (1 << 23)) {
                // If bit 23 is set to 1, meaning that the bot hit the edge of the map

                // 4th to last bit determines whether Neutral or enemy unit; 1 if enemy, 0 if neutral
                if (((muckrakerFlag >> 3) & 1) == 1) {
                    MapLocation detectedEnemyEC = getDestinationFromFlag(muckrakerFlag);
                    if (! (enemyBuildingLocations.contains(detectedEnemyEC))) {
                        enemyBuildingLocations.add(detectedEnemyEC);
                    }

                    rc.setFlag(muckrakerFlag + 2);
                    while (rc.getFlag(id) == muckrakerFlag) {
                        if (rc.getFlag(id) == (muckrakerFlag + 2)) {
                            break;
                        }
                    }
                } else {
                    MapLocation detectedNeutralEC = getDestinationFromFlag(muckrakerFlag);
                    if (! (neutralBuildingLocations.contains(detectedNeutralEC))) {
                        neutralBuildingLocations.add(detectedNeutralEC);
                    }

                    rc.setFlag(muckrakerFlag + 2);
                    while (rc.getFlag(id) == muckrakerFlag) {
                        if (rc.canGetFlag(id)) {
                            if (rc.getFlag(id) == (muckrakerFlag + 2)) {
                                break;
                            }
                        }
                        if (! rc.canGetFlag(id)) {
                            System.out.println("Unable to obtain ID " + id + ", perhaps the bot was destroyed");
                            break;
                        }
                    }
                }
            }
        }



        for (Integer id : politicianIDs) {

            // Remove this bot's ID from the ArrayList of muckraker IDs as this bot is probably dead
            if (! rc.canGetFlag(id)) {
                muckrakerIDs.remove(id);
            }
            // Check if (flag > 2^23)?

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
                        int codedDestinationX = loc.x % 128;
                        int codedDestinationY = loc.y % 128;

                        int moveOrder = ((128 * codedDestinationX) + (codedDestinationY)) << 5;

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
        int normalBid = (int) (Math.floor(phase) + 1);
        int chanceBid = 2 * normalBid;


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

        // muckrakerRing is set to 0 every 200 rounds and for every muckraker spawned and coded incremented by 1
        // until it equals the length of the spawnableDirections array

        if (muckrakerRing != spawnableDirections.size()) {
            // First 3 bits of moveOrder are a direction from 0-7 in the directions[] array
            // the last 2 bits are SYN and ACK, respectively.
            int codedDirection;
            int j = 0;

            // Gives an order to the muckraker to move in a certain direction
            if (rc.canBuildRobot(RobotType.MUCKRAKER, spawnableDirections.get(muckrakerRing), phase)) {
                rc.buildRobot(RobotType.MUCKRAKER, spawnableDirections.get(muckrakerRing), phase);


                for (Direction dir : directions) {
                    if (spawnableDirections.get(muckrakerRing) != dir) {
                        j += 1;
                    } else {
                        break;
                    }
                }

                moveOrder = (j << 2) + 2;
                rc.setFlag(moveOrder);
                muckrakerID = rc.senseRobotAtLocation(myCoords.add(spawnableDirections.get(muckrakerRing))).getID();
                muckrakerIDs.add(muckrakerID);
                muckrakerRing += 1;

                // Set the muckraker's command while its flag is 0
                waitForCommand = false;
                while (rc.getFlag(muckrakerID) == 0) {
                    if (rc.getFlag(muckrakerID) == (moveOrder + 1)) {
                        // When the muckraker has both syn and ack flags on (last two bits are 1) set the ack flag of the EC to 1
                        moveOrder += 1;
                        rc.setFlag(moveOrder);
                        break;
                    } else {
                        if (waitForCommand = false) {
                            System.out.println("Waiting for acknowledgement of flag setting...");
                            waitForCommand = true;
                        }
                    }
                }
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
        int enemyMuckrakersInRange = 0;
        int adjacentMuckrakers = 0;

        for (int i = 0; i < 8; i ++) {
            if (rc.isLocationOccupied(myCoords.add(directions[i]))) {
                if (rc.senseRobotAtLocation(myCoords.add(directions[i])).getType() == RobotType.MUCKRAKER) {
                    adjacentMuckrakers += 1;
                }
            }
        }

        if (adjacentMuckrakers >= 1) {
            if (Math.random() < ((double) adjacentMuckrakers / 8)) {
                if (rc.canEmpower(2)) {
                    rc.empower(2);
                    System.out.println("Cleaning up muckrakers...");
                }
            }
        }

        if ((myFlag < (1 << 5)) && (rc.getInfluence() == 107)) {
            rc.setFlag(ecID + 2);
        }

        if (myFlag == 0) {
            // The SYN-ACK part of the transition
            if (rc.canGetFlag(ecID)) {
                // Set this politician's own flag to SYN-ACK
                rc.setFlag(rc.getFlag(ecID) + 2);
            }
        // If the robot can't move, if it's not on the edge, if there's a robot in its way...
        } else {
            for (RobotInfo info : surroundings) {
                if ((info.getType() == RobotType.ENLIGHTENMENT_CENTER) && (info.getTeam() == enemy) && ((myCoords.distanceSquaredTo(info.getLocation())) <= 5)) {
                    if (rc.canEmpower(myCoords.distanceSquaredTo(info.getLocation()))) {
                        rc.empower(myCoords.distanceSquaredTo(info.getLocation()));
                    }
                }
                if ((info.getType() == spawnableRobot[2]) && (rc.getTeam() == enemy)) {
                    enemyMuckrakersInRange += 1;
                }

            }

            if (enemyMuckrakersInRange >= 5) {
                if (rc.canEmpower(9)) {
                    rc.empower(9);
                    System.out.println("Boom I go, along with the wall...");
                }
            }

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
                int empowerRadius = myCoords.distanceSquaredTo(destination);
                if (empowerRadius <= 5) {
                    if (rc.canEmpower(empowerRadius)) {
                        rc.empower(empowerRadius);
                        System.out.println("empowered!");
                    }
                } else {
                    if (useBugDirection) {
                        basicBug(destination);
                        System.out.println("Basic bug to destination: " + getDestinationFromFlag(myFlag));
                    } else {
                        tryMove(myCoords.directionTo(destination));
                        System.out.println("Destination: " + getDestinationFromFlag(myFlag));
                    }
                }
            } else {
                if (useBugDirection) {
                    basicBug(destination);
                    System.out.println("Basic bug to destination: " + getDestinationFromFlag(myFlag));
                } else {
                    tryMove(myCoords.directionTo(destination));
                    System.out.println("Destination: " + getDestinationFromFlag(myFlag));
                }
            }
            if (alliedPoliticians >= 3) {
                for (Direction dir : directions) {
                    tryMove(dir);
                }
            }
        }


        // If you can't move in the direction you planned to...
        Direction bearing = myCoords.directionTo(getDestinationFromFlag(myFlag));

        if (! (rc.canMove(bearing))) {
            // If this robot is not on the map's edge...
            if (rc.getCooldownTurns() < 1) {
                if (rc.onTheMap(myCoords.add(bearing))) {
                    //
                    if (rc.canSenseLocation(myCoords.add(bearing))) {
                        if (!(rc.senseRobotAtLocation(myCoords.add(bearing)) == null)) {
                            Team detected = rc.senseRobotAtLocation(myCoords.add(bearing)).getTeam();
                            System.out.println("Obstacle of " + detected + " detected!");
                            sidestep();
                            /*
                            *if (cardinals.contains(bearing)) {
                            *    swerveAroundCardinal();
                            *} else {
                            *    swerveAroundDiagonal();
                            *}
                            *
                            */

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
                        sidestep();
                        /*if (cardinals.contains(myCoords.directionTo(getDestinationFromFlag(myFlag)))) {
                        *    swerveAroundCardinal();
                        *} else {
                        *    swerveAroundDiagonal();
                        *}
                        *
                        */
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


        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {

                    // 50% chance for the muckraker to change course towards the muckraker, in hopes of finding an EC
                    if (Math.random() > 0.5) {
                        Direction toMuckraker = myCoords.directionTo(robot.location);
                        int testedDirection = ((myFlag >> 2) & 7);
                        while (directions[testedDirection] != toMuckraker) {
                            testedDirection += 1;
                            if (testedDirection == 8) {
                                testedDirection = 0;
                            }
                        }
                        rc.setFlag((testedDirection << 2) + 3);
                    }


                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        if (((myFlag >> 19) & 1) == 1) {
            MapLocation centerOfOrbit = getDestinationFromFlag(myFlag);
            tryMove((myCoords.directionTo(centerOfOrbit).rotateRight()).rotateRight());
        }

        for (RobotInfo info : surroundings) {
            // Since the only neutral buildings are neutral ECs...

            if (info.getTeam() == Team.NEUTRAL) {

                if (! neutralBuildingLocations.contains(info.getLocation())) {
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
                    neutralBuildingLocations.add(info.getLocation());
                }
            }

            // If the muckraker detects an enemy EC:
            if ((info.getTeam() == enemy) && (info.getType() == RobotType.ENLIGHTENMENT_CENTER)) {


                MapLocation location = info.getLocation();
                int oldFlag = rc.getFlag(myID);
                int x = location.x % 128;
                int y = location.y % 128;
                if (! enemyBuildingLocations.contains(info.getLocation())) {
                    System.out.println("Enemy EC detected!");
                    //Stores the old flag in an integer while a new flag is created to send back to the EC
                    int transmittedData = ((1 << 23) + (((x << 7) + y) << 5) + (1 << 3) + 1);
                    rc.setFlag(transmittedData);
                    while ((rc.getFlag(myID)) == transmittedData) {
                        if (rc.getFlag(ecID) == (transmittedData + 2)) {
                            rc.setFlag(ecID);
                            break;
                        }
                    }
                    rc.setFlag(oldFlag);
                    enemyBuildingLocations.add(info.getLocation());
                }

                // 50% chance to start orbiting that enemy EC

                if (myCoords.distanceSquaredTo(info.getLocation()) <= 16) {
                    if ((Math.random() * 2) >= 1) {
                        int codedDirection = 0;
                        Direction initialOrbitDirection = myCoords.directionTo(info.getLocation());

                        MapLocation enemyECLocation = info.getLocation();

                        for (Direction dir : directions) {
                            if (initialOrbitDirection != dir) {
                                codedDirection += 1;
                                if (codedDirection == 8) {
                                    codedDirection = 0;
                                }
                            } else {
                                codedDirection += 2;
                                if (codedDirection >= 8) {
                                    codedDirection = codedDirection % 8;
                                }
                                break;
                            }
                        }

                        myFlag = (1 << 19) + (x << 12) + (y << 5) + (codedDirection << 2) + 3;
                    }
                }
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
        }

        if ((((myFlag - 3) >> 2) >= 0) && ((((myFlag - 3) >> 2)) <= 7)) {
            if (!(rc.canMove(directions[(myFlag - 3) >> 2]))) {
                if (rc.onTheMap(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                    if (rc.canSenseLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag))))) {
                        if (!(rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))) == null)) {
                            Team detected = rc.senseRobotAtLocation(myCoords.add(myCoords.directionTo(getDestinationFromFlag(myFlag)))).getTeam();
                            System.out.println("Obstacle of " + detected + " detected!");

                            sidestep();
                            /*if (((myFlag - 3) >> 2) % 2 == 0) {
                            *    swerveAroundCardinal();
                            *} else {
                            *    swerveAroundDiagonal();
                            *}
                            *
                            */
                        }
                    }
                } else {
                    int directionIndex = (myFlag >> 2) & 7;

                    // If moving in a cardinal direction
                    if (directionIndex % 2 == 0) {

                    }
                    // If moving diagonally
                    if (directionIndex % 2 == 1) {

                    }

                    while (! rc.onTheMap(myCoords.add(directions[directionIndex]))) {
                        directionIndex += 1;
                        if (directionIndex == 8) {
                            directionIndex = 0;
                        }
                    }
                    int newFlag = (directionIndex << 2) + 3;

                    rc.setFlag(newFlag);
                    System.out.println("Turning...");


                }
            } else {
                if (useBugDirection) {
                    basicBug(getDestinationFromFlag(myFlag));
                    System.out.println("Basic bug to destination: " + getDestinationFromFlag(myFlag));
                } else {
                    tryMove(myCoords.directionTo(getDestinationFromFlag(myFlag)));
                    System.out.println("Destination: " + getDestinationFromFlag(myFlag));
                }
            }

            for (Direction dir : directions) {
                if (! rc.onTheMap(myCoords.add(dir))) {
                    if ((Math.random() * 30 ) >= 29 && (rc.sensePassability(myCoords)) > 0.5) {
                        int directionIndex = (myFlag >> 2) & 7;

                        // If moving in a cardinal direction
                        if (directionIndex % 2 == 0) {

                        }
                        // If moving diagonally
                        if (directionIndex % 2 == 1) {

                        }

                        directionIndex += 1;
                        int newFlag = (directionIndex << 2) + 3;

                        rc.setFlag(newFlag);
                        System.out.println("Turning...");
                    }
                }
            }

        }

    }

    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;


    /* This method is a bit of a misnomer; while this is supposed to get the location of coordinates a bot is moving to,
    *  it also allows for if a robot is moving in one specific direction or if a robot found something at
    *  a specific location
    */

    static MapLocation getDestinationFromFlag(int flag) {

        // Going in one direction
        if ((flag < 32) || (((flag >> 19) & 1) == 1)) {

            if (((flag >> 19) & 1) == 1) {
                MapLocation orbitCenter = new MapLocation(flag >> 12, flag >> 5);
                if (myCoords.directionTo(orbitCenter) != directions[((flag >> 2) & 7)]) {
                    if (flag % 8 == 7) {
                        myFlag -= 7;
                        return myCoords.add(directions[(myFlag >>> 2) & 7]);
                    } else {
                        myFlag += 1;
                        return myCoords.add(directions[(myFlag >>> 2) & 7]);
                    }
                } else {
                    return myCoords.add(directions[(flag >>> 2) & 7]);
                }
            } else {
                MapLocation myCoords = rc.getLocation();
                return myCoords.add(directions[(flag >>> 2)]);
            }
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
    /*
    *static int checkMostOccupiedMuckrakerSpawnLocationIndex() throws GameActionException {
    *    MapLocation myCoords = rc.getLocation();
    *    int highestDirectionIndex = -1;
    *    for (int x : directionIndexes) {
    *        if (rc.isLocationOccupied(myCoords.add(directions[x]))) {
    *            highestDirectionIndex = x;
    *        }
    *    }
    *
    *    if (rc.getRoundNum() > (int) (Math.floor(7 * (2 / rc.sensePassability(myCoords) + 1)) + 1)) {
    *        return 13;
    *    } else if (highestDirectionIndex == -1) {
    *        return -1;
    *    } else {
    *        return highestDirectionIndex;
    *    }
    *}
    */

    static void sidestep() throws GameActionException {
        int myID = rc.getID();
        int myFlag = rc.getFlag(myID);
        MapLocation myCoords = rc.getLocation();
        Direction failedDirection = myCoords.directionTo(getDestinationFromFlag(myFlag));

        Direction testR1 = failedDirection.rotateRight();
        Direction testR2 = testR1.rotateRight();
        Direction testR3 = testR2.rotateRight();

        Direction testL1 = failedDirection.rotateLeft();
        Direction testL2 = testL1.rotateLeft();
        Direction testL3 = testL2.rotateLeft();

        Direction last = failedDirection.opposite();

        if (rc.isLocationOccupied(myCoords.add(failedDirection)) && rc.isReady()) {
            if (rc.canMove(testL1) && rc.canMove(testR1)) {
                double passabilityL = rc.sensePassability(myCoords.add(testL1));
                double passabilityR = rc.sensePassability(myCoords.add(testR1));
                if (passabilityL > passabilityR) {
                    rc.move(testL1);
                } else {
                    rc.move(testR1);
                }
            } else if (rc.canMove(testL1)) {
                rc.move(testL1);
            } else {
                if (rc.canMove(testR1)) {
                    rc.move(testR1);
                }
            }
            if (rc.canMove(testL2) && rc.canMove(testR2)) {
                double passabilityL = rc.sensePassability(myCoords.add(testL2));
                double passabilityR = rc.sensePassability(myCoords.add(testR2));
                if (passabilityL > passabilityR) {
                    rc.move(testL2);
                } else {
                    rc.move(testR2);
                }
            } else if (rc.canMove(testL2)) {
                rc.move(testL2);
            } else {
                if (rc.canMove(testR2)) {
                    rc.move(testR2);
                }
            }

            if (rc.canMove(testL3) && rc.canMove(testR3)) {
                double passabilityL = rc.sensePassability(myCoords.add(testL3));
                double passabilityR = rc.sensePassability(myCoords.add(testR3));
                if (passabilityL > passabilityR) {
                    rc.move(testL3);
                } else {
                    rc.move(testR3);
                }
            } else if (rc.canMove(testL3)) {
                rc.move(testL3);
            } else {
                if (rc.canMove(testR3)) {
                    rc.move(testR3);
                }
            }

            tryMove(last);
            System.out.println("Sidestepping obstacle...");
        }
    }

    // Deprecated method for moving if the original intended direction was blocked
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
                        if (tryMove(dir)) {
                            watchdogTimer = 0;
                            System.out.println("Timed out, moving " + dir);
                            break;
                        }
                    }
                    step += 1;
                } else {
                    tryMove((currentDirection).rotateRight());
                    step += 1;
                }
            }

            while (step == 2) {
                if (! rc.canMove((currentDirection).rotateLeft())) {
                    if (rc.isReady()) {
                        if (! rc.onTheMap(myCoords.add(currentDirection.rotateLeft()))) {
                            int directionIndex = (myFlag >> 2) & 7;
                            while (! rc.onTheMap(myCoords.add(directions[directionIndex]))) {
                                directionIndex += 1;
                                if (directionIndex == 8) {
                                    directionIndex = 0;
                                }
                            }
                            int newFlag = (directionIndex << 2) + 3;
                            rc.setFlag(newFlag);
                        }



                    }
                    Clock.yield();
                } else if (watchdogTimer > 3) {
                    for (Direction dir : directions) {
                        if (tryMove(dir)) {
                            watchdogTimer = 0;
                            System.out.println("Timed out, moving " + dir);
                            break;
                        }
                    }
                    step = 0;
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
                        if (tryMove(dir)) {
                            watchdogTimer = 0;
                            System.out.println("Timed out, moving " + dir);
                            break;
                        }
                    }
                    step += 1;
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
                        if (tryMove(dir)) {
                            watchdogTimer = 0;
                            System.out.println("Timed out, moving " + dir);
                            break;
                        }
                    }
                    step += 1;
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
                    step = 0;
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

    // Borrowed code from Jerry Mao's Pathfinding lecture, with modifications
    static final double passabilityThreshold = 0.6;
    static Direction bugDirection = null;

    static void basicBug(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)) {
            // do something else, now that you're there
            System.out.println("Arrived at target destination");
        } else if (rc.isReady()) {

            double direction = Math.floor(Math.random()) * 2;
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                rc.move(d);
                bugDirection = null;
            } else {

                if (bugDirection == null) {
                    bugDirection = d;
                }
                for (int i = 0; i < 8; ++i) {
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                        rc.setIndicatorDot(rc.getLocation().add(bugDirection), 0, 255, 255);
                        rc.move(bugDirection);
                        if (direction <= 1) {
                            bugDirection = bugDirection.rotateLeft();
                        } else {
                            bugDirection = bugDirection.rotateRight();
                        }
                        break;
                    }
                    rc.setIndicatorDot(rc.getLocation().add(bugDirection), 255, 0, 0);
                    if (direction <= 1) {
                        bugDirection = bugDirection.rotateRight();
                    } else {
                        bugDirection = bugDirection.rotateLeft();
                    }
                }
            }
        }
    }

}