import java.io.*;
import java.util.*;

public class EntryPoint {
	public static void main(String[] args) throws Exception {

    ArrayList<LogEntry> logs = getLogs();

    for(LogEntry e : logs){
      System.out.println(e);
    }


	}

  public static ArrayList<LogEntry> getLogs()throws IOException{
    runCommand("git log", "logout.txt");
    Scanner scan = new Scanner(new File("logout.txt"));
    String currLine = "";
    ArrayList <LogEntry> logs = new ArrayList<>();
    System.out.println(scan.hasNextLine());
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
      }else if(words[0].contains("Author")){
        curr.addAuthor(build(Arrays.copyOfRange(words, 1, words.length-1)));
      }else if(words[0].contains("Date")){
        curr.addDate(build(Arrays.copyOfRange(words, 1, words.length-1)));
      }else
        curr.addLine(currLine);



    }
    return logs;
  }
	public static void runCommand(String command, String outputName) {

		try {
			PrintWriter pw = new PrintWriter(new File(outputName));
			String line;
			Process p = Runtime.getRuntime().exec(command, null, new File("C:\\Users\\Colin\\Documents\\Github\\lang_german"));

			BufferedReader in = new BufferedReader(
			new InputStreamReader(p.getInputStream()));
			while ((line = in .readLine()) != null) {
				pw.println(line);
        //System.out.println(line);
			} in.close(); pw.flush(); pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
  private static String build(String [] array){
    StringBuilder builder = new StringBuilder();
    for(String s : array) {
        builder.append(s + " ");
    }
    return builder.toString();
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
