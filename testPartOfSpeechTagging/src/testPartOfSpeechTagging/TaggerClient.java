package testPartOfSpeechTagging;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This example sends material to the tagger server one line at a time. Each
 * line should be at least a whole sentence, but can be a whole document.
 */
public class TaggerClient {
	
	public static void main(String[] args) throws IOException {
		communicateWithMaxentTaggerServer("han.d1.comp.nus.edu.sg", 8083, "utf-8");
	}

	public static void communicateWithMaxentTaggerServer(String host,
			int port, String charset) throws IOException {

		if (host == null) {
			host = "localhost";
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in, charset));
		System.err
				.println("Input some text and press RETURN to POS tag it, or just RETURN to finish.");

		for (String userInput; (userInput = stdIn.readLine()) != null
				&& !userInput.matches("\\n?");) {
			try {
				Socket socket = new Socket(host, port);
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						socket.getOutputStream(), charset), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream(), charset));
				PrintWriter stdOut = new PrintWriter(new OutputStreamWriter(
						System.out, charset), true);
				out.println(userInput);

				stdOut.println(in.readLine());
				while (in.ready()) {
					stdOut.println(in.readLine());
				}
				in.close();
				socket.close();
			} catch (UnknownHostException e) {
				System.err.print("Cannot find host: ");
				System.err.println(host);
				return;
			} catch (IOException e) {
				System.err.print("I/O error in the connection to: ");
				System.err.println(host);
				return;
			}
		}
		stdIn.close();
	}
} // end static class NERClient