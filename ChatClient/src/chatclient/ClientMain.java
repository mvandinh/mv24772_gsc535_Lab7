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

public class ClientMain extends Application {
	// variables
	String userName = new String();
	boolean userNameSet = false;
	//JavaFX
	TextArea ta;
	// IO streams 
	PrintWriter toServer; 
	InputStreamReader fromServer;
	BufferedReader reader;
	@Override
	public void start(Stage primaryStage) {
		// panel to hold the the text field and the input
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Set your username: "));
		TextField tf = new TextField();
		tf.setAlignment(Pos.BOTTOM_RIGHT);
		paneForTextField.setCenter(tf);
		BorderPane mainPane = new BorderPane(); 
		// Text area to display contents
		ta = new TextArea();
		ta.setEditable(false);
		mainPane.setTop(new ScrollPane(ta)); 
		mainPane.setBottom(paneForTextField);
		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 480, 220);
		primaryStage.setTitle("Client");
		// Set the stage title 
		primaryStage.setScene(scene); 
		// Place the scene in the stage 
		primaryStage.show(); 
		// Display the stage
		tf.setOnAction(e -> {
			if (!userNameSet) {
				userName = tf.getText().trim();
				userNameSet = true;
				paneForTextField.setLeft(new Label("Message: "));
				tf.setText("");
				primaryStage.setTitle(userName + " Messenger");
			} else {
				String message = userName + ": " + tf.getText().trim();
				tf.setText("");
				// Send the message to the server 
				toServer.println(message); 
				toServer.flush();
			}
		}); try {
			// Create a socket to connect to the server @SuppressWarnings("resource") 
			Socket socket = new Socket("localhost", 8018);
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
		
		} class IncomingReader implements Runnable {
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
		} public static void main(String[] args) {
			launch(args);
	}
}

