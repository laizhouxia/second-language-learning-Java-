package testLocalServer;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
	public static void main(String args[]) {
		String data = "Toobie ornaught toobie";
		      
		try {
			while(true)
			{
				ServerSocket srvr = new ServerSocket(1235);
				Socket skt = srvr.accept();
				System.out.println("Server has connected!\n");

				System.out.println("Message:");

				BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				System.out.println("Client:" + in.readLine());

				PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
				System.out.print("Sending string: '" + data + "'\n");
				out.print(data);
				out.close();
				skt.close();
				srvr.close();
			}
		}
		catch(Exception e) {
			System.out.print("Whoops! It didn't work!\n");
		}
/*	      try {
	          ServerSocket srvr = new ServerSocket(1234);
	          Socket skt = srvr.accept();
	          System.out.print("Server has connected!\n");
	          PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
	          System.out.print("Sending string: '" + data + "'\n");
	          out.print(data);
	          out.close();
	          skt.close();
	          srvr.close();
	       }
	       catch(Exception e) {
	          System.out.print("Whoops! It didn't work!\n");
	       }*/
	}
}
