package testDictionaryCrawler;

//package pictoriality;

/**
* 
*/
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author wangpd
* 
*/
public class GoogleTranslator {
	private final String m_googleTranslate = "http://translate.google.com.sg/translate_a/t?client=t&text=%s&sl=%s&tl=%s&multires=1&prev=btn&ssel=0&tsel=0&sc=1";
	private final String m_googleTranslatePostURL = "http://translate.google.com/translate_a/t";
	private final String m_googleTranslatePostParameters = "client=t&text=%s&hl=en&sl=%s&tl=%s&multires=1&ssel=0&tsel=0&sc=1";
	private Pattern m_patternTranslation = Pattern
			.compile("\\[\".*?\",\".*?\",\".*?\",\".*?\"\\]");
	private int m_maxNumCharactersPerBatch = 10000;
	private Pattern m_patternUnicode = Pattern.compile("\\\\u[0-9a-f]{4}");
	private String m_enDetokenizer = " (?<![a-zA-Z])";
	private String m_enDetokenizer2 = " (?=[\\[(])";

	private String enDetokenize(String input) {
		String re = null;
		re = input.replaceAll(m_enDetokenizer, "");
		return re;
	}

	public GoogleTranslator() {
	}

	/**
	 * translate one String by putting the String in the request URL using GET
	 * HTTP request, and this method has one problem: the sentence length can
	 * not be very long (more than 200 unicode characters); Thus, if the input
	 * sentence is too long, we will call ArrayList<String> translate(String
	 * from, String to, ArrayList<String> itemList) instead
	 * 
	 * @param from
	 *            source language ID in Google translate
	 * @param to
	 *            target language ID in Google translate
	 * @param item
	 *            the input sentence in the source language for Google to
	 *            translate
	 * @return the Google translate of the input sentence
	 * @throws IOException
	 */
	public String translate(String from, String to, String item) throws IOException {

		String translation = null;

		if (item.length() > 200) {
			ArrayList<String> inputList = new ArrayList<String>();
			inputList.add(item);
			ArrayList<String> outputList = translate(from, to, inputList);
			return outputList.get(0);
		}
		try {
			item = URLEncoder.encode(item, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		translation = String.format(m_googleTranslate, item, from, to);
		URL translationURL;

		try {
			translationURL = new URL(translation);
		} catch (MalformedURLException e) {
			System.err.println("ERROR: " + e.getMessage());
			return null;
		}

		BufferedReader httpin;
		String fullPage = "";
		System.out.println("this is translation: " + translation);
		try {
			HttpURLConnection connection = (HttpURLConnection) translationURL
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("X-HTTP-Method-Override", "GET");
			connection.setRequestProperty("referer", "accounterlive.com");
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
			httpin = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = httpin.readLine()) != null) {
				fullPage += line + '\n';
			}
			httpin.close();
		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
			return null;
		}
		System.out.println("this is full page: " + fullPage);
		
		FileWriter writer = new FileWriter("dictionary.txt", true);
		writer.write(fullPage);
		writer.close();
		
		// [[["Chinese people.","中国人民。","","Zhōngguó rénmín."]],,"zh-CN",,[["Chinese people.",[5],0,0,1000,0,3,0]],[["中国 人民 .",5,[["Chinese people.",1000,0,0]],[[0,5]],"中国人民。"]],,,[["zh-CN"]],41]
		Matcher matcher = m_patternTranslation.matcher(fullPage);

		if (!matcher.find()) {
			System.err.println("ERROR: cannot find translation results from:\n"
					+ fullPage);
			System.exit(-1);
		}
		System.out.println("this is matcher: " + matcher);
		String re = matcher.group();
		System.out.println("this is re: " + re);
		int startPosition = 2;
		int endPosition = re.indexOf('"', 2);
		while (((endPosition - 1) >= 2 && (endPosition - 1) < re.length())
				&& re.charAt(endPosition - 1) == '\\')
			endPosition = re.indexOf('"', endPosition + 1);
		re = re.substring(startPosition, endPosition);
		re = re.replaceAll("\\\\n", "");
		re = re.replaceAll("\\\\\"", "\"");
		return re;
	}

