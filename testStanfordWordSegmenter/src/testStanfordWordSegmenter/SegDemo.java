package testStanfordWordSegmenter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;

public class SegDemo {

	  public static void main(String[] args) throws Exception {

	    if (args.length != 1) {
	      System.err.println("usage: java -mx1g SegDemo filename");
	      return;
	    }
	    
	    Properties props = new Properties();
	    props.setProperty("sighanCorporaDict", "data");
	    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
	    // props.setProperty("normTableEncoding", "UTF-8");
	    // below is needed because CTBSegDocumentIteratorFactory accesses it
	    props.setProperty("serDictionary","data/dict-chris6.ser.gz");
	    props.setProperty("testFile", args[0]);
	    props.setProperty("inputEncoding", "UTF-8");
	    props.setProperty("sighanPostProcessing", "true");

	    CRFClassifier<CoreLabel> segmenter = new CRFClassifier<CoreLabel>(props);
	    segmenter.loadClassifierNoExceptions("data/ctb.gz", props);
	    //segmenter.classifyAndWriteAnswers(args[0]);
	    
	    String sample = "我住在美国";
	    List<String> segmented = segmenter.segmentString(sample);
	    System.out.println(segmented);
	    
	    ArrayList<String> fileNames = new ArrayList<String>();
	    //fileNames.add("entertainment");
	    //fileNames.add("military");
	    //fileNames.add("finance");
	    //fileNames.add("sports");
	    //fileNames.add("international");
	    //fileNames.add("social");
	    //fileNames.add("technology");
	    //fileNames.add("lady");
	    //fileNames.add("auto");
	    //fileNames.add("game");
	    //fileNames.add("education");
	    //fileNames.add("bing");
	    
	    for(int i=0;i<fileNames.size();i++)
	    {
		    BufferedReader br = new BufferedReader(new FileReader(fileNames.get(i)+".txt"));
		    FileWriter fw = new FileWriter(fileNames.get(i)+".out");
			
		    String line;
		    while ((line = br.readLine()) != null) {
		    	segmented = segmenter.segmentString(line);
		    	System.out.println(segmented);
		    	String outLine = "";
		    	for(int j=0;j<segmented.size();j++)
		    	{
		    		if(j!=0)
		    			outLine+=',';
		    		outLine+=segmented.get(j);
		    	}
		    	outLine+='\n';
		    	fw.write(outLine);
		    }
		    br.close();
		    fw.close();
	    }
	    try {
			
			
	    	while(true)
	    	{ 
				ServerSocket srvr = new ServerSocket(1238);
				Socket skt = srvr.accept();
				System.out.println("Server has connected!\n");
				 
				System.out.println("Message:");
				 
				BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				//System.out.println("Client:" + in.readLine());
				String line = in.readLine();
				System.out.println("Client: " + line);
				
				segmented = segmenter.segmentString(line);
				System.out.println(segmented);
				String outLine = "";
				
				for(int j=0;j<segmented.size();j++)
				{
					if(j!=0)
						outLine+=',';
					outLine+=segmented.get(j);
				}
				
				PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
				System.out.print("Sending string: '" + outLine + "'\n");
				 
				out.print(outLine);
				
				out.close();
				skt.close();
				srvr.close();
	         }

	      }
	      catch(Exception e) {
	         System.out.print("Whoops! It didn't work!\n");
	      }
	  }

	}
