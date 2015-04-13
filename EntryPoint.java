import java.io.*;
import java.util.*;
import java.text.Normalizer;
import org.google.translate.api.v2.core.model.Detection;
import org.google.translate.api.v2.core.model.Language;
import org.google.translate.api.v2.core.model.Translation;
import org.google.translate.api.v2.core.Translator;

/*
* TODO:
*	- Maybe remove the race condition inside GitShell
*/
public class EntryPoint {

    public static void main(String[] args){
		if(args.length != 1){
			System.err.println("Please supply one argument, the parent git folder.");
			System.err.println("Form: java -cp [externals] program [C:/Some/Git/Folder]");
			System.exit(1);//Arguments exit
		}
		File gitParent = new File(args[0]);
		if(!gitParent.exists()){
			System.err.println("Error finding folder: Please ensure that the path is correct.");
			System.exit(1);
		}
		GitShell shell = new GitShell(gitParent);
		if(!shell.isValidGitRepository()){
			System.err.println("Not valid git folder: Please ensure the folder is correct");
			System.exit(1);
		}
        File logFile = new File("logs.txt");
        shell.generateLogs(logFile);
		try{
			ArrayList<LogEntry> logs = Reader.getLogsFromFile(logFile);
			GoogleTranslationService late = new GoogleTranslationService();
			System.out.println("Attempting to reach google translation service...");
			late.translateLogs(logs);
			System.out.println("Translation complete, attempting to update logs...");
			shell.updateLogs(logs);
			System.out.println("Update complete!  Press enter to exit.");
			System.in.read();
		}catch(Exception e){
			System.err.println("Error accessing files. Common issues:");
			System.err.println("- Incorrect/invalid API key.\n- No read/write permission.");
			//e.printStackTrace();
		}
		System.exit(0);
    }
}
class GoogleTranslationService{
    final static String API_KEY = "PUT_YOUR_API_KEY_HERE";
    private Translator service;
    public GoogleTranslationService(){
        service = new Translator(API_KEY);
    }
    public void translateLogs(ArrayList<LogEntry> logs)throws Exception{
        String [] array = extractMessages(logs);
        Translation [] after = service.translate(array, "de", "en");
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
        logs.add(curr);
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
            System.exit(3);
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
	private class BooleanTimer extends TimerTask  {
        public boolean isValid = true;

        public BooleanTimer() {}

        @Override
        public void run() {
            try {
                String line;
                Process p = executeNonCommand("git log", target);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));

                while ((line = in.readLine()) != null) {
                    isValid = !line.contains("fatal");
                } in.close();
            } catch (Exception e) {
                e.printStackTrace();
                isValid = false;
            }
        }
    }
    public boolean isValidGitRepository(){
		BooleanTimer ft = new BooleanTimer();
		//This is to stop the bufferedreader from looping infinitly
		(new Timer()).schedule(ft, 200);
		try{
			Thread.sleep(300);
		}catch(InterruptedException e){
			e.printStackTrace();
		}

		return ft.isValid;

	}
    public void updateLogs(ArrayList<LogEntry> logs){
        for(LogEntry log : logs){
            try{
                executeNonCommand(log.getUpdateCommand(), target).waitFor();
            }catch(Exception ex){
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
    //    private static Normalizer normal;
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
        message += Normalizer.normalize(line, Normalizer.Form.NFD) + "\n";
    }
    public void appendNote(String line){
        if(line == null || line.trim() == "" || line.contains("null"))
        return;
        notes += Normalizer.normalize(line, Normalizer.Form.NFD) + "\n";
    }
    public void addDate(String date){
        this.date = Normalizer.normalize(date, Normalizer.Form.NFD);
    }
    public void addAuthor(String author){
        this.author = Normalizer.normalize(author, Normalizer.Form.NFD);
    }
    public String getUpdateCommand(){
        return String.format("git notes append -m \"%s\" %s", notes.replaceAll("\"", "\'"), sha);
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
        return String.format("SHA:%s\nAuthor: %s\nDate: %s\nMessage:\n%snNotes:\n%s\n",
            this.sha, this.author,this.date,this.message, this.notes);
    }
}
