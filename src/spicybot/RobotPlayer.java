// This code borrows off Battlecode 2021's examplefuncsplayer and Jerry Mao's RobotPlayer from the pathfinding lecture.

package spicybot;
import battlecode.common.*;
import java.util.*;

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
    static final Direction[] diagonals = {
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST,
    };
    static final Direction[] sides = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };
    static MapLocation ECCords;
    static double ECpassability;


    public static void run(RobotController rc) throws GameActionException {
        // turnCount = 0;
        spicybot.RobotPlayer.rc = rc;
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

    //TODO the base won't know the existence of converted politicians, so make them useful without assignment
    //assign roles to numbers (4 bytes [0, 15])
    static final int lost = 0;
    static final int explorer = 1;
    static final int hopelessSlanderer = 2;
    static final int protectedSlanderer = 3;
    static final int communicator = 4;
    static final int wall = 5;
    static final int wallFluff = 6;

    //assign commands to numbers (4 bytes [0, 15])
    static final int assignRole = 1; //for the EC to use to assign a role to a bot
    static final int baseID = 2; //for the bots to use to tell each other who their EC is
    static final int enemyBotFound = 3; //for the bots to use to tell the EC when they found an Important enemy
    static final int enemyBaseFound = 4; //for the bots to use when they found an enemy base
    static final int foundWall = 5; //to communicate they found a wall


    //transform from number to binary string
    static String int_to_binary(int number, int byteSize){
        String binary = Integer.toBinaryString(number);
        if (binary.length() < byteSize){
            //makes a string with the number of 0s missing
            char[] charArray = new char[byteSize - binary.length()];
            Arrays.fill(charArray, '0');
            String filling = new String(charArray);

            binary = filling + binary;
        } else if (binary.length() > byteSize){
            System.out.println("Error! the int " + number + "was too big to fit in the byte size" );
        }
        return binary;
    }
    //transform from binary string to number
    static int binary_to_int(String binary){
        return Integer.parseInt(binary, 2);
    }


    //keeps track of all our robots and which type they are
    static Map <Integer, RobotType> allBotsMap = new HashMap<>();

    //two ways of making a robot, depending whether you care about the direction or not
    //NOT READY YET
    /*
    static void makeRobot(RobotType type, int roleID) throws GameActionException{

        Direction selectedDirection = randomDirection();
        if (rc.canBuildRobot(RobotType.SLANDERER, selectedDirection, 130)) {
            rc.buildRobot(RobotType.SLANDERER, selectedDirection, 130);
        }
    }
    */

    //TODO optimize this
    //make robots for you, one of them if you know which direction and the other if you don't care the direction
    static boolean makeRobot(RobotType type, int influence, int roleID, Direction direction ) throws GameActionException{
        System.out.println(ECCords + "EC CORDS!!!!!!!");
        if (rc.canBuildRobot(type, direction, influence)) {
            //make the robot
            rc.buildRobot(type, direction, influence);

            //get the robot's ID to add them to the ID map
            int botId = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                if (ECCords.directionTo(robot.location) == direction){
                    botId = robot.ID;
                }
            }
            if (botId == 0){
                System.out.println("ERROR!! Not able to find ID for some reason");
                return false;
            }
            allBotsMap.put(botId, type);

            //tell the new robot its assigned ID
            int flagInt = binary_to_int( int_to_binary(assignRole, 4) + int_to_binary(botId, 16) + int_to_binary(roleID, 4));
            rc.setFlag(flagInt);

            return true;
        } else {
            return false;
        }
    }
    static boolean makeRobot(RobotType type, int influence, int roleID) throws GameActionException{
        int botId = 0;
        for (Direction direction : directions) {
            if (rc.canBuildRobot(type, direction, influence)) {
                //make the robot
                rc.buildRobot(type, direction, influence);

                //get the robot's ID to add them to the ID map

                for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
                    if (ECCords.directionTo(robot.location) == direction) {
                        botId = robot.ID;
                    }
                }
                break;
            }
        }
        if (botId == 0){
            System.out.println("ERROR!! Not able to find ID for some reason or to make robot!");
            return false;
        }
        allBotsMap.put(botId, type);

        //tell the new robot its assigned ID
        int flagInt = binary_to_int( int_to_binary(assignRole, 4) + int_to_binary(botId, 16) + int_to_binary(roleID, 4));
        rc.setFlag(flagInt);

        return true;

    }

    //after the EC does all the essential actions for the round, this runs, checking all (or as many as possible) bots for flags and information
    static boolean boredEC(){
        return true;
    }

    static void bid() throws GameActionException {
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

    // Code for Enlightenment Centers
    static void runCodeEC() throws GameActionException {

        // Get the coordinates of the Enlightenment Center, get that coordinates' passability
        ECCords = rc.getLocation();
        ECpassability = rc.sensePassability(ECCords);

        double safetyIndex = 10; //this is how safe our EC feels and it will make decisions based on that

        //uhhh what?? Sets up the coordinates to where the muckrakers will form around the Enlightenment Center
        int[] xMDefenders = {-4, -4, -4, -4, -3, -3, -2, -1, 0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4};
        int[] yMDefenders = {0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 2, 1, 0, -1, -2, -3, -3, -4, -4, -4, -4, -4, -4, -4, -3, -3, -2, -1};
        int[] xPDefenders = {-8, 0, 8, 8, 8, 0, -8, -8};
        int[] yPDefenders = {8, 8, 8, 0, -8, -8, -8, 0};

        boolean sBuilt = true;

        //TODO - make a new slandered if there is room and safe, determine influence on safety index
        //?????then spawn a slanderer every 50 or so rounds depending on the passability
        Direction selectedDirection = randomDirection();
        int spawnRobot = (int) Math.floor((rc.getRoundNum() - 1) / rc.sensePassability(ECCords));

//Strategy for EC at passability 1, safety index 10, not touching any walls
        //TODO implement strategy for lower safety indexes and different passabilities
        //TODO make a set for each one of the roles so the EC can keep track and make it easier to reassign and search within them
        //TODO find out what happens in our code when a game exception happens

        makeRobot(RobotType.SLANDERER, 130, hopelessSlanderer, Direction.NORTHEAST);
        bid();
        Clock.yield();
        for (Direction direction : sides){
            while (! rc.isReady()){
                bid();
                Clock.yield();
            }
            makeRobot(RobotType.POLITICIAN, 1, explorer, direction);
        }
        for (int i = 0; i < 4; i ++){
            while (! rc.isReady()){
                bid();
                Clock.yield();
            }
            makeRobot(RobotType.MUCKRAKER, 1, explorer);
        }
/*
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

 */
        while (rc.getRoundNum() < 1500){
            if (rc.isReady()){
                makeRobot(RobotType.MUCKRAKER, 1, explorer);
            }
            bid();
            Clock.yield();
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

    //PUT ALL THE ROLE CODES HERE
    static void runExplorer(int ecID, MapLocation ecLocation) throws GameActionException {
        int thisID = rc.getID();
        //if its next to an enlightenment center, use that to decide its destination (by going in the opposite direction)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                ecID = robot.ID;
                ECCords = robot.location;
            }
        }



    }
    static void runRole(int role, int ecID, MapLocation ECCords) throws GameActionException{
        //run the correct code based on this robot's role
        switch (role){
            case lost:
                //run lost
                break;
            case explorer:
                runExplorer(ecID, ECCords);
                break;
            case hopelessSlanderer:
                //run hopelessSlanderer
                break;
            case protectedSlanderer:
                //run protectedSlanderer
                break;
            case communicator:
                //run communicator
                break;
            case wall:
                //run wall
                break;
            case wallFluff:
                //run wallFluff
                break;
        }
    }

    // Code for POLITICIANS
    static void runCodeP() throws GameActionException {
        int thisID = rc.getID();
        int ecID = 0;
        int thisRole = 0; //this is the role Lost
        MapLocation ECCords = null;
        MapLocation pMovementDestination = null;

        MapLocation here = rc.getLocation();

        //gets the location and coordinates of the EC
        for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                ecID = robot.ID;
                ECCords = robot.location;
            }
        }

        //reads the ECs flag to get your assignment type
        if (rc.canGetFlag(ecID)) {
            int readingFlag = rc.getFlag(ecID);
            String binaryFlag = int_to_binary(readingFlag, 24);
            int command = binary_to_int(binaryFlag.substring(0, 4));
            int flagID = binary_to_int(binaryFlag.substring(4, 20));
            if (command == assignRole && flagID == thisID) {
                thisRole = binary_to_int(binaryFlag.substring(20, 24));
            } else {
                System.out.println("ERROR! The politician assignment flag didn't match what it should to assign" + binaryFlag);
            }
        } else {
            System.out.println("ERROR!!!! Politician could not get flag to assign its role!" + thisID);
        }

        //this waits out the initial 10 round delay
        while(! rc.isReady()){
            Clock.yield();
        }
        //takes us to role central which is basically a switch that runs the correct role code
        runRole(thisRole, ecID, ECCords);

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
