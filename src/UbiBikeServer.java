import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class UbiBikeServer {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;
    private static ClientMessage clientMessage;

    private static HashMap<String, User> users = new HashMap<String, User>();
    private static HashMap<String, User> loggedUsers = new HashMap<String, User>();
    private static HashMap<String, Station> stations = new HashMap<String, Station>();



    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(4444); // Server socket

        } catch (IOException e) {
            System.out.println("Could not listen on port: 4444");
        }

        System.out.println("UbiBike Server 2 started!");

        // TODO - init stations properly
        Station station1 = new Station("station1", new Location(111, 111, 111), 5);
        Station station2 = new Station("station1", new Location(222, 222, 222), 5);
        stations.put(station1.stationName, station1);
        stations.put(station2.stationName, station2);


        while (true) {
            try {
                //TODO - find a good way to send and receive data
                clientSocket = serverSocket.accept(); // accept the client connection

                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                clientMessage = (ClientMessage)objectInputStream.readObject() ;
                objectInputStream.close();

                System.out.println("Message: " + clientMessage.message);

                ClientMessage response = parseMessage(clientMessage);

                System.out.println("parseMessage result: " + response.message);

                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                objectOutputStream.writeObject(response);
                objectOutputStream.close();

                clientSocket.close();

            } catch (Exception ex) {
                System.out.println("Problem in message reading...");
                System.out.println(ex);
            }
        }
    }

    private static ClientMessage parseMessage(ClientMessage clientmessage) {
        try
        {
            ClientMessage result = new ClientMessage("", null);

            // message parameters are separated by the character |
            String[] splitMessage = clientmessage.message.split("|");

            // 1 - operation is the 1st parameter in the message (REGISTER, LOGIN)
            switch (splitMessage[0]) {
                case "REGISTER":
                    result = register(splitMessage, clientmessage);
                    break;
                case "LOGIN":
                    result = login(splitMessage, clientmessage);
                    break;
                case "LOGOUT":
                    result = logout(splitMessage, clientmessage);
                    break;
                case "SENDPOINTS":
                    result = sendPoints(splitMessage, clientmessage);
                    break;
                case "USERINFO":
                    result = getUserInfo(splitMessage, clientmessage);
                    break;
                case "RESERVE":
                    result = reserveBike(splitMessage, clientmessage);
                    break;
                case "STATIONSBIKES":
                    result = stationsWithBikes(splitMessage, clientmessage);
                    break;

                default:
                    result = new ClientMessage("Invalid Operation: " + splitMessage[0].toString(), null);


            }

            return result;
        }
        catch (Exception ex) {
            System.out.println("Error while parsing the message:");
            System.out.println(ex);

            return new ClientMessage("ERROR", null);
        }
    }



    //region Functionalities

    private static ClientMessage register(String[] splitMessage, ClientMessage message) {
        //Message Template: REGISTER|username|password
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 3) {
                result.message = "Register error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];

            if(users.containsKey(username)) {
                result.message = "Register error: User already exists";
                return result;
            }
            else {
                String password = splitMessage[2];

                User newUser = new User(username, password);
                users.put(username, newUser);

                ClientMessage.message = "User '" + username + "' has been created!";

                return result;
            }


        }
        catch (Exception ex) {
            System.out.println("Error while registering the user:");
            System.out.println(ex);
            return new ClientMessage("Register error.", null);
        }
    }

    private static ClientMessage login(String[] splitMessage, ClientMessage message) {
        //Message Template: LOGIN|username|password
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 3)
            {
                result.message = "Login error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                result.message = "Login error: User '\" + username + \"' is not registered!";
                return result;
            }

            String password = splitMessage[2];

            User activeUser = users.get(username);
            if(activeUser.password.equals(password)) {
                loggedUsers.put(username, activeUser);
                result.message = "User '" + username + "' logged in!";
                result.content = activeUser;
                return result;
            }
            else {
                result.message = "Invalid password for user '" + username + "'!";
                return result;
            }

        }
        catch (Exception ex) {
            System.out.println("Error while logging in the user:");
            System.out.println(ex);
            return new ClientMessage("Login error.", null);
        }
    }

    private static ClientMessage logout(String[] splitMessage, ClientMessage message) {
        //Message Template: LOGOUT|username
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 2)
            {
                result.message = "Login error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                result.message = "Logout error: User '" + username + "' is not registered!";
                return result;
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                loggedUsers.remove(username);
                result.message = "User '" + username + "' logged out!";
                return result;
            }



            return result;
        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return new ClientMessage("Logout error.", null);
        }
    }

    private static ClientMessage sendPoints(String[] splitMessage, ClientMessage message) {
        //Message Template: SENDPOINTS|username1|username2|points
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 4)
            {
                result.message = "Login error: Invalid number of parameters!";
                return result;
            }

            String username1 = splitMessage[1];
            String username2= splitMessage[2];
            int pointsToSend= Integer.parseInt(splitMessage[3]);


            if(!users.containsKey(username1)) {
                result.message = "Send points error: Origin user '" + username1 + "' is not registered!";
                return result;
            }
            if(!users.containsKey(username2)) {
                result.message = "Send points error: Destination user '" + username2 + "' is not registered!";
                return result;
            }


            User user1 = users.get(username1);
            User user2 = users.get(username2);

            if(loggedUsers.containsKey(username1)) {
                if(user1.points < pointsToSend) {
                    result.message = "User '" + username1 + "' doesn't have enough points to send!";
                    return result;
                }
                else {
                    user1.points -= pointsToSend;
                    user2.points += pointsToSend;
                    result.message = "User '" + username1 + "' gave '" + pointsToSend + "' points to user '" + username2 + "'!";
                    return result;

                }
            }
            else {
                result.message = "User '" + username1 + "' must be logged in to send points!";
                return result;
            }
        }
        catch (Exception ex) {
            System.out.println("Error while logging out the user:");
            System.out.println(ex);
            return new ClientMessage("Logout error.", null);

        }
    }

    private static ClientMessage getUserInfo(String[] splitMessage, ClientMessage message) {
        //Message Template: USERINFO|username
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 2)
            {
                result.message = "User info error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                result.message = "User info error: User '" + username + "' is not registered!";
                return result;
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                result.message = "User '" + username + "' info available.";
                result.content = activeUser;
                return result;
            }
            else {
                result.message = "User '" + username + "' must be logged in to get his info!";
                return result;
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting info of the user:");
            System.out.println(ex);
            return new ClientMessage("User info error.", null);
        }
    }

    private static ClientMessage reserveBike(String[] splitMessage, ClientMessage message) {
        //Message Template: RESERVE|username|stationname
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 3)
            {
                result.message = "Reservation error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];
            String stationname = splitMessage[2];

            if(!users.containsKey(username)) {
                result.message = "Reservation error: User '" + username + "' is not registered!";
                return result;
            }


            User activeUser = users.get(username);
            if(loggedUsers.containsKey(username)) {
                if(stations.containsKey(stationname)) {
                    Station station = stations.get(stationname);
                    if(station.bikes > station.reservations.size())
                    {
                        station.reservations.add(username);
                        result.message = "Bike reserved for user '" + username + "' in station '" + stationname + "'!";
                        result.content = station;

                        return result;
                    }
                    else {
                        result.message = "No bikes available for reservation in station '" + stationname + "'!";
                        return result;
                    }
                }
                else {
                    result.message = "Station '" + stationname + "' is not available!";
                    return result;
                }
            }
            else {
                result.message = "User '" + username + "' must be logged in to reserve a bike!";
                return result;
            }
        }
        catch (Exception ex) {
            System.out.println("Error while reserving:");
            System.out.println(ex);
            return new ClientMessage("Reserving error.", null);
        }
    }


    private static ClientMessage stationsWithBikes(String[] splitMessage, ClientMessage message) {
        //Message Template: STATIONSBIKES|username
        try {
            ClientMessage result = new ClientMessage("", null);

            if(splitMessage.length != 2)
            {
                result.message = "Get stations with bikes error: Invalid number of parameters!";
                return result;
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                result.message = "Get stations with bikes error: User '" + username + "' is not registered!";
                return result;
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
                result.message = "Found '" + stationsAvailable.size() + "' stations with bikes available";
                result.content = stationsAvailable;
                return result;
            }
            else {
                result.message = "User '" + username + "' must be logged in to get the stations with bikes!";
                return result;
            }
        }
        catch (Exception ex) {
            System.out.println("Error while getting stations with bikes:");
            System.out.println(ex);
            return new ClientMessage("Getting stations with bikes error.", null);
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

    public static class ClientMessage {
        public static String message;
        public static Object content;

        public ClientMessage(String mes, Object obj) {
            message = mes;
            content = obj;
        }
    }

    //endregion

}
