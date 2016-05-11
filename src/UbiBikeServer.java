import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;

public class UbiBikeServer {

    private static HashMap<String, User> users = new HashMap<String, User>();
    private static HashMap<String, User> loggedUsers = new HashMap<String, User>();
    private static HashMap<String, Station> stations = new HashMap<String, Station>();



    public static void main(String[] args) {


        System.out.println("UbiBike Server 2 started!");

        // TODO - init stations properly
        Station station1 = new Station("station1", new Location(111, 111, 111), 5);
        Station station2 = new Station("station2", new Location(222, 222, 222), 5);
        stations.put(station1.stationName, station1);
        stations.put(station2.stationName, station2);


        try {
            ServerSocket server = new ServerSocket(4444);
            //keep listens indefinitely until receives 'exit' call or program terminates
            while(true){
                System.out.println("Waiting for client request");
                //creating socket and waiting for client connection
                Socket socket = server.accept();
                //read from socket to ObjectInputStream object
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //convert ObjectInputStream object to String
                String message = (String) ois.readObject();
                System.out.println("Message Received: " + message);
                String response = parseMessage(message);
                //create ObjectOutputStream object
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //write object to Socket
                oos.writeObject(response);
                //close resources
                ois.close();
                oos.close();
                socket.close();
                //terminate the server if client sends exit request
                if(message.equalsIgnoreCase("exit")) break;
            }

            server.close();
        } catch (Exception ex) {
            System.out.println("Error in server main: " + ex);
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
                return "Register error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(users.containsKey(username)) {
                return "Register error: User already exists";
            }
            else {
                String password = splitMessage[2];

                User newUser = new User(username, password);
                users.put(username, newUser);

                return "User '" + username + "' has been created!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while registering the user:");
            System.out.println(ex);
            return "Register error.";
        }
    }

    private static String login(String[] splitMessage) {
        //Message Template: LOGIN|username|password
        try {
            if(splitMessage.length != 3)
            {
                return "Login error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "Login error: User '\" + username + \"' is not registered!";
            }

            String password = splitMessage[2];

            User activeUser = users.get(username);
            if(activeUser.password.equals(password)) {
                loggedUsers.put(username, activeUser);
                return "User '" + username + "' logged in!";
            }
            else {
                return "Invalid password for user '" + username + "'!";
            }

        }
        catch (Exception ex) {
            System.out.println("Error while logging in the user:");
            System.out.println(ex);
            return "Login error.";
        }
    }

    private static String logout(String[] splitMessage) {
        //Message Template: LOGOUT|username
        try {

            if(splitMessage.length != 2)
            {
                return "Login error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "Logout error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                loggedUsers.remove(username);
                return "User '" + username + "' logged out!";
            }
            else {
                return "User '" + username + "' was not logged in!";
            }

        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return "Logout error.";
        }
    }

    private static String sendPoints(String[] splitMessage) {
        //Message Template: SENDPOINTS|username1|username2|points
        try {

            if(splitMessage.length != 4)
            {
                return "Login error: Invalid number of parameters!";
            }

            String username1 = splitMessage[1];
            String username2= splitMessage[2];
            int pointsToSend= Integer.parseInt(splitMessage[3]);


            if(!users.containsKey(username1)) {
                return "Send points error: Origin user '" + username1 + "' is not registered!";
            }
            if(!users.containsKey(username2)) {
                return "Send points error: Destination user '" + username2 + "' is not registered!";
            }


            User user1 = users.get(username1);
            User user2 = users.get(username2);

            if(loggedUsers.containsKey(username1)) {
                if(user1.points < pointsToSend) {
                    return "User '" + username1 + "' doesn't have enough points to send!";
                }
                else {
                    user1.points -= pointsToSend;
                    user2.points += pointsToSend;
                    return "User '" + username1 + "' gave '" + pointsToSend + "' points to user '" + username2 + "'!";

                }
            }
            else {
                return "User '" + username1 + "' must be logged in to send points!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return "Logout error.";

        }
    }

    private static String getUserInfo(String[] splitMessage) {
        //Message Template: USERINFO|username
        try {

            if(splitMessage.length != 2)
            {
                return "User info error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "User info error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                return "User '" + username + "' info available.";
            }
            else {
                return "User '" + username + "' must be logged in to get his info!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting info of the user:");
            System.out.println(ex);
            return "User info error.";
        }
    }

    private static String reserveBike(String[] splitMessage) {
        //Message Template: RESERVE|username|stationname
        try {

            if(splitMessage.length != 3)
            {
                return "Reservation error: Invalid number of parameters!";
            }

            String username = splitMessage[1];
            String stationname = splitMessage[2];

            if(!users.containsKey(username)) {
                return "Reservation error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                if(stations.containsKey(stationname)) {
                    Station station = stations.get(stationname);
                    if(station.bikes > station.reservations.size())
                    {
                        station.reservations.add(username);
                        return "Bike reserved for user '" + username + "' in station '" + stationname + "'!";
                    }
                    else {
                        return "No bikes available for reservation in station '" + stationname + "'!";
                    }
                }
                else {
                    return "Station '" + stationname + "' is not available!";
                }
            }
            else {
                return "User '" + username + "' must be logged in to reserve a bike!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while reserving:");
            System.out.println(ex);
            return "Reserving error.";
        }
    }


    private static String stationsWithBikes(String[] splitMessage) {
        //Message Template: STATIONSBIKES|username
        try {

            if(splitMessage.length != 2)
            {
                return "Get stations with bikes error: Invalid number of parameters!";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "Get stations with bikes error: User '" + username + "' is not registered!";
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                HashMap<String, Station> stationsAvailable = new HashMap<String, Station>();
                for (HashMap.Entry<String, Station> station : stations.entrySet())
                {
                    if(station.getValue().bikes > station.getValue().reservations.size()) {
                        stationsAvailable.put(station.getKey(), station.getValue());
                    }
                }
                return "Found '" + stationsAvailable.size() + "' stations with bikes available";
            }
            else {
                return "User '" + username + "' must be logged in to get the stations with bikes!";
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting stations with bikes:");
            System.out.println(ex);
            return "Getting stations with bikes error.";
        }
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
        public int degrees;
        public int minutes;
        public int seconds;

        public Location(int degs, int mins, int secs) {
            degrees = degs;
            minutes = mins;
            seconds = secs;
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
