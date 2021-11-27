package de.erik.sockets.multichat;

import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import socketio.Socket;

public class EchoClient extends Application {

	private Socket socket;
	private TextArea textArea;
	private TextField textFieldMsg;
	private TextField textFieldIP;
	private Button btnCon;
	private Button btnEnd;
	private Button btnSave;
	private Thread receiver;

	public EchoClient() throws Exception {
		socket = new Socket("localhost", 1234);
	}

	private Parent createContent() {
		var root = new VBox(15);
		root.setPrefSize(400, 400);
		root.setPadding(new Insets(10));

		btnCon = new Button("verbinden");
		btnCon.setOnAction(e -> verbinden());

		btnEnd = new Button("trennen");
		btnEnd.setDisable(true);
		btnEnd.setOnAction(e -> beenden());

		textFieldIP = new TextField("localhost"); // TODO
		textFieldIP.setPrefWidth(100);
//		textFieldMsg.setOnAction(e -> {});

		btnSave = new Button("save IP");
//		btnSave.setonAction(e -> {});

		textArea = new TextArea();
		textArea.setPrefHeight(999);
		textArea.setEditable(false);
		textArea.setPromptText("disconneted");

		textFieldMsg = new TextField();
		textFieldMsg.setEditable(false);
		textFieldMsg.setPromptText("enter message");
		textFieldMsg.setOnAction(e -> send());

		root.getChildren().addAll(new HBox(15, btnCon, btnEnd, textFieldIP, btnSave), textArea, textFieldMsg);
		return root;
	}

	private void verbinden() { // kann nach zweiter verbindung nicht mehr readen
		if (socket.connect()) {
			textArea.clear();
			textArea.appendText("Verbindung hergestellt. Chat offen.\n");
			btnCon.setDisable(true);
			btnEnd.setDisable(false);
			textFieldMsg.setEditable(true);
			textFieldIP.setEditable(false);
			receiver = new Thread(() -> {
				do {
					try {
						String msg = "\n" + socket.readLine();
						textArea.appendText(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} while (true);
			});
			receiver.start();
		} else {
			textArea.appendText("Verbindung konnte nicht hergestellt werden. Server offline\n");
		}
	}

	private void send() {
		String msg = textFieldMsg.getText();
		textFieldMsg.clear();
		try {
			socket.write(msg + "\n");
			textArea.appendText("\nDu -> " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void beenden() {
		receiver.stop();
		try {
			socket.write("~+*#\n");
//			Thread.sleep(50); // ??
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		btnCon.setDisable(false);
		btnEnd.setDisable(true);
		textFieldMsg.setEditable(false);
		textFieldIP.setEditable(true);
		textArea.appendText("\n\nDu hast den Chat verlassen\n");
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setScene(new Scene(createContent()));
		stage.setMinWidth(360);
		stage.setMinHeight(250);
		stage.setTitle("Group Chat");
		stage.getIcons().add(new Image(getClass().getResource("").toExternalForm())); // TODO
		stage.setOnCloseRequest(e -> {
			if (btnCon.isDisabled())
				beenden();
		});
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
