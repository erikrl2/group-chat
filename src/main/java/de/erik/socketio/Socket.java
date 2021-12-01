package de.erik.socketio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Socket {
	private final String hostname;
	private final int port;
	private java.net.Socket socket;
	private BufferedReader reader;

	public Socket(final String hostname, final int port) throws IOException {
		this.hostname = hostname;
		this.port = port;
	}

	public Socket(final java.net.Socket socket) throws IOException {
		this.socket = socket;
		this.port = socket.getPort();
		this.hostname = socket.getRemoteSocketAddress().toString();
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void close() throws IOException {
		this.reader.close();
		this.socket.close();
	}

	public boolean connect() {
		try {
			this.socket = new java.net.Socket(this.hostname, this.port);
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public int dataAvailable() throws IOException {
		return this.socket.getInputStream().available();
	}

	public int read() throws IOException {
		return this.socket.getInputStream().read();
	}

	public int read(final byte[] b, final int len) throws IOException {
		return this.socket.getInputStream().read(b, 0, len);
	}

	public String readLine() throws IOException {
		return this.reader.readLine();
	}

	public void write(final int b) throws IOException {
		this.socket.getOutputStream().write(b);
	}

	public void write(final byte[] b, final int len) throws IOException {
		this.socket.getOutputStream().write(b, 0, len);
	}

	public void write(final String s) throws IOException {
		this.socket.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
	}
}