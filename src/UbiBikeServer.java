import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class UbiBikeServer {

    private static HashMap<String, User> users = new HashMap<String, User>();
    private static HashMap<String, User> loggedUsers = new HashMap<String, User>();
    private static HashMap<String, Station> stations = new HashMap<String, Station>();



    public static void main(String[] args) {


        System.out.println(new Date().toString() + " UbiBike Server 2 started!");

        initStations();


        try {
            ServerSocket server = new ServerSocket(4444);
            //keep listens indefinitely until receives 'exit' call or program terminates
            while(true){
                System.out.println(new Date().toString() + " Waiting for client request");
                //creating socket and waiting for client connection
                Socket socket = server.accept();
                //read from socket to ObjectInputStream object
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //convert ObjectInputStream object to String
                String message = (String) ois.readObject();
                System.out.println(new Date().toString() + " Message Received: " + message);
                String response = parseMessage(message);
                System.out.println(new Date().toString() + " Message to send: " + response);
                //create ObjectOutputStream object
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //write object to Socket
                oos.writeObject(response);
                //close resources
                ois.close();
                oos.close();
                socket.close();

            }
        } catch (Exception ex) {
            System.out.println(new Date().toString() + " Error in server main: " + ex);
        }
    }

    private static String parseMessage(String clientmessage) {
        try
        {
            String result;
            // message parameters are separated by the character |
            String[] splitMessage = clientmessage.split("\\|");

            // 1 - operation is the 1st parameter in the message (REGISTER, LOGIN)
            switch (splitMessage[0]) {
                case "REGISTER":
                    result = register(splitMessage);
                    break;
                case "LOGIN":
                    result = login(splitMessage);
                    break;
                case "LOGOUT":
                    result = logout(splitMessage);
                    break;
                case "SENDPOINTS":
                    result = sendPoints(splitMessage);
                    break;
                case "USERINFO":
                    result = getUserInfo(splitMessage);
                    break;
                case "RESERVE":
                    result = reserveBike(splitMessage);
                    break;
                case "STATIONSBIKES":
                    result = stationsWithBikes(splitMessage);
                    break;
                case "SENDTRAJECTORY":
                    result = sendTrajectory(splitMessage);
                    break;

                default:
                    result = "Invalid Operation: " + splitMessage[0];
                    break;

            }

            return result;
        }
        catch (Exception ex) {
            System.out.println("Error while parsing the message:");
            System.out.println(ex);

            return "ERROR";
        }
    }



    //region Functionalities

    private static String register(String[] splitMessage) {
        //Message Template: REGISTER|username|password
        try {
            if(splitMessage.length != 3) {
                return "ERROR|Register error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(users.containsKey(username)) {
                return "ERROR|Register error: User already exists";
            }
            else {
                String password = splitMessage[2];

                User newUser = new User(username, password);
                users.put(username, newUser);

                return "OK|User '" + username + "' has been created!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while registering the user:");
            System.out.println(ex);
            return "ERROR|Register error.";
        }
    }

    private static String login(String[] splitMessage) {
        //Message Template: LOGIN|username|password
        try {
            if(splitMessage.length != 3)
            {
                return "ERROR|Login error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "ERROR|Login error: User '" + username + "' is not registered!";
            }

            String password = splitMessage[2];

            User activeUser = users.get(username);
            if(activeUser.password.equals(password)) {
                loggedUsers.put(username, activeUser);
                return "OK|User '" + username + "' logged in!";
            }
            else {
                return "ERROR|Invalid password for user '" + username + "'!";
            }

        }
        catch (Exception ex) {
            System.out.println("Error while logging in the user:");
            System.out.println(ex);
            return "ERROR|Login error.";
        }
    }

    private static String logout(String[] splitMessage) {
        //Message Template: LOGOUT|username
        try {

            if(splitMessage.length != 2)
            {
                return "ERROR|Login error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "ERROR|Logout error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                loggedUsers.remove(username);
                return "OK|User '" + username + "' logged out!";
            }
            else {
                return "ERROR|User '" + username + "' was not logged in!";
            }

        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return "ERROR|Logout error.";
        }
    }

    private static String sendPoints(String[] splitMessage) {
        //Message Template: SENDPOINTS|username1|username2|points
        try {

            if(splitMessage.length != 4)
            {
                return "ERROR|Login error: Invalid number of parameters!";
            }

            String username1 = splitMessage[1];
            String username2= splitMessage[2];
            int pointsToSend= Integer.parseInt(splitMessage[3]);


            if(!users.containsKey(username1)) {
                return "ERROR|Send points error: Origin user '" + username1 + "' is not registered!";
            }
            if(!users.containsKey(username2)) {
                return "ERROR|Send points error: Destination user '" + username2 + "' is not registered!";
            }


            User user1 = users.get(username1);
            User user2 = users.get(username2);

            if(loggedUsers.containsKey(username1)) {
                if(user1.points < pointsToSend) {
                    return "ERROR|User '" + username1 + "' doesn't have enough points to send!";
                }
                else {
                    user1.points -= pointsToSend;
                    user2.points += pointsToSend;
                    return "OK|" + user1.username + "|" + user1.points + "|" + user2.username + "|" + user2.points;

                }
            }
            else {
                return "ERROR|User '" + username1 + "' must be logged in to send points!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return "ERROR|Logout error.";

        }
    }

    private static String getUserInfo(String[] splitMessage) {
        //Message Template: USERINFO|username
        try {

            if(splitMessage.length != 2)
            {
                return "ERROR|User info error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "ERROR|User info error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                // messages template:
                // ok | points | trajectorylist
                // OK|123|&trajectory1name-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy&trajectory2-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy
                String userPoints = new Integer(activeUser.points).toString();
                String userTrajectories = "";
                for (Trajectory trajectory: activeUser.trajectories) {
                    userTrajectories += "&" + trajectory.trajectoryName;
                    for (Location location: trajectory.locations) {
                        userTrajectories += "-" + location.latitude + "+" + location.longitude;
                    }
                }
                return "OK|" + userPoints + "|" + userTrajectories;
            }
            else {
                return "ERROR|User '" + username + "' must be logged in to get his info!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting info of the user:");
            System.out.println(ex);
            return "ERROR|User info error.";
        }
    }

    private static String reserveBike(String[] splitMessage) {
        //Message Template: RESERVE|username|stationname
        try {

            if(splitMessage.length != 3)
            {
                return "ERROR|Reservation error: Invalid number of parameters!";
            }

            String username = splitMessage[1];
            String stationname = splitMessage[2];

            if(!users.containsKey(username)) {
                return "ERROR|Reservation error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                if(stations.containsKey(stationname)) {
                    Station station = stations.get(stationname);
                    if(station.bikes > station.reservations.size())
                    {
                        station.reservations.add(username);
                        return "OK|Bike reserved for user '" + username + "' in station '" + stationname + "'!";
                    }
                    else {
                        return "ERROR|No bikes available for reservation in station '" + stationname + "'!";
                    }
                }
                else {
                    return "ERROR|Station '" + stationname + "' is not available!";
                }
            }
            else {
                return "ERROR|User '" + username + "' must be logged in to reserve a bike!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while reserving:");
            System.out.println(ex);
            return "ERROR|Reserving error.";
        }
    }


    private static String stationsWithBikes(String[] splitMessage) {
        //Message Template: STATIONSBIKES|username
        try {

            if(splitMessage.length != 2)
            {
                return "ERROR|Get stations with bikes error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "ERROR|Get stations with bikes error: User '" + username + "' is not registered!";
            }

            if(loggedUsers.containsKey(username)) {
                // messages template:
                // ok | stations available | station list
                // OK|123|&stationName-xxxx+yyyy&stationName-xxxx+yyyy
                String stationList = "";
                int stationsAvailable= 0;
                for (HashMap.Entry<String, Station> station : stations.entrySet())
                {
                    if(station.getValue().bikes > station.getValue().reservations.size()) {
                        Station currentStation = station.getValue();
                        stationsAvailable++;
                        stationList += "&" + currentStation.stationName + "-" + currentStation.location.latitude + "+" + currentStation.location.longitude;
                    }
                }
                return "OK|" + stationsAvailable + "|" + stationList;
            }
            else {
                return "ERROR|User '" + username + "' must be logged in to get the stations with bikes!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting stations with bikes:");
            System.out.println(ex);
            return "ERROR|Getting stations with bikes error.";
        }
    }

    public static String sendTrajectory(String[] splitMessage) {
        //Message Template: SENDTRAJECTORY|username|&trajectory1name-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy

        try {

            if(splitMessage.length != 3)
            {
                return "ERROR|Send trajectory error: Invalid number of parameters!";
            }

            String username = splitMessage[1];
            String trajectoryString = splitMessage[2];


            if(!users.containsKey(username)) {
                return "ERROR|Send trajectory error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                //&trajectory1name-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy&trajectory1name-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy
                String[] trajectorySplit = trajectoryString.split("&");
                for(int i=1; i < trajectorySplit.length; i++) {
                    //trajectory1name-xxxx+yyyy-xxxx+yyyy-xxxx+yyyy
                    String[] trajectoryLocationsSplit = trajectorySplit[i].split("\\-");

                    Trajectory trajectory = new Trajectory(trajectorySplit[0], new ArrayList<Location>());

                    for(int j=1; j < trajectoryLocationsSplit.length; j++) {
                        //xxxx+yyyy
                        String[] locationSplit = trajectoryLocationsSplit[i].split("\\+");
                        Location location = new Location(Double.parseDouble(locationSplit[0]), Double.parseDouble(locationSplit[1]));
                        trajectory.locations.add(location);
                    }
                    activeUser.trajectories.add(trajectory);
                }
                return "OK|Trajectory received for user '" + username + "'!";
            }
            else {
                return "ERROR|User '" + username + "' must be logged in to send trajectory!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while sending trajectory:");
            System.out.println(ex);
            return "ERROR|Send trajectory error.";
        }

    }
    //endregion

    // region stationInitialization
    public static void initStations() {
        Station station1 = new Station("Station1", new Location(38.75322986, -9.20676827), 5);
        Station station2 = new Station("Station2", new Location(38.75077, -9.19113), 10);
        Station station3 = new Station("Station3", new Location(38.7601071, -9.18283225), 15);


        //add stations to global list
        stations.put(station1.stationName, station1);
        stations.put(station2.stationName, station2);
        stations.put(station3.stationName, station3);

    }
    //endregion

    //region SubClasses

    public static class User {
        public String username;
        public String password;
        public int points;
        public ArrayList<Trajectory> trajectories;


        public User(String name, String pass) {
            username = name;
            password = pass;
            points = 0;
            trajectories = new ArrayList<Trajectory>();
        }

    }

    public static class Station {
        public String stationName;
        public Location location;
        public int bikes;
        public ArrayList<String> reservations;

        public Station(String name, Location loc, int nbikes) {
            stationName = name;
            location = loc;
            bikes = nbikes;
            reservations = new ArrayList<String>();
        }

    }


    public static class Location {
        public double latitude;
        public double longitude;

        public Location(double lat, double lon) {
            latitude = lat;
            longitude = lon;
        }

    }

    public static class Trajectory {
        public String trajectoryName;
        public ArrayList<Location> locations;


        public Trajectory(String name, ArrayList<Location> locs) {
            trajectoryName = name;
            locations = locs;
        }

    }

    //endregion

}
