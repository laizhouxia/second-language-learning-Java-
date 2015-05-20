package testMachineTranslation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static final String IP = "han.d1.comp.nus.edu.sg";
	public static final int PORT = 4001;

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		System.out.println("start...");
		Socket socket = new Socket(IP, PORT);

		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		String str = "learning english is not easy .";
		
		System.out.println("sending...");
		out.writeUTF(str);
		out.write('\n');
		System.out.println("finishing sending...");
		
		
		String ret = input.readUTF();
		System.out.println("Translation: " + ret);
		
		
		out.close();
		input.close();
	}

}
