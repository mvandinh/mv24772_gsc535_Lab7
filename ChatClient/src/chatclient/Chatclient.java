package chatclient;
import java.io.*;
import java.net.*;

import javafx.application.Application; 
import javafx.geometry.Insets; 
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label; 
import javafx.scene.control.ScrollPane; 
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField; 
import javafx.scene.layout.BorderPane; 
import javafx.stage.Stage;

public class Chatclient extends Application {
	TextArea ta;
	PrintWriter toServer; 
	InputStreamReader fromServer;
	BufferedReader reader;
	boolean connected;
	Socket socket;
	static String clientName = null;
	static String connectIP = null;
	@Override
	public void start(Stage primaryStage) {
			BorderPane pane1 = new BorderPane();
			TextField username = new TextField();
			Label l1 = new Label("ENTER USERNAME:");
			pane1.setTop(l1);
			pane1.setCenter(username);
			Scene scene1 = new Scene(pane1, 250, 60);
			primaryStage.setTitle("Client Log-In");
			primaryStage.setScene(scene1);
			primaryStage.show();
			username.setOnAction(e -> {
				clientName = username.getText().trim();
				if (clientName.matches(".*\\s+.*")){
					username.clear();
					l1.setText("ENTER USERNAME: [SPACES NOT ALLOWED]");
				}
				else{
					ipenter(primaryStage);
				}
			});
	}

	public void ipenter(Stage primaryStage){
		BorderPane pane1 = new BorderPane();
		TextField username = new TextField();
		Label l1 = new Label("ENTER SERVER IP:");
		pane1.setTop(l1);
		pane1.setCenter(username);
		Scene scene1 = new Scene(pane1, 250, 60);
		primaryStage.setTitle("Client Log-In");
		primaryStage.setScene(scene1);
		primaryStage.show();
		username.setOnAction(e -> {
			connectIP = username.getText().trim();
			try {
				connected = true;	
				socket = new Socket(connectIP, 8014); // Create a socket to connect to the server
				} catch (IOException ex) {
					l1.setText("ENTER SERVER IP: [CONNECTION FAILED]");
					connected = false;
			}
			if(connected){
				chatroom(primaryStage);
			}
		});
	}
	
	public void chatroom(Stage primaryStage){
		// panel to hold the the text field and the input
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Message: "));
		TextField tf = new TextField();
		tf.setAlignment(Pos.BOTTOM_LEFT);
		paneForTextField.setCenter(tf);
		BorderPane mainPane = new BorderPane(); 
		// Text area to display contents
		ta = new TextArea();
		ta.setEditable(false);
		mainPane.setTop(new ScrollPane(ta)); 
		mainPane.setBottom(paneForTextField);
		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 480, 220);
		primaryStage.setTitle("Chat Client [" + clientName + "]");
		// Set the stage title 
		primaryStage.setScene(scene); 
		// Place the scene in the stage 
		primaryStage.show(); 
		// Display the stage
		tf.setOnAction(e -> {
			String message = clientName + ": " + tf.getText().trim();
			tf.setText("");
			// Send the message to the server 
			toServer.println(message); 				
			toServer.flush();
			});
			try {
			// Create a socket to connect to the server @SuppressWarnings("resource") 
			// Create an output stream to send data to the server 
			toServer = new PrintWriter(socket.getOutputStream());
			// Create an input stream to receive data from the server 
			fromServer = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(fromServer);
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
			} catch (IOException ex) {
				ta.appendText(ex.toString() + '\n');
			}
	}
	class IncomingReader implements Runnable {
		String message;
		public void run() {
			while (true) {
				try {
					message = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ta.appendText(message + "\n");
			}
		}
	} 
	public static void main(String[] args) {
		launch(args);
	}
}