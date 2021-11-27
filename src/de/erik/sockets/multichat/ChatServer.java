package de.erik.sockets.multichat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import socketio.ServerSocket;
import socketio.Socket;

public class ChatServer {

	private int port;
	private ServerSocket sSocket;
	public static List<Socket> meineSockets;
	private List<Thread> meineThreads;

	public ChatServer() throws IOException {
//		System.setProperty("fazecast.jSerialComm.appid", "YOUR_APPLICATION_IDENTIFIER");
		this.port = 1234;
		this.sSocket = new ServerSocket(port);
		meineThreads = new ArrayList<>();
		meineSockets = new ArrayList<>();
		var update = updateThreads();
		int i = 0;
		System.out.println("Waiting for Clients...");
		do {
			var socket = sSocket.accept();
			var t = new Thread(new Kommunikator(i, socket));
			meineSockets.add(socket);
			meineThreads.add(t);
			t.start();
			if (i == 0)
				update.start();
			i++;
		} while (true);
	}

	// updates meineThreads every second
	private Thread updateThreads() {
		return new Thread(() -> {
			do {
				try {
					meineThreads = meineThreads.stream().filter(t -> t.isAlive()).collect(Collectors.toList());
//					if (meineThreads.isEmpty())
//						beenden(); // shut down server if all sockets closed
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		});
	}

	@SuppressWarnings("unused")
	private void beenden() {
		try {
			System.out.println("No Clients left. Server shut down");
			sSocket.close();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			new ChatServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class Kommunikator implements Runnable {

	private int clientnr;
	private Socket socket;

	public Kommunikator(int clientnr, Socket socket) {
		this.clientnr = clientnr + 1;
		this.socket = socket;
	}

	private void kommunizieren() {
		System.out.println("\nClient " + clientnr + " has joined the server");
		String s = "", kw = "~+*#";
		do {
			try {
				String msg = socket.readLine();
				if (!msg.equals(kw))
					System.out.println("\nMessage from Client " + clientnr + ": " + msg);
				String full = "Client " + clientnr + " -> " + msg + "\n";
				ChatServer.meineSockets.forEach(e -> {
					String a = !msg.equals(kw) ? full : "Client " + clientnr + " hat den Chat verlassen\n";
					if (!e.equals(socket)) {
						try {
							e.write(a);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				});
				s = msg;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!s.equals(kw));
		beenden();
	}

	public void beenden() {
		try {
			ChatServer.meineSockets.remove(socket);
			socket.close();
			System.out.println("Client " + clientnr + " has left");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		kommunizieren();
	}

}
