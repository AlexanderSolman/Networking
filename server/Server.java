/*Class to setup a server to listen for connections and help 
 *handle those connections and broadcast each clients message.
 *
 *LAST CHANGE: 31-05-2023
 *AUTHOR: Solman
 *
 **/

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
	private ServerSocket server;
	private Socket client;
	private Connections connect;
	private Thread seperateClient;
	
	//Ensuring that server listens on the desired port
	private static final int PORT = 5000;
	
	//Servers connection pooling to store clients
	private List<Connections> connectionList;

	Server(int port) throws IOException{
		//Initiating the pool
		connectionList = new ArrayList<Connections>();
		//Serversocket listening on port 5000
		server = new ServerSocket(port);
		//Message to display that the server went online
		System.out.println("Server listening on port: " + port);
		
		//Continously ready to accept client connecting and add them 
		//to the storage and create a serperate thread for each
		while(true) {
			client = server.accept();
			connect = new Connections(client);
			connectionList.add(connect);
			seperateClient = new Thread(connect);
			seperateClient.start();
		}
	}

	//Private inner-class to handle connections
	private class Connections implements Runnable{
		
		private Socket client;
		private InputStream in;
		private OutputStream out;
		
		Connections(Socket client){
			this.client = client;
		}
		//overriding thread's run() function
		@Override
		public void run() {
			//Setting up bufferedreader as inputstream and reading the input from the clients
			//Calling sendMessage() to send message to all clients
			try {
				in = client.getInputStream();
				out = client.getOutputStream();
				BufferedReader bf = new BufferedReader(new InputStreamReader(in));
				String buffer;
				while((buffer = bf.readLine()) != null) {
					String message = buffer;
	                sendMessage(message);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		//Send function to send message using outputstream on the socket (each clients listener thread can then pick it up)
		public void sendMessage(String message) {
			for(Connections connection : connectionList) {
				if(connection != null) {
					try {
			        	connection.out.write((message + "\n").getBytes());
						connection.out.flush();
					} catch (IOException e) {
						//e.printStackTrace();
					}
				}
			}
		}
	}
	
	//Servers driver function
	public static void main(String[] args) throws IOException  {
		new Server(PORT);
	}
}
