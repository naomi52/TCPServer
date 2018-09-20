package a;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Worker implements Runnable{
	public Socket clientSocket;
	public TCPServer server;
	public String clientIp;
	public Map<String, String> authMap;

	public Worker(TCPServer server, Socket client) {
		this.clientSocket  = client;	//saving the clients socket
		this.server = server;			//saving the server
		this.clientIp = clientSocket.getInetAddress().toString();
		
	}
	
	public void handle() throws IOException {
	 server.insertLogEntry("Client is connected", clientIp);
	 System.out.println("Client is connected");
	 
	 clientIO();
	 
	 System.out.println("Client is disconnected");
	}
	
	public void run() {
		try {
			handle();
		}
		catch(IOException e){
			server.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + "closing");
		}
	}
	
	private long prime(int digits) {
		/*
		 * converts a given integer to a binary number
		 */
		final float DEC_TO_BIN_RATIO = 3.5f;
		int bitsToDec = (int)(DEC_TO_BIN_RATIO * digits);
		
		/*
		 * converts the given binary to a prime number
		 */
		BigInteger bigPrime = BigInteger.probablePrime(bitsToDec, new Random());
		long primeNum = bigPrime.longValue();
		
		return primeNum;
	}
	
	private void parser(String clientInput, PrintStream clientOutput) {
		boolean clientQuery = true;
		
		Pattern getTimePattern = Pattern.compile("(\\s*)(get)(\\s*)(time)(\\s*)");
		Matcher getTimeMatcher = getTimePattern.matcher(clientInput);
		
		Pattern byePattern = Pattern.compile("(\\s*)(bye|exit)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher byeMatcher = byePattern.matcher(clientInput);
		
		Pattern primePattern = Pattern.compile("(\\s*)(prime)(\\s*)(\\d+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher primeMatcher = primePattern.matcher(clientInput);
		
		Pattern authPattern = Pattern.compile("(\\s*)(auth)(\\s*)(\\S+)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher authMatcher = authPattern.matcher(clientInput);
		
		Pattern rosterPattern = Pattern.compile("(\\s*)(roster)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher rosterMatcher = rosterPattern.matcher(clientInput);
		
		if(byeMatcher.matches()) {
			clientQuery = false;
		}
		
		else if(getTimeMatcher.matches()) {
			clientOutput.println(server.getTime()+ "\n");
		}
		/*
		 * saves the clients input number which is in the fourth bracket (group 4)
		 * and changes it to a prime then returns it
		 */
		else if (primeMatcher.matches()) {
			int clientDigits = Integer.parseInt(primeMatcher.group(4));
			long primeNumber = prime(clientDigits);
			
			clientOutput.println(primeNumber + "\n");
		}
		
		else if(authMatcher.matches()) {
			String userName = authMatcher.group(4);
			String userPass = authMatcher.group(6);
			
			clientOutput.println(authorization(userName, userPass));
		}
		
		else if(rosterMatcher.matches()) {
			String courseNum = rosterMatcher.group(4);
			//String courseJson = 
		}
		
	}
	
	private String authorization(String userName, String userPass) {
		if(authMap.containsKey(userName)) {
			//this gets the password that is matched to the username and saves it in validPass
			String validPass = authMap.get(userName);
			if(validPass.matches(userPass)) {
				return "Access granted";
			}
		}
		return userPass;		
	}
	
	private String roster(String courseNum) {
		//Course course = Uti
		return "hello";
	}
	
	private void clientIO() throws IOException{
		PrintStream clientOutput = new PrintStream(clientSocket.getOutputStream());
		Scanner clientScanner = new Scanner(clientSocket.getInputStream());
		clientOutput.println("Enter a command. Print 'bye' to exit");
		String clientInput = clientScanner.nextLine();
		
	}
	
	public String getTime() {
		return server.getTime();
	}
	
	public void bye() {
		try {
			clientSocket.close();
		}
		catch(IOException e){
			server.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + " closing");
			System.exit(1);
		}
		
		server.insertLogEntry("Client is disconnected", clientIp);
	}
	
	public void punch(InetAddress a) {
		server.addToFirewall(a);
	}
	
	public void plug(InetAddress a) {
		server.removeFromFirewall(a);
		
	}
	
	

}
