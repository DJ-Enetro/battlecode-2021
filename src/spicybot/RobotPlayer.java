// This code borrows off Battlecode 2021's examplefuncsplayer and Jerry Mao's RobotPlayer from the pathfinding lecture.

package spicybot;
import battlecode.common.*;
import java.util.*;
import java.lang.*;

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

    //where the robot will store the passability of all squares it sees
    static Map<MapLocation, Double> passabilityMap = new HashMap<>();

    //given a location, return all the location adjecent to that one (without using add())
    static Set<MapLocation> getAdjacent(MapLocation location){
        Set<MapLocation> adjacent = new HashSet<>();
        System.out.println("bytes1 " + Clock.getBytecodeNum());
        int xCord = location.x;
        int yCord = location.y;
        System.out.println("bytes2 " + Clock.getBytecodeNum());
        for (int i = -1; i <= 1; i++){
            int x = xCord + i;

            for (int j = -1; j <= 1; j++){
                adjacent.add(new MapLocation(x, yCord + j));
            }
        }
        System.out.println("bytes3 " + Clock.getBytecodeNum());
        adjacent.remove(location);
        return adjacent;
    }

    static int getSensingDistanceSquared(RobotType type){
        int sensingDistanceSquared = 0;
        switch (type){
            case MUCKRAKER:
                sensingDistanceSquared = 30;
                break;
            case SLANDERER:
                sensingDistanceSquared = 20;
                break;
            case POLITICIAN:
                sensingDistanceSquared = 25;
                break;
            case ENLIGHTENMENT_CENTER:
                sensingDistanceSquared = 40;
                break;
        }
        return sensingDistanceSquared;
    }
    //finds the farthest location that the robot can sense in the given direction
    static MapLocation findFarthest(Direction direction, MapLocation startingLocation, RobotType type){
        int sensingDistanceSquared = getSensingDistanceSquared(type);
        int goalDistance = 0;
        MapLocation goal = startingLocation;
        MapLocation tempGoal = goal;
        while(goalDistance < sensingDistanceSquared){
            goal = tempGoal;
            tempGoal = tempGoal.add(direction);
            goalDistance = tempGoal.distanceSquaredTo(startingLocation);
        }
        System.out.println("goal distance, " + goalDistance + "sensingDIstanceSquared, " + sensingDistanceSquared);
        return goal;

    }


    static RobotInfo[] moveTowards(Direction direction, RobotType type) throws GameActionException {
        int sensingDistanceSquared = getSensingDistanceSquared(type);

        Map<MapLocation, Double> agenda = new HashMap<>();
        Set<MapLocation> expanded = new HashSet<>();
        Map<MapLocation, MapLocation> children = new HashMap<>();

        MapLocation startingLocation = rc.getLocation();
        MapLocation current = startingLocation;


        //we need to sense the location of all robot around us to see if there are any of them blocking our path
        //since sensing this is relatively expensive and our function doesn't have to return anything, just move,
        // we will return the information that we get from the sensing so whichever function called us can use it without wasting more bytes calling it again
        RobotInfo[] robotsNearby = rc.senseNearbyRobots();
        //if the robot is in a position right next to our starting position, we add it to the expanded set so that
        //our robot can't consider moving into it. If the robot isn't immediately right next to ours, that means he could
        //move next round, so we won't concern ourselves with his location yet
        for (RobotInfo eachRobot : robotsNearby){
            if (eachRobot.location.distanceSquaredTo(startingLocation) == 2){
                expanded.add(eachRobot.location);
            }
        }

        //finds our goal location which is the farthest location that the robot can sense in the given direction
        MapLocation goal = findFarthest(direction, startingLocation, type);

        //repeats until we find a path that gets to the goal
        while (!goal.equals(current)){
            //finds and gets the location in the agenda with the smallest cooldown
            double smallest = 10000;
            for (MapLocation agendaLocation : agenda.keySet() ){
                double cooldown = agenda.get(agendaLocation);
                if (cooldown + Math.sqrt(agendaLocation.distanceSquaredTo(goal)) < smallest) {
                    smallest = cooldown;
                    current = agendaLocation;
                }
            }
            agenda.remove(current);
            expanded.add(current);
            Set<MapLocation> adjacent = getAdjacent(current);

            //will add the children to the agenda if we have their passability
            for (MapLocation eachAdjacent : adjacent){
                //will only add them if they haven't been considered already (expanded) or if they weren't added already
                if ((! expanded.contains(eachAdjacent)) && (!agenda.containsKey(eachAdjacent))){
                    //if we haven't found this square's passability yet but we can, find it and save it to the passabilityMap
                    if ((!passabilityMap.containsKey(eachAdjacent)) && (startingLocation.distanceSquaredTo(eachAdjacent) <= sensingDistanceSquared)){
                        passabilityMap.put(eachAdjacent, rc.sensePassability(eachAdjacent));
                    }
                    //if we don't have the passability of this square, we can't consider it and add it to the agenda
                    if (passabilityMap.containsKey(eachAdjacent)){
                        children.put(eachAdjacent, current);
                        agenda.put(eachAdjacent, smallest + (1/passabilityMap.get(eachAdjacent)));
                    }
                }
            }
            if (agenda.isEmpty()){
                System.out.println("Couldn't find somewhere to move!");
                return robotsNearby;
            }
        }
        MapLocation movingTo = current;
        while (!current.equals(startingLocation)){
            movingTo = current;
            current = children.get(current);
        }
        Direction movingDirection = startingLocation.directionTo(movingTo);

        if (rc.canMove(movingDirection)){
            rc.move(movingDirection);
        } else  {
            System.out.println("failed to move to " + movingTo);
        }
        return robotsNearby;

    }



    //PUT ALL THE ROLE CODES HERE
    static void runExplorer(int ecID, MapLocation ecLocation) throws GameActionException {
        int thisID = rc.getID();
        RobotType type = rc.getType();
        //if its next to an enlightenment center, use that to decide its destination (by going in the opposite direction)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                ecID = robot.ID;
                ECCords = robot.location;
            }
        }
        Direction direction = rc.getLocation().directionTo(ECCords).opposite();

        while (rc.onTheMap(findFarthest(direction, rc.getLocation(), type))){
            if (rc.isReady()){
                RobotInfo[] nearbyRobots = moveTowards(direction, type);
                System.out.println("bytes after moving " + Clock.getBytecodeNum());
            } else {
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            }


            Clock.yield();
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
