package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import util.Config;

public class Client {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String nickname = "";

	public Client() {
		Scanner scan = new Scanner(System.in);
		try {
			socket = new Socket(Config.ip, Config.port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("Enter your nickname");
			while ("".equals(nickname)) {
				nickname = scan.nextLine();
			}
			out.println(nickname);
			Resender resend = new Resender();
			resend.start();

			String message = "";
			while (!"exit".equals(message)) {
				message = scan.nextLine().trim();
				out.println(message);
			}
			resend.setStop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scan.close();
			close();
		}
	}

	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.err.println("Threads are not closed");
		}
	}

	private class Resender extends Thread {

		private boolean stoped = false;
		
		public void setStop() {
			stoped = true;
		}

		@Override
		public void run() {
			try {
				while (!stoped) {
					String message = in.readLine();
					System.out.println(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
