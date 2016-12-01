package chatclient;
import java.io.*;
import java.net.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

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
				socket = new Socket(connectIP, 6000); // Create a socket to connect to the server
				} catch (IOException ex) {
					l1.setText("ENTER SERVER IP: [CONNECTION FAILED]");
					connected = false;
			}
			if(connected){
				try{
					toServer = new PrintWriter(socket.getOutputStream());
					// Create an input stream to receive data from the server 
					fromServer = new InputStreamReader(socket.getInputStream());
					toServer.println(clientName + ": /register " + clientName); 				
					toServer.flush();
					chatroom(primaryStage);
				} catch (IOException ex) {
					ta.appendText(ex.toString() + '\n');
				}
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
			String message = clientName + " : " + tf.getText().trim();
			tf.setText("");
			String[] split = message.split("\\s+");
			if(split[2].equals("/whisper") && (split.length < 4)){
				ta.appendText("Must specify a user to whisper to \n");
			}
			else{
				// Send the message to the server 
				toServer.println(message); 				
				toServer.flush();
				}
			});
			// Create a socket to connect to the server @SuppressWarnings("resource") 
			// Create an output stream to send data to the server 
			reader = new BufferedReader(fromServer);
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
	}
	class IncomingReader implements Runnable {
		String message;
		public void run() {
			while (true) {
				try {
					message = reader.readLine();
					ta.appendText(message + "\n");
					playSound();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	} 
	public void playSound() {
	    try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("./notification.wav").getAbsoluteFile());
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start();
	    } catch(Exception ex) {
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	    }
	}
	public static void main(String[] args) {
		launch(args);
	}
}