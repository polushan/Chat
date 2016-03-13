package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import util.CircularQueue;
import util.Config;

public class Server {
	private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
	private ServerSocket server;
	private Queue<String> messagesHistory = new CircularQueue<String>(new LinkedList<String>(), Config.hisorySize);

	public Server() {

		try {
			server = new ServerSocket(Config.port);
			while (true) {
				Socket socket = server.accept();
				Connection con = new Connection(socket);
				connections.add(con);
				con.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	private void closeAll() {
		try {
			server.close();
			synchronized (connections) {
				Iterator<Connection> iter = connections.iterator();
				while (iter.hasNext()) {
					iter.next().close();
				}
			}
		} catch (Exception e) {
			System.err.println("Threads are not closed");
		}
	}

	private class Connection extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;

		private String nickname = "";

		public Connection(Socket socket) {
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}

		@Override
		public void run() {
			try {
				login();
				send("connected");
				String message = "";
				while (true) {
					message = in.readLine().trim();
					if ("exit".equals(message)) {
						break;
					}
					send(message);
				}
				send("disconnected");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}

		private void login() throws IOException {
			while ("".equals(nickname)) {
				nickname = in.readLine().trim();
			}
			out.println();
			synchronized (connections) {
				for (String mes: messagesHistory) {
					out.println(mes);
				}
				out.flush();
			}
		}
		
		private void send(String message) throws IOException {
			synchronized (connections) {
				messagesHistory.offer(nickname + ":\n" + message);
				Iterator<Connection> iter = connections.iterator();
				while (iter.hasNext()) {
					iter.next().out.println(getTime() + " " + nickname + ":\n" + message);
				}
				out.flush();
			}
		}
		
		private String getTime() {
			Date now = new Date();
		       DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		       String time = formatter.format(now);
		       return time;
		}

		private void close() {
			try {
				in.close();
				out.close();
				socket.close();
				connections.remove(this);
			} catch (Exception e) {
				System.err.println("Threads are not closed");
			}
		}
	}
}
