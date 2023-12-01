import java.net.*;
import java.io.*;

public class PeerProcessClient {

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
	public PeerProcessClient() { }

	public PeerProcessClient() {

	}

	// Constructor for peer process client, uses host name and listening port number
	public PeerProcessClient(String host, int lPort) {
		this.hostName = host;
		this.cPort = lPort;
	}

	// Run method for running peer process client
	void run()
	{
		for
		try{
			//create a socket to connect to the server
			requestSocket = new Socket(this.hostName, this.cPort);
			System.out.println("Connected to " + this.hostName + " in port " + this.cPort);
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

	/**
	 * A handler thread class for clients.
	 */
	private static class ClientHandler extends Thread {
		private String message;    //message to send to server
		private String MESSAGE;    //uppercase message received by server
		private Socket connection;
		private ObjectInputStream in;    //stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private int no;        //The index number of the server

		public ClientHandler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		public void run() {
			try{
				//initialize inputStream and outputStream
				out = new ObjectOutputStream(this.connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(this.connection.getInputStream());

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
					this.connection.close();
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
			}
		}

		//send a message to the output stream
		void sendMessage(String msg) {
			try {
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
}
