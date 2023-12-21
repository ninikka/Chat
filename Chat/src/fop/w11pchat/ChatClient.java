package fop.w11pchat;

import java.net.ConnectException;
import java.net.Socket;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient implements Runnable{

    private Socket sock;
    private BufferedReader input;
    private PrintWriter output;



    boolean done=false;


    @Override
    public void run() {
        try{
            sock = new Socket("localhost", 3000);
            output = new PrintWriter(sock.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();

            String inMessage;
            while((inMessage = input.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done=true;
        try {
            input.close();
            output.close();
            if(!sock.isClosed()){
                sock.close();
            }
        }
        catch (IOException e){
        }
    }
    public class InputHandler implements Runnable {


        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {

                    String message = in.readLine();
                    if (message.equals("LOGOUT")) {
                        output.println(message);

                        in.close();
                        shutdown();
                    } else {
                        output.println(message);
                    }
                }

            } catch (IOException ioException) {
                shutdown();
            }
        }



    }

    public static void main(String[] args) throws IOException {
        ChatClient client1 = new ChatClient();
        client1.run();
    }

}