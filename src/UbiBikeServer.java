import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class UbiBikeServer {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static String message;

    private static HashMap<String, User> users = new HashMap<String, User>();
    private static HashMap<String, User> loggedUsers = new HashMap<String, User>();


    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(4444); // Server socket

        } catch (IOException e) {
            System.out.println("Could not listen on port: 4444");
        }

        System.out.println("UbiBike Server 2 started!");

        while (true) {
            try {

                clientSocket = serverSocket.accept(); // accept the client connection
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader); // get the client message
                message = bufferedReader.readLine();

                System.out.println("Message: " + message);

                String result = parseMessage(message);


                System.out.println("parseMessage result: " + result);

                inputStreamReader.close();
                clientSocket.close();

            } catch (Exception ex) {
                System.out.println("Problem in message reading...");
                System.out.println(ex);
            }
        }
    }

    private static String parseMessage(String message) {
        try
        {
            String result = "";

            // message parameters are separated by the character |
            String[] splitMessage = message.split("|");

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

                default:
                    result = "Invalid Operation: " + splitMessage[0].toString();


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
        //Message Template: REGISTER|username|email|password
        try {
            String result = "";

            if(splitMessage.length != 4)
            {
                return "Register error: Invalid number of parameters";
            }

            String username = splitMessage[1];

            if(users.containsKey(username)) {
                return "Register error: User already exists";
            }

            String email = splitMessage[2];
            String password = splitMessage[3];

            User newUser = new User(username, email, password);
            users.put(username, newUser);

            result = "User '" + username + "' has been created!";

            return result;
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
            String result = "";

            if(splitMessage.length != 3)
            {
                return "Register error: Invalid number of parameters";
            }

            String username = splitMessage[1];

            if(!users.containsKey(username)) {
                return "Register error: User '" + username + "' is not registered!";
            }

            String password = splitMessage[2];


            result = "User '" + username + "' logged in!";

            return result;
        }
        catch (Exception ex) {
            System.out.println("Error while registering the user:");
            System.out.println(ex);
            return "Register error.";
        }
    }

    private static String logout(String[] splitMessage) {
        String result = "";


        return result;
    }

    //endregion



    //region SubClasses

    public static class User {
        public String username;
        public String email;
        public String password;
        public int points;

        public User(String name, String mail, String pass) {
            username = name;
            email = mail;
            password = pass;
            points = 0;
        }

    }

    //endregion

}