	/**
	 * a faked version of Python's join function
	 * 
	 * @param spliter
	 *            the String that is inserted between each two continuous items
	 *            of the itemList
	 * @param itemList
	 *            the actual item list whose items are going to be joined
	 *            together
	 * @return the joined String of all the items of the itemList
	 */
	private String join(String spliter, ArrayList<String> itemList) {
		StringBuilder sb = new StringBuilder();
		if (itemList.size() == 0)
			return "";
		sb.append(itemList.get(0));
		for (int i = 1; i < itemList.size(); i++) {
			sb.append(spliter);
			sb.append(itemList.get(i));
		}
		String item = sb.toString();
		return item;
	}

	/**
	 * using POST HTTP requests to ask Google to translate multiple Strings
	 * 
	 * @param from
	 *            the source language
	 * @param to
	 *            the target language
	 * @param itemList
	 *            the String list of all input sentences
	 * @return the output list contains all the translations of the itemList
	 */
	public ArrayList<String> translate(String from, String to,
			ArrayList<String> itemList) {
		String translation = null;
		String item = join("\n", itemList);
		System.out.println(item);
		try {
			item = URLEncoder.encode(item, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		translation = String.format(m_googleTranslatePostParameters, item,
				from, to);
		URL translationURL;

		try {
			translationURL = new URL(m_googleTranslatePostURL);
		} catch (MalformedURLException e) {
			System.err.println("ERROR: " + e.getMessage());
			return null;
		}

		BufferedReader httpin;

		System.out.println(translation);
		StringBuffer answer = new StringBuffer();
		try {
			HttpURLConnection connection = (HttpURLConnection) translationURL
					.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// we do not need to set the
			// connection.setRequestProperty("Content-Length", lenstr);,
			// since it will be calculated by Java automatically according to
			// the content that we write to the output stream
			// but we must write the output stream first, and then open the
			// input stream,
			// otherwise, there is no content-length field in the HTTP request
			// header

			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("referer",
					"http://translate.google.com");
			connection
					.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; rv:10.0.1) Gecko/20100101 Firefox/10.0.1");
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setInstanceFollowRedirects(false);
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// actually send the request
			OutputStreamWriter httpout = new OutputStreamWriter(
					connection.getOutputStream());
			// write POST parameters
			httpout.write(translation);
			httpout.flush();

			// Get the response
			httpin = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = httpin.readLine()) != null) {
				answer.append(line);
			}
			httpin.close();
			httpout.close();
			connection.disconnect();

		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
			return null;
		}
		// System.out.println(fullPage);
		// [[["Buffett\n","巴菲特","","Ba fēi tè"],["Google\n","谷歌","","Gǔgē"],
		// ["Wang Yan\n","王燕","","Wáng yàn"],["Avandia\n","文迪雅","","Wén dí yǎ"],
		// ["Toffee\n","奶糖","","Nǎi táng"],["Mourinho\n","穆里尼奥","","Mù lǐ ní ào"],
		// ["Areas of cooperation\n","协作区","","Xiézuò qū"],["West family\n","西家人","","Xi jiārén"],["Faya De\n","法雅德","","Fǎ yǎ dé"],["Do 's\n","别氏","","Bié shì"],["Ying\n","莹","","Yíng"],["Ferrari\n","法拉利","","Fǎlā lì"],["Blackberry\n","黑莓","","Hēiméi"],["Surrender\n","退保","","Tuì bǎo"],["Liu Gangyi\n","刘刚毅","","Liú gāngyì"],["Southern family","南家人","","Nán jiārén"]],,"zh-CN",,[["Buffett",[4],0,0,1000,0,1,0],["\n",,0,0,0,0,0,0],["Google",[15],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Wang Yan",[29],0,0,1000,0,2,1],["\n",,0,0,0,0,0,0],["Avandia",[46],0,0,834,0,1,1],["\n",,0,0,0,0,0,0],["Toffee",[68],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Mourinho",[97],0,0,845,0,1,1],["\n",,0,0,0,0,0,0],["Areas of cooperation",[135],0,0,1000,0,3,1],["\n",,0,0,0,0,0,0],["West",[184],0,0,1000,0,1,1],["family",[185],1,0,1000,1,2,1],["\n",,0,0,0,0,0,0],["Faya De",[249],0,0,1000,0,2,1],["\n",,0,0,0,0,0,0],["Do",[329],0,0,1000,0,1,1],["'s",[330],0,0,1000,1,2,1],["\n",,0,0,0,0,0,0],["Ying",[414],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Ferrari",[501],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Blackberry",[625],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Surrender",[768],0,0,1000,0,1,1],["\n",,0,0,0,0,0,0],["Liu Gangyi",[932],0,0,1000,0,2,1],["\n",,0,0,0,0,0,0],["Southern",[1119],0,0,1000,0,1,1],["family",[1120],1,0,1000,1,2,1]],[["巴菲特",4,[["Buffett",1000,0,0],["Warren Buffett",0,0,0]],[[0,3]],"巴菲特"],["谷 歌",15,[["Google",1000,0,0]],[[0,2]],"谷歌"],["王燕",29,[["Wang Yan",1000,0,0],["Yan Wang",0,0,0]],[[0,2]],"王燕"],["文 迪雅",46,[["Avandia",834,0,0],["of Avandia",0,0,0],["that Avandia",0,0,0]],[[0,3]],"文迪雅"],["奶糖",68,[["Toffee",1000,0,0],["candy",0,0,0],["Creamy Candy",0,0,0],["Naitang",0,0,0]],[[0,2]],"奶糖"],["穆里 尼奥",97,[["Mourinho",845,0,0],["Jose Mourinho",0,0,0],["Mourinho is",0,0,0]],[[0,4]],"穆里尼奥"],["协作 区",135,[["Areas of cooperation",1000,0,0],["coordinated region",0,0,0],["Cooperation Zone",0,0,0],["coordinated region of",0,0,0]],[[0,3]],"协作区"],["西",184,[["West",1000,0,0],["Western",0,0,0],["the West",0,0,0]],[[0,1]],"西家人"],["家人",185,[["family",1000,1,0],["families",0,1,0],["family members",0,1,0],["their families",0,1,0],["his family",0,1,0]],[[1,3]],""],["法雅德",249,[["Faya De",1000,0,0],["Fa Yade",0,0,0]],[[0,3]],"法雅德"],["别",329,[["Do",1000,0,0],["do not",0,0,0],["not",0,0,0],["other",0,0,0]],[[0,1]],"别氏"],["氏",330,[["'s",1000,0,0],["s",0,0,0]],[[1,2]],""],["莹",414,[["Ying",1000,0,0],["- ying",0,0,0]],[[0,1]],"莹"],["法拉利",501,[["Ferrari",1000,0,0],["the Ferrari",0,0,0],["a Ferrari",0,0,0]],[[0,3]],"法拉利"],["黑莓",625,[["Blackberry",1000,0,0],["the BlackBerry",0,0,0],["blackberries",0,0,0]],[[0,2]],"黑莓"],["退保",768,[["Surrender",1000,0,0],["surrenders",0,0,0],["surrendered",0,0,0],["lapse",0,0,0],["the surrender",0,0,0]],[[0,2]],"退保"],["刘刚毅",932,[["Liu Gangyi",1000,0,0],["Liugang Yi",0,0,0]],[[0,3]],"刘刚毅"],["南",1119,[["Southern",1000,0,0],["South",0,0,0],["the Southern",0,0,0],["the South",0,0,0],["in Southern",0,0,0]],[[0,1]],"南家人"],["家人",1120,[["family",1000,1,0],["families",0,1,0],["family members",0,1,0],["their families",0,1,0],["his family",0,1,0]],[[1,3]],""]],,,[["zh-CN"]],8]
		System.out.println(answer);
		String fullPage = answer.toString();
		Matcher matcher = m_patternTranslation.matcher(fullPage);

