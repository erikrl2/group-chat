package de.erik.sockets.multiechoserver;

import java.io.IOException;
import java.util.Scanner;

import socketio.Socket;

public class EchoClient {

	private String host;
	private Socket cSocket;
	private int port;

	public EchoClient() {
		this.host = "localhost";
		this.port = 1234;
		try {
			this.cSocket = new Socket(host, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		verbinden();
	}

	public void verbinden() {
		if (cSocket.connect()) {
			System.out.println("Verbindung hergestellt");
			kommunizieren();
		} else {
			System.out.println("Verbindung konnte nicht hergestellt werden");
			System.exit(1);
		}
	}

	@SuppressWarnings("deprecation")
	public void kommunizieren() {
		var read = new Thread(() -> {
			do {
				try {
					// Empfangen und Ausgeben
					System.out.print("\n" + cSocket.readLine());
					System.out.print("\n\nEingabe: ");

				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (true);
		});
		read.start();

		Scanner scan = new Scanner(System.in);
		String msg = "";
		System.out.print("\nEingabe: ");
		do {
			try {
				// Eingabe und Senden
				msg = scan.nextLine();
				cSocket.write(msg + "\n");

			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!msg.endsWith("over"));
		read.stop();
		scan.close();
		beenden();
	}

	public void beenden() {
		try {
			System.out.println("\nDu hast den Chat verlassen");
			cSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new EchoClient();
	}

}
