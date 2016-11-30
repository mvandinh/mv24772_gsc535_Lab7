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
	// IO streams 
	PrintWriter toServer = null; 
	InputStreamReader fromServer = null;
	BufferedReader reader = null;
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
		TextArea ta = new TextArea(); 
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
			try {
				if (!userNameSet) {
					userName = tf.getText().trim();
					userNameSet = true;
					paneForTextField.setLeft(new Label("Message: "));
					tf.setText("");
				} else {
					String message = userName + ": " + tf.getText().trim();
					tf.setText("");
					// Send the message to the server 
					toServer.println(message); 
					toServer.flush();
					String chat = reader.readLine();
					ta.appendText(chat + "\n");
				}
			} catch (IOException ex) { System.err.println(ex); }
		}); try {
			// Create a socket to connect to the server @SuppressWarnings("resource") 
			Socket socket = new Socket("localhost", 8000);
			// Create an output stream to send data to the server 
			toServer = new PrintWriter(socket.getOutputStream());
			// Create an input stream to receive data from the server 
			fromServer = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(fromServer);
			} catch (IOException ex) {
				ta.appendText(ex.toString() + '\n');
			}
		} public static void main(String[] args) {
			launch(args);
	}
}