		ArrayList<String> translationList = new ArrayList<String>();

		StringBuilder buffer = new StringBuilder();
		while (matcher.find()) {
			String re = matcher.group();
			int startPosition = 2;
			int endPosition = re.indexOf('"', 2);
			while (((endPosition - 1) >= 2 && (endPosition - 1) < re.length())
					&& re.charAt(endPosition - 1) == '\\')
				endPosition = re.indexOf('"', endPosition + 1);
			re = re.substring(startPosition, endPosition);
			// there can be some spliting difference, e.g.:
			// 《妈妈，戈尔巴乔夫怎么写？》 is splited into two lines
			// so we have to use \\\\n to judge whether the line ends
			// there can also be some joining differences, e.g.:
			// the input is "[上一页]【返回页顶】【返回目录】[下一页]\n\n", which is treated as
			// one segment by Google
			Matcher unicodeMatcher = m_patternUnicode.matcher(re);
			while (unicodeMatcher.find()) {
				String unicode = unicodeMatcher.group();
				int decimal = Integer.parseInt(unicode.substring(2), 16);
				re = re.replace(unicode, String.format("%c", (char) decimal));
			}
			buffer.append(re);
			int end_index = matcher.end();
			if (end_index < fullPage.length()
					&& fullPage.charAt(end_index) == ']') {// ignore the phrase
															// alignment
															// information
				break;
			}
		}
		String all = buffer.toString().replaceAll("\\\\\"", "\"");
		for (String one : itemList) {// Google trims the input text before
										// translating the text
			if (one.length() == 0)
				translationList.add(one);
			else
				break;
		}
		for (String one : all.split("\\\\n")) {
			translationList.add(one);
		}
		for (int i = itemList.size() - 1; i >= 0; i--) {// Google trims the
														// input text before
														// translating the text
			String one = itemList.get(i);
			if (one.length() == 0)
				translationList.add(one);
			else
				break;
		}

