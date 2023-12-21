package fop.w11pchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable {

    private   ServerSocket server;
    private static ArrayList<ConnectionHandler> connections;
    private boolean done;
    private ExecutorService pool;


    public ChatServer (Integer k) throws IOException {
        server =new ServerSocket(k);
        connections=new ArrayList<>();
    }
    public ChatServer() throws IOException {
        connections=new ArrayList<>();
    }

    public ServerSocket getServer() {
        return server;
    }

    @Override
    public void run() {
        try {
            ChatServer chatServer = new ChatServer(3000);
            pool= Executors.newCachedThreadPool();
            while (!chatServer.server.isClosed()) {

                try {
                    System.out.println(LocalTime.now()+": Server is waiting on Port " + chatServer.getServer().getLocalPort());
                    Socket client = chatServer.getServer().accept();

                    ConnectionHandler handler = new ConnectionHandler(client);
                    connections.add(handler);
                    pool.execute(handler);
                } catch (IOException ioException) {
                    shutdown();
                }
            }
    } catch (IOException e) {
        shutdown();
    }
    }


    public void broadcast(String mess) {
        for (ConnectionHandler kon : connections) {
            if (kon != null) {
                kon.sendMessage(mess);
            }
        }
    }

    public void shutdown(){
        pool.shutdown();
        if(server !=null&&!server.isClosed()) {

            try {
                server.close();
            } catch (IOException e) {
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdownCH();
            }
        }

    }
    HashMap<String ,String> clientTime =new HashMap<>();
    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader input;
        private PrintWriter output;
        private String username;

        public String message;


        public ConnectionHandler(Socket client) {
            this.client = client;
        }


        public void toAll(String message){
            for (ConnectionHandler connection : connections) {
                if (connection != this) {
                    connection.sendMessage(message);
                }
            }
        }

        public void restricted() throws IOException {
            while (message.contains("nigga") || message.contains("faggot") || message.contains("bitch") || message.contains("pidarast") || message.contains("Putin") || message.contains("fuck") || message.contains("retard") || message.contains("Hitler") || message.contains("SovietUnion") || message.contains("motherfucker") || message.contains("slut") || message.contains("cunt") || message.contains("dick") || message.contains("penis") || message.contains("cock") || message.contains("vagina") || message.contains("cuck") || message.contains("cum") || message.contains("chingchong") || message.contains("sucker") || message.contains("lightskin") ||message.contains("bdsm") || message.contains("cbt") || message.contains("fag") || message.contains("semen") || message.contains("anal") || message.contains("anus") || message.contains("asshole") || message.contains("ass") || message.contains("pubes") || message.contains("pubic") || message.contains("tits") || message.contains("boobs") || message.contains("breasts") || message.contains("nips") || message.contains("nipples")) {
                output.println("Your message contains slur." +
                        "Please enter another message");
                message = input.readLine();
            }
        }

        @Override
        public void run() {


            try {

                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));


                output.println("Enter the username:");
                username = input.readLine();
                while (username.isBlank()) {
                    output.println("Your username is blank.\n" +
                            "Please enter the username.");
                    username = input.readLine();
                }

                while (username.contains("nigga") || username.contains("faggot") || username.contains("bitch") || username.contains("pidarast") || username.contains("Putin")) {
                    output.println("Your username contains slur." +
                            "Please enter another username");
                    username = input.readLine();
                }

                LocalTime connectTime = LocalTime.now();
                System.out.println(connectTime+" *** " + username + " has joined the chat room *** ");


                clientTime.put(username,connectTime.toString());
                output.println(connectTime +": connection accepted " + client.getInetAddress().getHostAddress()+": "+ client.getLocalPort());

                broadcast(connectTime+" ***" + username + " has joined the chat room ***");
                output.println("""
                        Hello! welcome to the chatroom!
                        Instructions :
                        1. type the message to send broadcast to all active clients.
                        2. Type '@username <space> 'your message' without quotes to send message to desired client.
                        3. Type 'WHOIS' without quotes to see list of active clients.
                        4. Type 'LOGOUT' without quotes to logoff from server.
                        5. Type 'PINGU' without quotes to request a random penguin fact.
                        Important rule: no slurs or insults are allowed""");

                while ((message = input.readLine()) !=null){

                    restricted();

                    if(message.equals("WHOIS")) {
                        output.println(">");

                        output.println("List Of Users Connected at " + LocalTime.now());

                        int clientNum = 1;
                        for (int i = 0; i < connections.size(); i++) {

                            if (connections.get(i).username != null) {
                                output.println("> " + clientNum + ") " + connections.get(i).username + " since "
                                        + clientTime.get(connections.get(i).username));
                                clientNum++;
                            }
                        }
                    }

                        else if (message.startsWith("@")) {

                        String [] messageArr = message.split(" ");
                        for(int i=0 ; i < connections.size() ; i++){
                            if (("@"+connections.get(i).username).equals(messageArr[0])){

                                    connections.get(i).sendMessage(LocalTime.now()+" Direct Message from "+this.username +"  : "+
                                            message.replace(messageArr[0]+" ",""));

                                    break;
                                }
                        }
                    }
                    else if (message.equals("LOGOUT")) {
                        System.out.println(LocalTime.now() +" * "+ username +" has left the chat room *");
                        shutdownCH();

                        connections.remove(this);
                        broadcast(LocalTime.now() + " *** "+ username +" left the chat *** ");

                    }
                    else if (message.startsWith("PINGU")){
                        String[] facts = new String[7];
                        facts[0] = "A penguins black and white colouring is called counter-shading.";

                        facts[1] = """
                                The black and white “tuxedo” look donned by most penguin species is a clever camouflage called countershading." +
                                                                "When swimming, the black on their backs helps them blend in with the darkness of the ocean from predators viewing from above." +
                                                                "Their white bellies help them blend in with the bright surface of the ocean when viewed by predators and prey from below.""";

                        facts[2] = """
                                Penguins are birds, and birds don’t have teeth.
                                Still, it can be scary the first time you see the inside of a penguin’s mouth,
                                complete with serrated ridges on the top of the mouth that can be used to break up food.
                                """;

                        facts[3] = "Gentoo Penguins are the fastest of all penguin species!";

                        facts[4] = "The oldest penguin fossils are 62 million years old";

                        facts[5] = "Penguins poop every 20 minutes";

                        facts[6] = "Penguins dive to catch their food. The Emperor penguin can dive down to 530m,\n" +
                                "Gentoo penguins can dive 200m and King penguins can dive around 350m deep!";

                        output.println(getRandom(facts));

                    }
                    else {
                        toAll(username +" : "+message);
                        output.println("You Broadcasted message : "+message+"\n");
                    }
                }


            } catch (IOException ioException) {
            }
        }

        public String getRandom(String[] array) {
            int rnd = new Random().nextInt(array.length);
            return array[rnd];
        }

        public void sendMessage(String message) {
            output.println(message);
        }

        public void shutdownCH() {

            try {
                input.close();
                output.close();
                if(!client.isClosed()){
                    client.close();
                }
            } catch (IOException e) {
            }
        }
    }


    public static void main(String[] args) {
        try {
            ChatServer server=new ChatServer();
            server.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (NullPointerException nullPointerException) {
        }

    }
}