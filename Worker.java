  package a;

import projA.Util;
import projA.Course;
import projA.Student;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//@XmlRootElement(name = "TCPServer")
@XmlRootElement(name = "Worker")

public class Worker implements Runnable{

	private Socket myclient;
	private TCPServer myserver;
	private String ip;
	private Map<String, String> authorization;
	private PrintStream clientOutput;
	BufferedOutputStream clientOutStream;
	private String clientInput;
	private String format;
	
	
	public Worker(TCPServer myserver, Socket myclient ) {
		this.myserver= myserver;
		this.myclient= myclient;
		//make the authorization map in server
		//so it there's one copy of this
		authorization = new HashMap<>();
		
		//authorization.put("init", "init");
		authorization.put("Tee", "Teee");
		
		
	
	}

	public String getTime()
	{
		return myserver.myTime();
	}
	
	private void bye() throws IOException 
	{
		
			myclient.close();  //closes socket
			
			myserver.insertLogEntry("Disconnected", ip); // shows on log that there has been a disconnection
	}
	public void punch(InetAddress inetAd)
	{
		myserver.firewallAdd(inetAd);
	}
	
	
	
	public void plug(InetAddress inetAd)
	{
		myserver.firewallRemove(inetAd);
	}
	
	public BigInteger prime(int digitCount)
	{
		final float DEC_BIN_RATIO = 3.33f;
		int totalDigitCount = (int) (DEC_BIN_RATIO * digitCount);
		BigInteger randomPrime = BigInteger.probablePrime(totalDigitCount, new Random());
		//long methodReturnedPrime = randomPrime.longValue();
		
		return randomPrime;
	}
	
	public void handle() throws IOException, JAXBException
	{
		ip = myclient.getInetAddress().toString(); // store the socket ip as a string
		myserver.insertLogEntry("Connected to Client", ip);
		System.out.println("Connected to client");
		clientAccept();
		System.out.println("Disconnected from Client");
	}
	
	private void clientAccept () throws IOException, JAXBException
	{
		PrintStream clientOutput = new PrintStream(myclient.getOutputStream());
		BufferedOutputStream clientOutStream = new BufferedOutputStream(clientOutput);
		Scanner clientScreenInput = new Scanner(myclient.getInputStream());
		
		clientOutput.println("Enter your command. Type 'bye' to exit");
		String clientInput = clientScreenInput.nextLine();
		
		while(parser(clientInput, clientOutput))
		{
			clientOutput.println("Enter another Command");
			clientInput = clientScreenInput.nextLine();
		}
		
		clientScreenInput.close();
		clientOutput.close();
	}
	
	private boolean parser(String clientInput, PrintStream clientOutput) throws JAXBException //throws IOException
	{
		boolean plausibleClient = true;
		
		Pattern byePattern = Pattern.compile("(\\s*)(bye)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher byeMatcher = byePattern.matcher(clientInput);
		
		Pattern timePattern = Pattern.compile("(\\s*)(get)(\\s*)(time)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher timeMatcher = timePattern.matcher(clientInput);
		
		Pattern authPattern = Pattern.compile("(\\s*)(auth)(\\s*)(\\S+)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher authMatcher = authPattern.matcher(clientInput);
		
		Pattern primePattern = Pattern.compile("(\\s*)(prime)(\\s*)(\\d+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher primeMatcher = primePattern.matcher(clientInput);
		
		Pattern rosterPattern = Pattern.compile("(\\s*)(roster)(\\s*)(\\S+)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher rosterMatcher = rosterPattern.matcher(clientInput);
		
		
		
		
		
		if(byeMatcher.matches())
		{
			plausibleClient = false;
		}
		
		else if(timeMatcher.matches())
		{
			String returnTime = myserver.myTime();
			clientOutput.println(returnTime + "\n");
			clientOutput.flush();
		}
		
		else if(authMatcher.matches())
		{
			
			String userNameInput = authMatcher.group(4);
			String passwordInput = authMatcher.group(6);
			authMethod(userNameInput, passwordInput);
			clientOutput.println(authMethod(userNameInput, passwordInput));
			clientOutput.flush();
			
		}
		
		else if(primeMatcher.matches())
		{
			int clientDigits = Integer.parseInt(primeMatcher.group(4));
			BigInteger returnedPrime = prime(clientDigits);
			clientOutput.println(returnedPrime + "\n");
			clientOutput.flush();
		}
		else if(rosterMatcher.matches())
		{
			String myCourse = rosterMatcher.group(4);
			
			String myFormat = rosterMatcher.group(6);
			
			roster(myCourse, myFormat);
			
		}
		
		return plausibleClient;
		
	}
	
	private String authMethod(String username, String password)
	{
		if(authorization.containsKey(username))
		{
			String correctPassword = authorization.get(username);
			if (correctPassword.equals(password))
			{
				
				return "Welcome!";
			}
			
		}
		
		return "Incorrect Username/Password";
			
	}
	
	private void roster(String whatcourse, String format) throws JAXBException 
	{
		Course course = Util.getCourse(whatcourse);
		//Util.
		
		if(format.matches("[xX][mM][lL]") )
		{
			JAXBContext context = JAXBContext.newInstance(Course.class);
			
			Marshaller m = context.createMarshaller();
			
			JAXBElement<Course> root = new JAXBElement<Course>(new QName("Course"), Course.class, course);
			m.marshal(root, clientOutput);
			clientOutput.flush();
			//m.marshal(root, System.out);
			
		}
		else if (format.matches("[jJ][sS][oO][nN]") )
		{
		//Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ourOutput = gson.toJson(course);
		System.out.println(ourOutput);
		clientOutput.flush();
		//clientOutput.println(ourOutput);
		
		}
		
		
		
		
	}
	
	/**
	 * implented because it implements runnable
	 * this is automatically called when an instance of worker is made
	 */
	public void run()
	{
		try
		{
				handle();
		}
		catch(IOException e)
		{
			myserver.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + "exiting");
			System.exit(1);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


