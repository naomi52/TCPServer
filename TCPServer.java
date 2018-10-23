package a;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TCPServer")
public class TCPServer {
	Set<InetAddress> firewall;	//makes a set called whitelist that stores ip addresses
	private PrintStream log;
	ServerSocket server;
	private File running;
	
	public TCPServer(int port, PrintStream log, HashSet<InetAddress>firewall, File running) throws Exception {
	
		this.log = log;
		this.firewall = firewall;
		this.server = new ServerSocket(port);
		this.running = running;
		
		//same as getting the loopBackAddress
		InetAddress localHost = InetAddress.getByName("127.0.0.1");
		insertLogEntry("\nServer Start", ipPortToString(server.getInetAddress(), port));
		
		//while(running.exists())	//runs only when the file running exists
		//{
		//	Socket client = server.accept();
		//	log.println((new Date()).toString() + "|" + "Connection" + "|" + client.getInetAddress());
		//	log.println((new Date()).toString() + "|" + "Disconnected" + "|" + client.getInetAddress());
			//Worker worker = new Worker();
			//worker.handle(client);
		//}
		//this.insertLogEntry("Server Shutdown", );
		
		firewall.add(localHost);	//adds the IP address of the server itself to allow for local testing
		System.out.println(InetAddress.getLocalHost()); // global ip

		while(running.exists()) {
			//receive client
			System.out.println("waiting");
			Socket client = createClient();
			System.out.println("Client found");
			
			//check whitelist
			if(ipAllowed(client)) {
				System.out.println("Client is on the whitelist");
				//create a worker and worker handles client
				Worker clientHandler = new Worker(this,client);
				Thread workerThread = new Thread(clientHandler);
				workerThread.start();
			}
			else {
				System.out.println("Client is NOT on the whitelist");
			}
		}
		
		server.close();
		
	}
	
//	public void listen() {
//			
//		if(ipAllowed(client)||true) { // for debugging. remove "true" when firewall implemented
//			Worker clientHandler = new Worker(this,client);
//			Thread workerThread = new Thread(clientHandler);
//			workerThread.start();
//		}
//		else {
//			firewallViol(client);
//		}
//	}
	
	public String myTime()
	{
		ZonedDateTime nowTime = ZonedDateTime.now();
		DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("E d MMM yyyy HH:mm:ss z");
		String finalReturn = nowTime.format(formattedTime);
		return finalReturn;
	}
	public void firewallAdd( InetAddress inetAd)
	{
		firewall.add(inetAd);  // Adds the ip address to the firewall list
	}
	
	public void firewallRemove( InetAddress inetAd)
	{
		firewall.remove(inetAd);  //Removes the ip address from the firewall list
	}

	public void insertLogEntry(String mainEntry, String subEntry)
	{
		log.println(mainEntry + " | " + subEntry + " | " + myTime() );  // inputs entry into the log
	}
	
	public boolean ipAllowed(Socket client)
	{
		InetAddress myClientIp = client.getInetAddress();
		boolean permitted = firewall.contains(myClientIp);
		if(permitted)
		{
			return permitted;
		}
		else
		{
			return false;
		}
	}
	private void closeServer()
	{
		try {
			server.close();
			insertLogEntry("Server shutdown", null);
		}
		catch(IOException e)
		{
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println("Error" + e.getMessage());
			System.exit(1);
		}
	}
	
	private ServerSocket serverCreate(int port, ServerSocket server)
	{
		try {
			server = new ServerSocket(port);
		}
		catch(IOException e)
		{
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println("Error" + e.getMessage());
			System.exit(1);
		}
		return server;
	}
	
	/**
	 * Creates a client object
	 * @return
	 */
	private Socket createClient()
	{
		Socket client = null;
		try {
			client = this.server.accept();
		}
		catch(IOException e)
		{
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
		}
		
		return client;
	}
	
	
	private void firewallViol(Socket client)
	{
		insertLogEntry("Firewall Violation", client.getInetAddress().toString());
		try {
			PrintStream clientOutput = new PrintStream(client.getOutputStream());
			clientOutput.println("Authentication failed, exiting...");
			client.close();
		}
		catch(IOException e)
		{
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
		}
	}
	
	private String ipPortToString(InetAddress ip, int port)
	{
		String ipPort = ip.toString() +":"+ Integer.toString(port); 
		return ipPort;
	}
	
	public static void main(String[] args) throws Exception {
		int listenPort = 10560;
		
		String userHome = System.getProperty("user.home");
		File logFile = new File(userHome, "/eclipse-workspace/4413/src/a/log.txt");
		
		File runningFile = new File(userHome, "/eclipse-workspace/4413/src/a/running.txt");

		HashSet<InetAddress> firewall = new HashSet<>();

		PrintStream log = new PrintStream(new FileOutputStream(logFile, true));

		System.out.println("Starting Server, connection port is " + listenPort);

		TCPServer theServer = new TCPServer(listenPort, log, firewall, runningFile);
		//theServer.listen();
		//theServer.closeServer();

		System.out.println("Server shutting down");

		log.close();
	}
}

