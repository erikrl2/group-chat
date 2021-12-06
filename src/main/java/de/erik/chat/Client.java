package de.erik.chat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import de.erik.socketio.Socket;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Client extends Application {

	private Socket socket;
	private TextArea textArea;
	private TextArea messageField;
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
		root.setBackground(new Background(new BackgroundFill(Color.SKYBLUE, null, null)));

		btnCon = new Button("verbinden");
		btnCon.setMinWidth(81);
		btnCon.setFont(Font.font(14));
		btnCon.setOnAction(e -> verbinden());

		btnEnd = new Button("trennen");
		btnEnd.setDisable(true);
		btnEnd.setMinWidth(68);
		btnEnd.setFont(Font.font(14));
		btnEnd.setOnAction(e -> beenden());

		textFieldIP = new TextField("");
		textFieldIP.setText(getSavedData(0));
		textFieldIP.setPrefWidth(105);
		textFieldIP.setFont(Font.font(14));

		textFieldName = new TextField("");
		textFieldName.setText(getSavedData(1));
		textFieldName.setMinWidth(65);
		textFieldName.setPrefWidth(75);
		textFieldName.setPromptText("name");
		textFieldName.setFont(Font.font(14));

		textArea = new TextArea();
		textArea.setPrefHeight(999);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setFont(Font.font(15));
		textArea.setPromptText("disconneted");

		messageField = new TextArea();
		messageField.setEditable(false);
		messageField.setWrapText(true);
		messageField.setMinHeight(75);
		messageField.setFont(Font.font(15));
		messageField.setPromptText("enter message");
		messageField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				messageField.setText(messageField.getText().strip());
				if (!messageField.getText().equals("") && btnCon.isDisabled())
					send();
			}
		});

		root.getChildren().addAll(new HBox(15, btnCon, btnEnd, textFieldIP, textFieldName), textArea, messageField);
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
		textArea.clear();
		createSocket();
		if (socket.connect()) {
			saveData();
			textArea.appendText("Verbindung hergestellt. Chat offen.");
			btnCon.setDisable(true);
			btnEnd.setDisable(false);
			messageField.setEditable(true);
			textFieldIP.setEditable(false);
			textFieldName.setEditable(false);
			userName = textFieldName.getText().equals("") ? "user" : textFieldName.getText();
			try {
				socket.write("\n" + replaceUmlaute(userName) + " -> [ist beigetreten]\n");
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
			Files.write(Paths.get(pathToIp), replaceUmlaute(data).getBytes());
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
		String msg = messageField.getText();
		messageField.clear();
		String full = "\n" + userName + " -> " + msg;
		try {
			socket.write(replaceUmlaute(full) + "\n");
			textArea.appendText("\nDu -> " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String replaceUmlaute(String s) {
		return s.replaceAll("ä", "ae").replaceAll("ü", "ue").replaceAll("ö", "oe");
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
		messageField.setEditable(false);
		messageField.clear();
		textFieldIP.setEditable(true);
		textFieldName.setEditable(true);
		textArea.appendText("\n\nDu hast den Chat verlassen.\n");
	}

	@Override
	public void start(Stage stage) throws Exception {
		String s = System.getProperty("user.dir");
		pathToIp = s + "/data.txt";
		stage.setScene(new Scene(createContent()));
		stage.setMinWidth(350);
		stage.setMinHeight(350);
		stage.setTitle("Group Chat");
		stage.getIcons().add(new Image(getResource("icons/chat.png")));
		stage.setOnCloseRequest(e -> {
			if (btnCon.isDisabled())
				beenden();
		});
		stage.show();
	}

	private InputStream getResource(String s) {
		var input = getClass().getResourceAsStream("/resources/" + s);
		if (input == null) {
			input = getClass().getResourceAsStream("/" + s);
		}
		return input;
	}

	public static void main(String[] args) {
		launch(args);
	}

}
