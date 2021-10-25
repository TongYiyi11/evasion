import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Client {
    // Initialize socket and input output streams
    private Socket socket            = null;
    private BufferedReader input   	 = null;
    private PrintWriter out     	 = null;
    boolean hunter = false;

    public Client(String address, int port)
    {
        // Establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from socket
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // sends output to the socket
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }

    }

    public static void main(String args[]) throws IOException
    {
        int portNumber;
        if (args.length == 0) {
            System.out.println("Connecting on default port 9000");
            portNumber = 9000;
        }
        else {
            portNumber = Integer.parseInt(args[0]);
            System.out.println(String.format("Connecting on specified port %d", portNumber));
        }
        Client client = new Client("127.0.0.1", portNumber);
        client.run();
    }

    public void run() throws IOException {
        String state;            // state of the game given by server
        String tosend;           // move to be sent to server

        while(true){
            state = input.readLine();    // get state
            System.out.println(state);   // for test
            tosend = null;

            if(state.equals("done")) {
                break;
            }else if(state.equals("hunter")){
                hunter = true;
            }else if(state.equals("prey")){
                hunter = false;
            }else if(state.equals("sendname")){
                // TODO replace with your team name
                tosend = "Java Client";

            }else if(state.startsWith("error")){
                // TODO handle corresponding error
                System.out.println("error");

            }else{
                tosend = sendmove(state);
            }

            // send move to server and print
            if(tosend != null){
                System.out.println("sending: " + tosend);
                out.println(tosend);
            }

//            try {
//                Thread.sleep(100);
//            }
//            catch(InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
        }

    }

    public String sendmove(String state){
        // TODO Given the state of the game, return formatted move to server.
        // Please replace below with your own algorithm
        // Random player
        String[] data = state.split(" ");
        String tosend = "";
        Random r = new Random();
        if(hunter){
            int x = r.nextInt(50);
            String wall = "0";
            if(x < 4){
                wall = Integer.toString(x + 1);
            }
            if(r.nextInt(80) == 0){
                wall = "0 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20";
            }

            tosend = data[1] + " " + data[2] + " " + wall;
        }else{
            int x = r.nextInt(3) - 1;
            int y = r.nextInt(3) - 1;

            tosend = data[1] + " " + data[2] + " " + x + " " + y;
        }

        return tosend;
    }
}
