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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import socketio.Socket;

public class EchoClient extends Application {

	private Socket socket;
	private TextArea textArea;
	private TextField textFieldMsg;
	private TextField textFieldIP;
	private Button btnCon;
	private Button btnEnd;
	private TextField textFieldName;
	private Thread receiver;
	private String userName;

	private Parent createContent() {
		var root = new VBox(15);
		root.setPrefSize(400, 400);
		root.setPadding(new Insets(10));

		btnCon = new Button("verbinden");
		btnCon.setOnAction(e -> verbinden());

		btnEnd = new Button("trennen");
		btnEnd.setDisable(true);
		btnEnd.setOnAction(e -> beenden());

		textFieldIP = new TextField("localhost");
//		textFieldIP.setText(""); // TODO: read from .txt
		textFieldIP.setPrefWidth(75);
		textFieldIP.setOnAction(e -> verbinden());

		textFieldName = new TextField();
//		textFieldName.setText(""); // TODO: read from .txt
		textFieldName.setPrefWidth(75);
		textFieldName.setPromptText("name");
		textFieldName.setOnAction(e -> verbinden());

		textArea = new TextArea();
		textArea.setPrefHeight(999);
		textArea.setEditable(false);
		textArea.setFont(Font.font(14));
		textArea.setPromptText("disconneted");

		textFieldMsg = new TextField();
		textFieldMsg.setEditable(false);
		textFieldMsg.setFont(Font.font(14));
		textFieldMsg.setPromptText("enter message");
		textFieldMsg.setOnAction(e -> send());

		root.getChildren().addAll(new HBox(15, btnCon, btnEnd, textFieldIP, textFieldName), textArea, textFieldMsg);
		return root;
	}

	private void verbinden() {
		createSocket();
		textArea.clear();
		if (socket.connect()) {
			textArea.appendText("Verbindung hergestellt. Chat offen.");
			btnCon.setDisable(true);
			btnEnd.setDisable(false);
			textFieldMsg.setEditable(true);
			textFieldIP.setEditable(false);
			textFieldName.setEditable(false);
			userName = textFieldName.getText().equals("") ? "user" : textFieldName.getText();
			try {
				socket.write("\n" + userName + " -> [ist beigetreten]\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			receiver = new Thread(() -> {
				do {
					try {
						String msg = socket.readLine();
						textArea.appendText("\n" + msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} while (true);
			});
			receiver.start();
			// TODO: IP und Name in .txt speichern
		} else {
			textArea.appendText(
					"Verbindung konnte nicht hergestellt werden.\nServer offline oder Adresse nicht gültig.\n");
		}
	}

	private void createSocket() {
		try {
			socket = new Socket(textFieldIP.getText(), 5731);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void send() {
		String msg = textFieldMsg.getText();
		textFieldMsg.clear();
		String full = "\n" + userName + " -> " + msg;
		// TODO: umlaute in ae,ue,oe umwandeln
		try {
			socket.write(full + "\n");
			textArea.appendText("\nDu -> " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void beenden() {
		receiver.stop();
		try {
			socket.write("over\n");
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		btnCon.setDisable(false);
		btnEnd.setDisable(true);
		textFieldMsg.setEditable(false);
		textFieldIP.setEditable(true);
		textFieldName.setEditable(true);
		textArea.appendText("\n\nDu hast den Chat verlassen.\n");
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setScene(new Scene(createContent()));
		stage.setMinWidth(360);
		stage.setMinHeight(250);
		stage.setTitle("Group Chat");
		stage.getIcons().add(new Image(getClass().getResource("").toExternalForm())); // TODO: add icon
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
