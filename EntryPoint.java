import java.io.*;
import java.util.*;
import java.text.Normalizer;
import org.google.translate.api.v2.core.model.Detection;
import org.google.translate.api.v2.core.model.Language;
import org.google.translate.api.v2.core.model.Translation;
import org.google.translate.api.v2.core.Translator;

public class EntryPoint {
	public static void main(String[] args) throws Exception {
		GitShell shell = new GitShell(new File("C:/Users/Colin/Documents/GitHub/lang_german"));
		File logFile = new File("logs.txt");
		shell.generateLogs(logFile);
    ArrayList<LogEntry> logs = Reader.getLogsFromFile(logFile);
		GoogleTranslationService late = new GoogleTranslationService();
		late.translateLogs(logs);
    for(LogEntry e : logs){
      System.out.println(e);
    }
		shell.updateLogs(logs);


	}

}

class GoogleTranslationService{

	final static String API_KEY = "PUT_YOUR_API_KEY_HERE";
	private Translator service;

	public GoogleTranslationService(){
		service = new Translator(API_KEY);
	}
	public void translateLogs(ArrayList<LogEntry> logs)throws Exception{
		System.out.println("Size of array before any operations: " + logs.size());
		String [] array = extractMessages(logs);
		System.out.println("Size of message array: " + array.length);
		Translation [] after = service.translate(array, "de", "en");
		System.out.println("Size of array after translation: " + after.length);
		for(int i = 0; i < after.length; i++){
			array[i] = after[i].getTranslatedText();
		}
		appendMessages(logs, array);
	}
	private String [] extractMessages(ArrayList<LogEntry>logs){
		String [] arr = new String[logs.size()];
		for(int i = 0; i < logs.size();i++){
			arr[i] = logs.get(i).getMessage();
		}
		return arr;
	}

	private void appendMessages(ArrayList<LogEntry> logs, String [] messages){
		for(int i = 0; i < logs.size(); i++){
			logs.get(i).setNote(messages[i]);
			System.out.println(messages[i]);
		}
	}

}

class Reader{
	public static ArrayList<LogEntry> getLogsFromFile(File logFile)throws IOException{
		Scanner scan = new Scanner(logFile);

		String currLine;
		ArrayList <LogEntry> logs = new ArrayList<>();
		LogEntry curr = null;
		boolean isNotes = false;
		while (scan.hasNextLine()) {
			currLine = scan.nextLine();
			String[] words = currLine.split(" ");
			if (words.length <= 0) continue; //Empty line, skip

			if (words[0].equals("commit")) {
				if(curr != null){
						logs.add(curr);
				}
				isNotes = false;
				curr = new LogEntry(words[1]);
				continue;
			}else if(words[0].equals("Author:")){
				curr.addAuthor(build(Arrays.copyOfRange(words, 1, words.length-1)));
				continue;
			}else if(words[0].equals("Date:")){
				curr.addDate(build(Arrays.copyOfRange(words, 1, words.length-1)));
				continue;
			}else if(words[0].equals("Notes:"))
				isNotes = true;
			if(isNotes)
					curr.appendNote(currLine);
			else
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


class Shell{
	protected static Process executeNonCommand(String cmd, File loc){
		try{
			return Runtime.getRuntime().exec(cmd, null, loc);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		return null;

	}
	protected static Process executeNonCommand(String cmd){
		return executeNonCommand(cmd, null);
	}


}
class GitShell extends Shell{
	private File target;

	public GitShell(File targetRepo){
		this.target = targetRepo;
	}

	public boolean isValidGitRepository(){
		return true;
	}

	public void updateLogs(ArrayList<LogEntry> logs){
		for(LogEntry log : logs){
			try{
				executeNonCommand(log.getUpdateCommand(), target).waitFor();
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}

		}
	}


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

//	private static Normalizer normal;

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
//		normal = new Normalizer();
  }


  public void addLine(String line){
    if(line == null || line.trim() == "" || line.contains("null"))
					return;
				message += Normalizer.normalize(line, Normalizer.Form.NFKD) + "\n";
  }
	public void appendNote(String line){
		if(line == null || line.trim() == "" || line.contains("null"))
			return;
		notes += Normalizer.normalize(line, Normalizer.Form.NFKD) + "\n";
	}

  public void addDate(String date){

    this.date = Normalizer.normalize(date, Normalizer.Form.NFKD);
  }

  public void addAuthor(String author){

    this.author = Normalizer.normalize(author, Normalizer.Form.NFKD);
  }

	public String getUpdateCommand(){
		System.out.println(String.format("git notes append -m \"%s\" %s", notes, sha));
		return String.format("git notes append -m \"%s\" %s", notes, sha);
	}

	public String getMessage(){
		return this.message;
	}

	public void setNote(String note){
		this.notes = note;
	}

	public String getNote(){
		return this.notes;
	}

  @Override
  public String toString() {
      return String.format("SHA:%s\nAuthor: %s\nDate: %s\nMessage:\n%s\nNotes:\n%s\n", this.sha, this.author,this.date,this.message, this.notes);
  }


}
