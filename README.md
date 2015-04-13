# Git-Translator

Dependencies
------------

There are two things you will need to do before using this project.  Firstly you will have to download git tools for windows.  This can be found here:

http://git-scm.com/downloads

The second thing you will need to do is update your path variables to include the bin directory or generally have the git toolset accessable though standard command prompt.  I can only assume the steps are the same for Linux, if they are not leave a comment/email me and I'll spin up a VM then update the doc.  

If your not too sure if this part was done correctly, open up commmand prompt (Windows Key + R then type CMD) and in the window type git.  You should get a list of git commands if git is setup correctly.

Compiling and running
---------------------



Firstly, before compiling the program make sure to change the API key in the code, it is found inside the `GoogleTranslationService class`.  Roughly on line 51.  

Compile from command line on windows with:

`javac -cp .;google-translate-api-v2-java-core-0.52.jar EntryPoint.java`

Run from commandline with:

`java -cp .;google-translate-api-v2-java-core-0.52.jar EntryPoint C:/Your/Path/To/The/Git/Folder`

Checklist for a working program:
<ol>
  <li>Install Git tools</li>
  <li>Update path variables (as needed)</li>
  <li>Update API key</li>
  <li>Working internet connection (for calling google translate)</li>
  <li>Supplied a valid local git repository that you have access too</li>
</ol>
