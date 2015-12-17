package processing;

import processing.core.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigLoaderSaver {

    public HashMap<String, Multimap<String, String>> loadProfiles() {

        HashMap<String, Multimap<String, String>> saves = new HashMap<String, Multimap<String, String>>();

        String projectDir = new File("").getAbsolutePath();

        File[] files = (new File(projectDir + "\\profiles")).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });

        for (int i = 0; i < files.length; i++) {

            Multimap<String, String> save = HashMultimap.create();

            //Player Name
            String name = files[i].getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0) name = name.substring(0, pos);
                
            BufferedReader br = null;
            String line = "";

            try {
                
                String textSection = "";
                br = new BufferedReader(new FileReader(files[i].getAbsolutePath()));
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        if (line.startsWith("#points")) textSection = "points";
                        else if (line.startsWith("#vocablist")) textSection = "vocablist";
                    }
                    if (line.startsWith("-")) save.put(textSection, line.substring(1).trim());
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            saves.put(name, save);
        }
        return saves;
    }
    
    public HashMap<String, HashMap<String, String>> loadFillInTheBlanks() {

        HashMap<String, HashMap<String, String>> wordImageTheme = new HashMap<String, HashMap<String, String>>();

        String projectDir = new File("").getAbsolutePath();
        String gameDir = projectDir + "\\images\\fillintheblanks";
        
        String[] themes = (new File(gameDir)).list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
              return new File(current, name).isDirectory();
            }
        });
        
        for(String theme : themes) {
            
            HashMap<String, String> wordImage = new HashMap<String, String>();

            File[] files = (new File(gameDir+"\\"+theme)).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().matches("(?i).*\\.(jpg|png|gif|bmp)$");
                }
            });

            for (File file : files) {

                //Image Name
                String name = file.getName();
                int pos = name.lastIndexOf(".");
                if (pos > 0) name = name.substring(0, pos);

                wordImage.put(name, file.getAbsolutePath());

            }
            
            wordImageTheme.put(theme, wordImage);
            
        }
        
        return wordImageTheme;
        
    }
    
    public void createProfile(String profileName) {
        
        String projectDir = new File("").getAbsolutePath();
        saveTextFile(projectDir+"\\profiles\\"+profileName.toLowerCase()+".txt",Arrays.asList("#points","-0","#vocablist",""));
        
    }
    
    public List<String> loadTextFile(String dir) {
        
        List<String> lines = null;
        
        try {
            lines = Files.readAllLines(Paths.get(dir));
        } catch(IOException e){ e.printStackTrace(); }
        
        return lines;
        
    }
    
    public void saveTextFile(String dir, List<String> lines) {
        
        try {
            Files.write(Paths.get(dir), lines);
        } catch(IOException e){ e.printStackTrace(); }
                
    }
    
    public static void main(String args[]) {
        
        ConfigLoaderSaver conf = new ConfigLoaderSaver();
        
        HashMap<String, Multimap<String, String>> profiles = conf.loadProfiles();
        
        profiles.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
        
        System.out.println();
        
        
    }
    
}
