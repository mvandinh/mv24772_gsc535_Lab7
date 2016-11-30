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
				clientOutputStreams = new ArrayList<PrintWriter>();
				ServerSocket serverSocket = new ServerSocket(8000); 
				ta.appendText("MultiThreadServer started at " + new Date() + '\n'); 
				// exit command
				primaryStage.setOnCloseRequest( e -> {
					try {
						serverSocket.close();
						Platform.exit();
						System.exit(0);
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
					for (PrintWriter writer : clientOutputStreams) {
						writer.println(message);
						writer.flush();
					}
					Platform.runLater(() -> { 
						ta.appendText("message received from client: " + message + '\n'); 
					});
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}