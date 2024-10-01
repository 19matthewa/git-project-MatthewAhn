import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.file.*;

public class git {
    String pathToDirectory = System.getProperty("user.dir");
    String gitPath = pathToDirectory + File.separator + "git";
    String objectsPath = gitPath + File.separator + "objects";
    String indexPath = gitPath + File.separator + "index";

    int zip = 0;

    // checks if files/directories exist then creates them
    public void initializeRepository() {

        File git = new File(gitPath);

        if (!git.exists()) {
            try {
                git.mkdir();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File objects = new File(objectsPath);

        if (!objects.exists()) {
            try {
                objects.mkdir();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File index = new File(indexPath);
        if (!index.exists()) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(indexPath))) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ((git.exists()) && (objects.exists()) && (index.exists())) {
            System.out.println("Git Repo already exits");
        }
    }

    // deletes index, objects, and git
    public void deleteReopsitory() {
        // File git = new File(gitPath);
        // File objects = new File(objectsPath);
        // File index = new File(indexPath);

        // String deleted = "deleted: ";
        // if (objects.exists()) {
        //     objects.delete();
        //     deleted += "objects ";
        // }
        // if (index.exists()) {
        //     index.delete();
        //     deleted += "index ";
        // }
        // if (git.exists()) {
            //     git.delete();
            //     deleted += "git ";
            // }
            
        // System.out.println(deleted);
        try (Stream<Path> walk = Files.walk(Paths.get("git"))) {
            walk.sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void blob(Path path) {
        // get file conents and sha1 hash
        
        StringBuffer contents = new StringBuffer();
        try {
            FileReader file = new FileReader(path.toString());
            String filename = path.getFileName().toString();
            //System.out.println(filename);
            BufferedReader bufferReader = new BufferedReader(file);

            char character = (char) bufferReader.read();

            while (character != (char) -1) {
                contents.append(character);
                character = (char) bufferReader.read();
            }

            bufferReader.close();;

            String hashCode = "";
            if (zip == 1) {
                // make zip file

                String zipFileName = filename + "zip";
                zip(contents.toString(), zipFileName, filename);

                try {
                    // hash ziped coneents
                    StringBuffer zippedContents = new StringBuffer();
                    FileReader file1 = new FileReader(gitPath + File.separator + zipFileName);
                    BufferedReader reader = new BufferedReader(file1);

                    char character1 = (char) reader.read();

                    while (character1 != (char) -1) {
                        zippedContents.append(character1);
                        character1 = (char) reader.read();
                    }

                    System.out.println("is " + reader.readLine());

                    contents = zippedContents;
                    filename = zipFileName;
                    reader.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            

            hashCode = sha1HashCode(contents.toString());

            BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(objectsPath + File.separator + hashCode));
            
            bufferedWriter.write(contents.toString());
            bufferedWriter.close();

            //BufferedWriter indexEditor = new BufferedWriter(new FileWriter(indexPath));
            Files.write(Paths.get("git"+File.separator+"index"), ("blob " + hashCode + " " + filename + "\n").getBytes(), StandardOpenOption.APPEND);
            //indexEditor.close();

            
        } catch (Exception e) {
            System.out.println(e + "sss");
        }        

    }

    public void generateAllTrees(Path path) {
        try{
            String rootName = sha1HashCode(listAllFiles(path, null, ""));
            Files.write(Paths.get("git"+File.separator+"index"), ("tree " + rootName + " " + path.toFile().getName() + "\n").getBytes(), StandardOpenOption.APPEND);
            
        }
        catch (IOException e){
            e.printStackTrace();
        }
        
        
    }

    //https://www.geeksforgeeks.org/list-all-files-from-a-directory-recursively-in-java/
    private String listAllFiles(Path currentPath, List<Path> allFiles, String content) throws IOException 
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath)) 
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    File[] listOfFiles = entry.toFile().listFiles();
                    String curContent = "";
                    if(listOfFiles != null) {
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                                blob(Paths.get(listOfFiles[i].getPath()));
                                if (curContent.equals("")){
                                    curContent += "blob " + sha1HashCode(Files.readString(Paths.get(listOfFiles[i].getPath()))) + " " + listOfFiles[i].getName();
                                }
                                 else{curContent += "\n"+ "blob " + sha1HashCode(Files.readString(Paths.get(listOfFiles[i].getPath()))) + " " + listOfFiles[i].getName();}
                            }
                            else if (listOfFiles[i].isDirectory()){
                                String hashCode = sha1HashCode(listAllFiles(Paths.get(listOfFiles[i].getPath()), allFiles, ""));
                                if (curContent.equals("")){
                                    curContent += "tree " + hashCode + " " + listOfFiles[i].getName();
                                }
                                else{
                                curContent += "\n" +"tree " + hashCode + " " + listOfFiles[i].getName();
                            }
                                Files.write(Paths.get("git"+File.separator+"index"), ("tree " + hashCode + " " + listOfFiles[i].getPath() + "\n").getBytes(), StandardOpenOption.APPEND);
                            }
                        }
                    }
                    
                    String curTreeName = sha1HashCode(curContent);
                    Files.write(Paths.get("git"+File.separator+"objects"+File.separator+curTreeName), curContent.getBytes());//puts cur content into objects (for sub trees of a tree [this has an extra linie])

                    if (content.equals("")){
                        content += "tree " + curTreeName + " " + entry.toFile().getName() ;//adds the tree to contents
                    }
                    else{
                        content += "\n" + "tree " + curTreeName + " " + entry.toFile().getName();//adds the tree to contents
                    }
                    Files.write(Paths.get("git"+File.separator+"index"), ("tree " + curTreeName + " " + entry.toFile().getPath() + "\n").getBytes(), StandardOpenOption.APPEND);//puts cur content into index
                    
                    
                } else {
                    if (content.equals("")){
                        content += "blob " + sha1HashCode(Files.readString(entry)) + " " + entry.toFile().getName();//adds the blob to contents
                    }
                    else{
                        content += "\n" + "blob " + sha1HashCode(Files.readString(entry)) + " " + entry.toFile().getName();//adds the blob to contents
                    }
                    blob(entry);
                }
                
            }

            String hashedFinalTreeName = sha1HashCode(content);//contents is what actually shows up in the files in objecta folder (fro final tree [this has an extra line])
            Files.write(Paths.get("git"+File.separator+"objects"+File.separator+hashedFinalTreeName), content.getBytes());
            // Files.write(Paths.get("git"+File.separator+"index"), ("tree " + hashedFinalTreeName + " " + currentPath.toFile().getName() + "\n").getBytes(), StandardOpenOption.APPEND);
            return content;
        }
    }
    

    // generates sha1 hashcode badsed on contents
    public static String sha1HashCode(String contents) {
        String result = "";
        try {
            MessageDigest message = MessageDigest.getInstance("SHA-1");
            byte[] array = message.digest(contents.getBytes());
            //System.out.println("Binary of contents:"+ contents.getBytes());
            //use above to test zip contents, by converting binary to zip online
            for (byte i : array) {
                result += String.format("%02x", i);
            }
            while (result.length() < 40) {
                result = "0" + result;
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }

    private String zip(String contentsString, String zipFileName, String FileName) {
        try {
            String result = contentsString;

            File zipFile = new File(gitPath + File.separator + zipFileName);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry zipEntry = new ZipEntry(FileName);
            out.putNextEntry(zipEntry);

            byte[] data = result.getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            out.close();

            return result;
        } catch (IOException e) {
            System.out.println(e);
        }
        return "";
    }
}