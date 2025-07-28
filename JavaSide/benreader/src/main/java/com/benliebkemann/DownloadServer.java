package com.benliebkemann;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

public class DownloadServer implements Runnable {

	private String serverLink;

	public String getLink() {
		return serverLink;
	}

	public DownloadServer() {
		this.serverLink = "";
	}

	@Override
	public void run() {
		serverLink = "http://";
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("google.com", 80));
			serverLink += socket.getLocalAddress().getHostAddress();
		} catch (IOException ioe) {
			Controller.showError("Couldn't get server link, make sure you are connected to the internet");
		}
		InetSocketAddress address = new InetSocketAddress(8080);
		Path path = Paths.get("res").toAbsolutePath();
		System.out.println(path);
		HttpServer server = SimpleFileServer.createFileServer(address, path, SimpleFileServer.OutputLevel.VERBOSE);
		server.start();
		serverLink += ":" + address.getPort();
		System.out.println(serverLink);
	}

	public static void main(String[] args) {
		Thread t = new Thread(new DownloadServer());
		t.start();
	}

}
