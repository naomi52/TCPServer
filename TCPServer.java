package a;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TCPServer {
	private Set<InetAddress> firewall;	//makes a set called whitelist that stores ip addresses
	private PrintStream log;
	
	
	public TCPServer(int port, PrintStream log, Set<InetAddress>firewall, File running) throws Exception {
		
		ServerSocket server = null;
		this.log = log;
		this.firewall = firewall;
		
		server = createServer(port, server);
		/*
		 * a loopback address is used to test communication or a transport medium on a
		 * local network card and/or for testing network applications.
		 */
		InetAddress localHost =  InetAddress.getLoopbackAddress();
		/*
		 * gets a string representation of the ip and port for documentation purposes.
		 */
		String serverIpPort = ipPortToString(localHost, port);
		
		insertLogEntry("Server starting", serverIpPort);
		firewall.add(localHost);
		
		while(!server.isClosed()) {
			Socket client = createClient(server);
			
			if(ipAllowed(client)) {
				Worker clientHandler = new Worker(this, client);
				/*
				 * Thread class in java just makes a thread of execution where each thread has a priority
				 * then the thread is started in the next line
				 */
				Thread workerThread = new Thread(clientHandler);
				workerThread.start();
			}
			else {
				firewallViolation(client);
			}
			
			closeServer(server);
		}
		
		
				
				
		log.println((new Date()).toString() + "|" + "Server start" + "|" + server.getInetAddress());
		System.out.println("Listening on: " + server.getLocalPort());
		firewall = new HashSet<InetAddress>();	//assigns whitelist as a set of type hashset
		firewall.add(server.getInetAddress());	//adds the IP address of the server itself to allow for local testing
		
		//this.insertLogEntry("Server Start", );
		//this.punch(server.getInetAddress());
		while(running.exists())	//runs only when the file running exists
		{
			Socket client = server.accept();
			log.println((new Date()).toString() + "|" + "Connection" + "|" + client.getInetAddress());
			log.println((new Date()).toString() + "|" + "Disconnected" + "|" + client.getInetAddress());
			//Worker worker = new Worker();
			//worker.handle(client);
		}
		//this.insertLogEntry("Server Shutdown", );
		server.close();
	}
	private String ipPortToString(InetAddress ip,int port) {
		String ipPort = ip.toString() + ": " + Integer.toString(port);
		
		return ipPort;
	}
	
	public void insertLogEntry(String entry, String subEntry) {
		log.println(getTime() +entry + " " + "(" + subEntry + ")" + "-");		//add get time
	}
	
	public void addToFirewall(InetAddress address) {
		this.firewall.add(address);
	}
	
	public void removeFromFirewall(InetAddress address) {
		this.firewall.remove(address);
	}
	
	private boolean ipAllowed(Socket client) {
		InetAddress clientIp = client.getInetAddress();
		boolean ipAllowed = firewall.contains(clientIp);
		if (ipAllowed) {
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private ServerSocket createServer(int port, ServerSocket server) {
		try {
			server = new ServerSocket(port);	
		}
		catch(IOException e){
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return server;
	}
	
	private void closeServer(ServerSocket server) {
		try {
			server.close();
			insertLogEntry("Server is shutting down", null);
		}
		catch(IOException e) {
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	private Socket createClient(ServerSocket server) throws IOException{
		Socket client = null;
		try {
			client = server.accept();
		}
		catch(IOException e) {
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage());
			//System.exit(1);
		}
		
		return client;
	}
	
	private void firewallViolation(Socket Client) {
		insertLogEntry("Firewall violation", "hello");		//more to do here
		
		
	}
	
	public String getTime() {
		ZonedDateTime currentTime = ZonedDateTime.now();	//gets the currentTime according to the time zone
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("E d MMM yyyy HH:mm:ss z");
		
		String correctTime = currentTime.format(timeFormat);
		return correctTime;
	}
	
	public static void main(String[] args) {
		//String user = System.getProperty("user.home");
		System.out.println("hi");
	}
}
