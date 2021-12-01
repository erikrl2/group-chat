package de.erik.socketio;

import java.io.IOException;

public class ServerSocket {
	private final java.net.ServerSocket serverSocket;

	public ServerSocket(final int localPort) throws IOException {
		this.serverSocket = new java.net.ServerSocket(localPort);
	}

	public Socket accept() throws IOException {
		return new Socket(this.serverSocket.accept());
	}

	public void close() throws IOException {
		this.serverSocket.close();
	}
}