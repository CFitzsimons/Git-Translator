import java.io.*;
import java.util.*;
import com.gtranslate.text.*;
import com.gtranslate.parsing.*;
import com.gtranslate.Translator;
import com.gtranslate.Language;

public class EntryPoint {
	public static void main(String[] args) throws Exception {
		GitShell shell = new GitShell(new File("C:/Users/Colin/Documents/GitHub/lang_german"));
		File logFile = new File("logs.txt");
		shell.generateLogs(logFile);
    ArrayList<LogEntry> logs = Reader.getLogsFromFile(logFile);
		Stuff late = new Stuff();
		late.translateLogs(logs);
    //for(LogEntry e : logs){
    //  System.out.println(e);
    //}


	}

}

class Stuff{

	public Stuff(){

	}
		public void translateLogs(ArrayList<LogEntry> allLogs){
			Translator translate = Translator.getInstance();
			String text = translate.translate("Hello!", Language.ENGLISH, Language.ROMANIAN);
			System.out.println(text); // "Bună ziua!"
		}


}

class Reader{
	public static ArrayList<LogEntry> getLogsFromFile(File logFile)throws IOException{
		Scanner scan = new Scanner(logFile);

		String currLine;
		ArrayList <LogEntry> logs = new ArrayList<>();
		LogEntry curr = null;

		while (scan.hasNextLine()) {
			currLine = scan.nextLine();
			String[] words = currLine.split(" ");
			if (words.length <= 0) continue; //Empty line, skip

			if (words[0].equals("commit")) {
				if(curr != null){
						logs.add(curr);
				}
				curr = new LogEntry(words[1]);
			}else if(words[0].equals("Author:")){
				curr.addAuthor(build(Arrays.copyOfRange(words, 1, words.length-1)));
			}else if(words[0].equals("Date:")){
				curr.addDate(build(Arrays.copyOfRange(words, 1, words.length-1)));
			}else
				curr.addLine(currLine);



		}
		return logs;
	}
	private static String build(String [] array){
		StringBuilder builder = new StringBuilder();
		for(String s : array) {
				builder.append(s + " ");
		}
		return builder.toString();
	}
}

class GitShell{
	private File target;

	public GitShell(File targetRepo){
		this.target = targetRepo;
	}

	private static Process executeNonCommand(String cmd, File loc){
		try{
			return Runtime.getRuntime().exec(cmd, null, loc);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		return null;

	}
	//new File("C:\\Users\\Colin\\Documents\\Github\\lang_german")

	public void generateLogs(File out){
		try {
			PrintWriter printer = new PrintWriter(out);
			String line;
			Process p = executeNonCommand("git log", target);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				printer.println(line);
				//System.out.println(line);
			} in.close(); printer.flush(); printer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


class LogEntry {
	private String sha;
	private String author;
	private String date;
  private String message;
  private String notes;

	public LogEntry(String sha, String author, String date) {
    this();
		this.sha = sha;
		this.author = author;
		this.date = date;

	}

  public LogEntry(String sha){
    this();
    this.sha = sha;
  }
  public LogEntry(){
    //Empty
    message = "";
  }


  public void addLine(String line){
    if(line == null || line.trim() == "" || line.contains("null"))
      return;
    message += line + "\n";
  }

  public void addDate(String date){
    this.date = date;
  }

  public void addAuthor(String author){
    this.author = author;
  }

  @Override
  public String toString() {
      return String.format("Author: %s\nDate: %s\nMessage:\n%s\n", this.author,this.date,this.message);
  }


}
