package de.erik.sockets.multichat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import socketio.Socket;

public class Client extends Application {

	private Socket socket;
	private TextArea textArea;
	private TextField textFieldMsg;
	private TextField textFieldIP;
	private Button btnCon;
	private Button btnEnd;
	private TextField textFieldName;
	private Thread receiver;
	private String userName;
	private String pathToIp;

	private Parent createContent() {
		var root = new VBox(15);
		root.setPrefSize(400, 400);
		root.setPadding(new Insets(10));
		root.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, null, null)));

		btnCon = new Button("verbinden");
		btnCon.setOnAction(e -> verbinden());

		btnEnd = new Button("trennen");
		btnEnd.setDisable(true);
		btnEnd.setOnAction(e -> beenden());

		textFieldIP = new TextField("");
		textFieldIP.setText(getSavedData(0));
		textFieldIP.setPrefWidth(90);
//		textFieldIP.setOnAction(e -> verbinden());

		textFieldName = new TextField("");
		textFieldName.setText(getSavedData(1));
		textFieldName.setPrefWidth(75);
		textFieldName.setPromptText("name");
//		textFieldName.setOnAction(e -> verbinden());

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

	private String getSavedData(int i) {
		List<String> list = null;
		try {
			list = Files.readAllLines(Paths.get(pathToIp), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return i == 1 && list.size() == 1 ? "" : list.size() == 0 ? "fehler" : list.get(i);
	}

	private void verbinden() {
		createSocket();
		textArea.clear();
		if (socket.connect()) {
			saveData();
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
		} else {
			textArea.appendText(
					"Verbindung konnte nicht hergestellt werden.\nServer offline oder Adresse nicht gültig.\n");
		}
	}

	private void saveData() {
		try {
			String data = textFieldIP.getText() + "\n" + textFieldName.getText();
			Files.write(Paths.get(pathToIp), data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
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
		pathToIp = "src/de/erik/sockets/multichat/data.txt";
		stage.setScene(new Scene(createContent()));
		stage.setMinWidth(375);
		stage.setMinHeight(250);
		stage.setTitle("Group Chat");
//		stage.getIcons().add(new Image(getClass().getResource("").toExternalForm())); // TODO: add icon
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
