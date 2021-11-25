package de.erik.sockets.multiechoserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import socketio.ServerSocket;
import socketio.Socket;

public class MultiEchoServerZweiClients { // TODO: zu GUI machen!

	private int port;
	private ServerSocket sSocket;
	public static List<Socket> meineSockets;
	private List<Thread> meineThreads;

	public MultiEchoServerZweiClients() throws IOException {
		this.port = 1234;
		this.sSocket = new ServerSocket(port);
		meineThreads = new ArrayList<>();
		meineSockets = new ArrayList<>();
		var update = updateThreads();
		int i = 0;
		System.out.println("Waiting for Clients...");
		do {
			var socket = sSocket.accept();
			var t = new Thread(new KommunikationsThread(i, socket));
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
					if (meineThreads.isEmpty())
						beenden(); // shut down server if all sockets closed
					Thread.sleep(1000); // update interval

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		});
	}

	public void beenden() {
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
			new MultiEchoServerZweiClients();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class KommunikationsThread implements Runnable {

	private int clientnr;
	private Socket socket;

	public KommunikationsThread(int clientnr, Socket socket) {
		this.clientnr = clientnr + 1;
		this.socket = socket;
	}

	public void kommunizieren() {
		System.out.println("\nClient " + clientnr + " has joined the server");
		String s = "";
		do {
			try {
				String msg = socket.readLine();
				System.out.println("\nMessage from Client " + clientnr + ": " + msg);
				MultiEchoServerZweiClients.meineSockets.forEach(e -> {
					try {
						if (!e.equals(socket)) {
							e.write("Client " + clientnr + " schreibt: " + msg + "\n");
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				});
				s = msg;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (!s.endsWith("over"));
		beenden();
	}

	public void beenden() {
		try {
			MultiEchoServerZweiClients.meineSockets.remove(socket);
			socket.close();
			System.out.println("\nClient " + clientnr + " has left");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		kommunizieren();
	}

}
