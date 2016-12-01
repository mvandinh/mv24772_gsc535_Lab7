package chatserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class Chatserver extends Application
{ // Text area for displaying contents 
	private TextArea ta = new TextArea(); 
	private ArrayList<PrintWriter> clientOutputStreams;
	private ArrayList<String> clientNames;
	private ArrayList<Group> groups;

	// Number a client 
	private int clientNo = 0; 

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) { 
		// Create a scene and place it in the stage 
		Scene scene = new Scene(new ScrollPane(ta), 480, 220); 
		primaryStage.setTitle("MultiThreadServer"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 
		new Thread( () -> { 
			try {  // Create a server socket 
				groups = new ArrayList<Group>();
				clientNames = new ArrayList<String>();
				clientOutputStreams = new ArrayList<PrintWriter>();
				ServerSocket serverSocket = new ServerSocket(6000); 
				ta.appendText("MultiThreadServer started\n");  
				//ta.appendText("MultiThreadServer started at " + new Date() + '\n'); 
				// exit command
				primaryStage.setOnCloseRequest( e -> {
					try {
						serverSocket.close();
					} catch (Exception e1) {
						System.out.println("socket closed");
					}
				});
				while (true) { 
					// Listen for a new connection request 
					Socket socket = serverSocket.accept(); 
					
					PrintWriter writer =  new PrintWriter(socket.getOutputStream());
					// Increment clientNo 
					clientNo++; 

					Platform.runLater( () -> { 
						// Display the client number 
						ta.appendText("Starting thread for client " + clientNo + " at " + new Date() + '\n'); 

						// Find the client's host name, and IP address 
						InetAddress inetAddress = socket.getInetAddress();
						ta.appendText("Client " + clientNo + "'s host name is "	+ inetAddress.getHostName() + "\n");
						ta.appendText("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + "\n");	}); 

					// Create and start a new thread for the connection
					new Thread(new HandleAClient(socket)).start();
					clientOutputStreams.add(writer);
				} 
			} 
			catch(IOException ex) { 
				System.err.println(ex);
			}
		}).start();
	}

	// Define the thread class for handling
	class HandleAClient implements Runnable {
		private Socket socket; // A connected socket
		private BufferedReader inputFromClient;
		/** Construct a thread 
		 * @throws IOException */ 
		public HandleAClient(Socket socket) throws IOException { 
			this.socket = socket;
			inputFromClient = new BufferedReader( new InputStreamReader( socket.getInputStream()));
		}
		/** Run a thread */
		public void run() { 
			try {
				// Continuously serve the client
				while (true) { 
					// Receive message from the client 
					String message = inputFromClient.readLine();
					String[] split = message.split("\\s+", 5);
					if(split.length < 3){
						for (PrintWriter writer : clientOutputStreams) {
							writer.println("[GENERAL_CHAT] " + message);
							writer.flush();
						}
						Platform.runLater(() -> { 
							ta.appendText("message received from client: " + message + '\n'); 
						});
					}
					else{
						String name = split[0];
						String colon = split[1];
						String command = split[2];
						if(colon.toLowerCase().equals("/register")){	//special case when client first connects
							ta.appendText("User " + message.split("\\s+")[2] + " has entered the chat.\n"); // user cant enter /register manually
							clientNames.add(message.split("\\s+")[2]);	//adds client name to clientNames list
						}
						else if(command.equals("/listusers")){
							int i = clientNames.indexOf(name);
							clientOutputStreams.get(i).println("--CURRENT USERS--");
							for(String s: clientNames){
								clientOutputStreams.get(i).println(s);
							}
							clientOutputStreams.get(i).flush();
						}
						else if(command.equals("/whisper")){
							String body;
							if (split.length < 5){
								body = "";
							}
							else{
								body = split[4];
							}
							if(clientNames.contains(split[3])){
								int i = clientNames.indexOf(split[3]);
								int j = clientNames.indexOf(split[0]);
								clientOutputStreams.get(i).println("[" + name + "  -->  " + split[3] + "] : " + body);
								clientOutputStreams.get(i).flush();
								clientOutputStreams.get(j).println("[" + name + "  -->  " + split[3] + "] : " + body);
								clientOutputStreams.get(j).flush();
							}
							else{
								int j = clientNames.indexOf(split[0]);
								clientOutputStreams.get(j).println("User " + split[3] + " does not exist.");
								clientOutputStreams.get(j).flush();
							}
						}
						else if(command.equals("/togroup")){
							String body;
							if (split.length < 5){
								body = "";
							}
							else{
								body = split[4];
							}
							boolean found = false;
							Group currentgroup = null;
							for(Group g: groups){
								if (g.title.equals(split[3])){
									currentgroup = g;
									found = true;
								}
							}
							if (found){
								for(String s: currentgroup.members){
									clientOutputStreams.get(clientNames.indexOf(s)).println("[" + currentgroup.title + "] " + name + " : " + body);
									clientOutputStreams.get(clientNames.indexOf(s)).flush();
								}
							}
							else {
								clientOutputStreams.get(clientNames.indexOf(name)).println("Group " + split[3] + " does not exist");
								clientOutputStreams.get(clientNames.indexOf(name)).flush();
							}
						}
						else if(command.equals("/creategroup")){
							boolean alreadyexists = false;
							for(Group c: groups){
								if (split[3].equals(c.title)){
									alreadyexists = true;
								}
							}
							if (alreadyexists == false){
								ArrayList<String> memberlist = new ArrayList<String>();
								String[] names = split[4].split(";");
								memberlist.add(name);
								for(String s: names){
									if (clientNames.contains(s) && !memberlist.contains(s)){
										memberlist.add(s);
										clientOutputStreams.get(clientNames.indexOf(s)).println("You've been added to group: " + split[3]);
										clientOutputStreams.get(clientNames.indexOf(s)).flush();
									}
								}
								groups.add(new Group(split[3], memberlist));
								int i = clientNames.indexOf(name);
								clientOutputStreams.get(i).println("Created group with the following users:");
								for(String s: memberlist){
									clientOutputStreams.get(i).println(s);
								}
							}
							else{
								clientOutputStreams.get(clientNames.indexOf(name)).println("A group with the name " + split[3] + " already exists");
							}
							clientOutputStreams.get(clientNames.indexOf(name)).flush();
						}
						else if(command.equals("/help")){
							clientOutputStreams.get(clientNames.indexOf(name)).println("/listusers\n/whisper [username] [message]\n/creategroup [groupname] [user1];[user2];[user3];...[userZ]\n/togroup [groupname] [message]");
							clientOutputStreams.get(clientNames.indexOf(name)).flush();
						}
						else{
							for (PrintWriter writer : clientOutputStreams) {
								writer.println("[GENERAL_CHAT] " + message);
								writer.flush();
							}
							Platform.runLater(() -> { 
								ta.appendText("message received from client: " + message + '\n'); 
							});
						}
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	public class Group{
		String title;
		ArrayList<String> members;
		
		public Group(String groupname, ArrayList<String> memberlist){
			title = groupname;
			members = memberlist;
		}
	}
	public static void main(String[] args) {
		launch(args);
	}
}