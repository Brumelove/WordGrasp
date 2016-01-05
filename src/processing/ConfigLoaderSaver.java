package processing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigLoaderSaver {

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
    
    public HashMap<String, HashMap<String, String>> loadFillInTheBlanks() {

        HashMap<String, HashMap<String, String>> wordImageTheme = new HashMap();

        String projectDir = new File("").getAbsolutePath();
        String gameDir = projectDir + "\\images\\games\\fillintheblanks";
        
        String[] themes = (new File(gameDir)).list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
              return new File(current, name).isDirectory();
            }
        });
        
        for(String theme : themes) {
            
            HashMap<String, String> wordImage = new HashMap();

            File[] files = (new File(gameDir+"\\"+theme)).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().matches("(?i).*\\.(jpg|png|bmp)$");
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
    
    public String getFillInTheBlanksResPath() {
        
        return (new File("").getAbsolutePath()+"\\images\\games\\fillintheblanks\\");
        
    }
    
    public String getFillInTheBlanksGifPath(String theme, String word) {
        
        return (new File("").getAbsolutePath()+"\\images\\games\\fillintheblanks\\"+theme+"\\"+word+".gif");
        
    }
    
    public String getFillInTheBlanksVidPath(String theme) {
        
        return (new File("").getAbsolutePath()+"\\images\\games\\fillintheblanks\\"+theme+"\\"+theme+".mp4");
        
    }
    
    public void createProfile(String profileName) {
        
        String projectDir = new File("").getAbsolutePath();
        saveTextFile(projectDir+"\\profiles\\"+profileName.toLowerCase()+".txt",Arrays.asList("#points","-0","#vocablist",""));
        
    }
    
    public HashMap<String, Multimap<String, String>> loadAllProfiles() {

        HashMap<String, Multimap<String, String>> saves = new HashMap();

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
    
    public Multimap<String, String> loadProfile(String profileName) { 
        return loadAllProfiles().get(profileName);
    }
    
    public List<Multimap<String, String>> loadProfiles(List<String>profileNames) { 
        
        HashMap<String, Multimap<String, String>> allProfiles = loadAllProfiles();
        List<Multimap<String, String>> profiles = new ArrayList();
        
        for (String profileName : profileNames)
            profiles.add(allProfiles.get(profileName));
        
        return profiles;
        
    }
    
    public void saveProfile(String profileName, int points, List<String> words) {
        
        String projectDir = new File("").getAbsolutePath();
        words.remove("");
        List<String> prefixedWords = words.stream().map(word -> "-"+word).collect(Collectors.toList());
        prefixedWords.add("");
        List<String> lines = new ArrayList(Arrays.asList("#points","-"+points,"#vocablist"));
        lines.addAll(prefixedWords);
        
        saveTextFile(projectDir+"\\profiles\\"+profileName.toLowerCase()+".txt",lines);
        
    }
    
    public List<String> getVocabList(Multimap<String, String> profile, List<String> words) {
        
        List<String> vocabList = new ArrayList(profile.get("vocablist"));
        vocabList.retainAll(words);
        
        return vocabList;
        
    }
    
    public List<String> getIdealWordOrder(List<String>profileNames, List<String>words) {
        
        Collections.sort(words, (String s1, String s2) -> s1.length() - s2.length());
        
        List<Multimap<String, String>> profiles = loadProfiles(profileNames);
        
        List<List<String>> vocabLists = new ArrayList();
        
        for (Multimap<String, String> profile : profiles)
            vocabLists.add(getVocabList(profile, words));
        
        HashMap<String,Integer> similarities = new HashMap();
        
        for (List<String> vocabList : vocabLists) {
            
            for (String word : vocabList) {
                
                if(similarities.get(word) == null)
                    similarities.put(word, 1);
                else similarities.put(word, similarities.get(word)+1);
                
            }
            
        }
        
        Map<String, Integer> sortedSimilarities = similarities.entrySet().stream()
                                                    .sorted(Map.Entry.comparingByValue())
                                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                                            (e1, e2) -> e1, LinkedHashMap::new));
        
        List<String> idealWordOrder = new ArrayList();
        
        idealWordOrder.addAll(words);
        idealWordOrder.removeAll(new ArrayList(sortedSimilarities.keySet()));
        
        for (String key : sortedSimilarities.keySet())
            idealWordOrder.add(key);
        
        return idealWordOrder;
        
    }
    
    public String getPoints(String profileName) {
        
        Multimap<String, String> pointsWords = loadProfile(profileName);
        List<String> points = new ArrayList(pointsWords.get("points"));
        return points.get(0);
        
    }
    
    public void addPointsAddWord(String profileName, int addPoints, String addWord) {
        
        Multimap<String, String> pointsWords = loadProfile(profileName);
        
        List<String> points = new ArrayList(pointsWords.get("points"));
        List<String> words = new ArrayList(pointsWords.get("vocablist"));
        
        if (!words.contains(addWord))
            words.add(addWord);
        
        saveProfile(profileName,(Integer.parseInt(points.get(0))+addPoints),words);
        
    }
    
    public void removePoints(String profileName, int subtractPoints) {
        
        Multimap<String, String> pointsWords = loadProfile(profileName);
        
        List<String> points = new ArrayList(pointsWords.get("points"));
        List<String> words = new ArrayList(pointsWords.get("vocablist"));
        
        saveProfile(profileName,(Integer.parseInt(points.get(0))-subtractPoints),words);
        
    }
    
}
