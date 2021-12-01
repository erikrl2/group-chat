package de.erik.chat;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.erik.socketio.ServerSocket;
import de.erik.socketio.Socket;

public class ChatServer {

	private ServerSocket sSocket;
	public static List<Socket> meineSockets;
	private List<Thread> meineThreads;

	public ChatServer() throws IOException {
		this.sSocket = new ServerSocket(5731);
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

	private String name;

	private void kommunizieren() {
		String s = "", kw = "over", f = "";
		do {
			try {
				String msg = socket.readLine();
				if (!msg.equals(kw) && !msg.equals("")) {
					name = msg.split(" -> ")[0];
					name = name.equals("user") ? name + clientnr : name;
					f = name + " -> " + msg.strip().split(" -> ")[1];
					System.out.println(name + " -> " + msg.split(" -> ")[1]);
				}
				String c = f;
				ChatServer.meineSockets.forEach(e -> {
					String a = "";
					if (!msg.equals("")) {
						a = !msg.equals(kw) ? c : name + " hat den Chat verlassen";
						if (!e.equals(socket)) {
							try {
								e.write(a + "\n");
							} catch (IOException ex) {
								ex.printStackTrace();
							}
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

	private void beenden() {
		try {
			ChatServer.meineSockets.remove(socket);
			socket.close();
			System.out.println(name + " hat den Chat verlassen");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		kommunizieren();
	}

}
