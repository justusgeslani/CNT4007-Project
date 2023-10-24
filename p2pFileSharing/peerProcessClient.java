import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class peerProcessClient {

	// socket connect to the server
	Socket requestSocket;

	// stream write to the socket
	ObjectOutputStream out;

	// stream read from the socket
 	ObjectInputStream in;

	//message send to the server
	String message;

	// capitalized message read from the server
	String MESSAGE;

	// The client will be using this host name
	private String hostName;

	// The client will be listening on this port number
	private int cPort = 8000;


	// Default constructor for peer process client
	public peerProcessClient() {

	}

	// Constructor for peer process client, uses host name and listening port number
	public peerProcessClient(String host, int lPort) {

		this.hostName = host;
		this.cPort = lPort;

	}

	// Run method for running peer process client
	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket(hostName, cPort);
			System.out.println("Connected to " + hostName + " in port " + cPort);
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server
				MESSAGE = (String)in.readObject();
				//show the message to the user
				System.out.println("Receive message: " + MESSAGE);
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}


}