		if (translationList.size() == 0) {
			System.err.println("ERROR: cannot find translation results from:\n"
					+ fullPage);
			return null;
		}
		if (translationList.size() != itemList.size()) {
			System.err
					.println("ERROR: the number of translations does not equal the number of intput sentences:\ntranslationList.size()="
							+ translationList.size()
							+ ", and itemList.size()="
							+ itemList.size());
			System.err.println(fullPage);
			return null;
		}
		return translationList;
	}

	private String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}

	/**
	 * calculate the number of lines of a file
	 * 
	 * @param filename
	 *            the input file whose lines are going to be counted
	 * @return the number of lines of the input file
	 */
	public static long getNOLines(String filename) {
		File file = new File(filename);
		if (!file.exists())
			return -1;
		long count_lines = 0;
		try {
			BufferedReader infile = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line = infile.readLine();
			while (line != null) {
				count_lines++;
				line = infile.readLine();
			}
			infile.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count_lines;
	}

	/**
	 * using Google translate to translate all the lines of a file, and output
	 * the translations to a specific file. this method can automatically detect
	 * the finished lines saved in the output file, and start from the
	 * unfinished lines of the input file
	 * 
	 * @param sourceLang
	 *            the source language
	 * @param targetLang
	 *            the target language
	 * @param filename
	 *            the input file name
	 * @param outfilename
	 *            the output file name
	 * @param numRequestsPerMinute
	 *            the number of requests which are sent to Google per minute
	 */
	public void translateFile(String sourceLang, String targetLang,
			String filename, String outfilename, int numRequestsPerMinute) {

		// requests per minute
		// inberval milli-second
		long waitTime = (long) (60.0 / (double) numRequestsPerMinute) * 1000;
		Random random = new Random();

		// detect how many lines have been finished
		long num_lines = getNOLines(outfilename);
		System.err.println("WPD: so far we have finished " + num_lines
				+ " sentences.");

		// Defines the standard input stream
		BufferedReader infile;
		try {
			infile = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			BufferedWriter outfile = null;
			int count = 0;
			String line = infile.readLine();
			ArrayList<String> inputList = new ArrayList<String>();
			// count actual characters (each unicode character means one
			// character)
			int countCharacters = 0;
			int countBatch = 0;
			while (line != null) {
				count++;
				if (count <= num_lines) {
					line = infile.readLine();
					continue;
				}
				line = line.trim();
				inputList.add(line.trim());
				countCharacters += line.length();
				if (countCharacters > m_maxNumCharactersPerBatch) {// translate
																	// a batch
																	// of
																	// sentences
																	// saved in
																	// inputList
					count -= inputList.size();
					countBatch += 1;
					System.err.println("WPD: going to translate Batch "
							+ countBatch);
					ArrayList<String> outputList = translate(sourceLang,
							targetLang, inputList);
					if (outputList == null) {
						System.err.println("WPD: Batch " + countBatch
								+ " failed.");
						infile.close();
						if (outfile != null)
							outfile.close();
						return;
					}
					// output translations to the output file
					for (String translatedText : outputList) {
						if (outfile == null)
							outfile = new BufferedWriter(
									new OutputStreamWriter(
											new FileOutputStream(outfilename,
													true), "UTF-8"));
						outfile.write(translatedText.trim() + "\n");
						outfile.flush();
						count += 1;
						System.err.println(String.format("%d : %s", count,
								translatedText));

					}
					System.err.println("WPD: Batch " + countBatch
							+ " is finished.");
					// reset
					inputList.clear();
					countCharacters = 0;
					// rest for a while
					//Thread.sleep(waitTime + 1000 + random.nextInt(9000));
				}
				line = infile.readLine();
			}
			// the last batch of sentences
			if (countCharacters > 0) {// translate a batch of sentences saved in
										// inputList
				count -= inputList.size();
				countBatch += 1;
				System.err.println("WPD: going to translate Batch "
						+ countBatch);
				ArrayList<String> outputList = translate(sourceLang,
						targetLang, inputList);
				if (outputList == null) {
					System.err.println("WPD: Batch " + countBatch + " failed.");
					infile.close();
					if (outfile != null)
						outfile.close();
					return;
				}
				// output translations to the output file
				for (String translatedText : outputList) {
					if (outfile == null)
						outfile = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(outfilename, true),
								"UTF-8"));
					outfile.write(translatedText.trim() + "\n");
					outfile.flush();
					count += 1;
					System.err.println(String.format("%d : %s", count,
							translatedText));
				}
				System.err
						.println("WPD: Batch " + countBatch + " is finished.");
				// reset
				inputList.clear();
				countCharacters = 0;
			}
			infile.close();
			if (outfile != null)
				outfile.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}/* catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		System.err.println("WPD: all done!");
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {
		GoogleTranslator google = new GoogleTranslator();
		// System.out.println(google.translate("zh-CN", "en", "\"你好\""));
		// System.out.println(google.translate("zh-CN", "en",
		// "[上一页]【返回页顶】【返回目录】[下一页]"));
		// 210 Chinese characters
		// System.exit(0);
		

		
		
		//System.out.println(google.translate("en", "zh-CN", "test"));
		
		/**
		 * ArrayList<String> inputList=new ArrayList<String>(); for (String one:
		 * "巴菲特\n谷歌\n王燕\n文迪雅\n奶糖\n穆里尼奥\n协作区\n西家人\n法雅德\n别氏\n莹\n法拉利\n黑莓\n退保\n刘刚毅\n南家人\n"
		 * .split("\n")) { inputList.add(one); } ArrayList<String>
		 * translationList=google.translatePost("zh-CN", "en", inputList); for
		 * (String one: translationList) { System.out.println(one); }
		 * System.exit(0);
		 */

		long startTime = System.currentTimeMillis();
/*		String sourceLang = args[0];
		String targetLang = args[1];
		String filename = args[2];
		String outfilename = args[3];
		// requests per minute
		int numRequestsPerMinute = 30;
		if (args.length == 5)
			numRequestsPerMinute = Integer.parseInt(args[4]);

		google.translateFile(sourceLang, targetLang, filename, outfilename,
				numRequestsPerMinute);*/
		
		long waitTime = (long) (60.0 / (double) 30) * 1000;
		Random random = new Random();
		
		BufferedReader br = new BufferedReader(new FileReader("in.txt"));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
        	google.translate("en", "zh-CN", line);
        	line = br.readLine();
        	Thread.sleep(waitTime + 1000 + random.nextInt(9000));
        }
		
        br.close();
        
        
        long endTime = System.currentTimeMillis();
		System.out.println("Total elapsed time is :" + (endTime - startTime)
				+ " Millis");
		

	}

}

