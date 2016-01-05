package processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import processing.core.*;
import processing.video.*;
import TUIO.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.io.File;
import java.util.Set;
import gifAnimation.*;

public class Sketch extends PApplet {

    PImage menuImage;
    PFont welcome;
    int screensizex, screensizey, stage;
    int time;
    
    HashMap<Integer, List<String>> fiducialDictionary = new HashMap();
    
    ConfigLoaderSaver config = new ConfigLoaderSaver();
    List<String>currentProfiles = new ArrayList();
    String currentChoice = "";
    List<String>currentSelection = new ArrayList();
    List<Float> cursorPosition = null;
    HashMap<String, List<Float>> choiceButtons = new HashMap();
    boolean selected = false;
    HashMap<String, String> saves = new HashMap();
    HashMap<String, String> fillInTheBlanks_ThemeImageWords = new HashMap();
    HashMap<Integer, List<Float>> fillInTheBlanks_gapCoords = new HashMap();
    List<String> fillInTheBlanks_WordList = new ArrayList();
    HashMap<List<String>,List<Float>> oldFiducials = new HashMap();
    
    String game = "";
    String theme = "";
    String word = "";
    String createProfile = "";
    HashMap<String,String>profilesColours = new HashMap();
    HashMap<String,String>wordGuessed = new HashMap();
    PImage img = null;
    
    Gif adviceGif = null;
    Gif wordGif = null;
    Movie successMovie = null;
    
    boolean init = true;
    boolean next = true;
    
    //// TUIO ////
    
    TuioProcessing tuioClient;

    float cursor_size = 15;
    float object_size = 60;
    float table_size = 760;
    float scale_factor = 1;
    PFont font;
    PFont selectorFont;

    boolean verbose = false;
    boolean callback = true;
    
    //////////////

    @Override
    public void settings() {

        fullScreen();

    }

    @Override
    public void setup() {
        stage = 1;
        screensizex = width;
        screensizey = height;
        menuImage = loadImage("menu.jpg");
        image(menuImage, 0, 0, screensizex, screensizey);        
        
        fiducialDictionary.put(0,Arrays.asList("CURSOR", "MENU"));
        for(int i = 0; i < 26; i++) { //1 - 52
            fiducialDictionary.put(i+1,Arrays.asList("LETTER", Character.toString((char)(i+65)), "RED"));
            fiducialDictionary.put(i+27,Arrays.asList("LETTER", Character.toString((char)(i+65)), "BLUE"));
        }
        
        //// TUIO ////
        
        font = createFont("Arial", 18);
        selectorFont = loadFont("KristenITC-Regular-48.vlw");
        scale_factor = height/table_size;
        tuioClient  = new TuioProcessing(this);
        
        //////////////
        
    }
    
    public void reInit() {
        
        currentProfiles.clear();
        currentChoice = "";
        currentSelection.clear();
        cursorPosition = null;
        choiceButtons.clear();
        selected = false;
        saves.clear();
        fillInTheBlanks_ThemeImageWords.clear();
        fillInTheBlanks_gapCoords.clear();
        fillInTheBlanks_WordList.clear();
        oldFiducials.clear();

        game = "";
        theme = "";
        word = "";
        createProfile = "";
        profilesColours.clear();
        wordGuessed.clear();
        img = null;
        
        adviceGif = null;
        wordGif = null;
        successMovie = null;

        init = true;
        next = true;
        
    }
    

    @Override
    public void draw() {

        image(menuImage, 0, 0, screensizex, screensizey);
        
        if (stage == 1) { //Choose Game

            if (selector(0,1,1,false))
                stage = 2;

        } else if (stage == 2) {

            switch(game) { // Choose Profile(s)
                
                case "WORD FILL":   if (selector(1,1,2,true))
                                        stage = 3;
                                    break;
                case "DICTIONARY": if (selector(1,1,1,false))
                                        stage = 3;
                                    break;
                
            }
            
        } else if (stage == 3) { //Choose Theme or Play
            
            switch(game) {
                
                case "WORD FILL":   if (selector(2,1,1,false))
                                        stage = 4;
                                    break;
                    
                case "DICTIONARY":  VocabList();
                                    break;
                
            }

        } else if (stage == 4) { //Play
            
            switch(game) {
                
                case "WORD FILL":   profilesColours = new HashMap();
                                    profilesColours.put(currentProfiles.get(0),"RED");
                                    if (currentProfiles.size() > 1)
                                        profilesColours.put(currentProfiles.get(1),"BLUE");
                                    
                                    printScores();
                                    
                                    FillInTheBlanksGame();
                                    break;
                
            }

        }
        
        //// TUIO ////
        textFont(font,18*scale_factor);
        float obj_size = object_size*scale_factor;
        float cur_size = cursor_size*scale_factor;

        ArrayList<TuioObject> tuioObjectList = tuioClient.getTuioObjectList();
        for (int i=0;i<tuioObjectList.size();i++) {
            TuioObject tobj = tuioObjectList.get(i);
            stroke(0);
            fill(0,0,0);
            pushMatrix();
            translate(tobj.getScreenX(width),tobj.getScreenY(height));
            rotate(tobj.getAngle());
            rect(-obj_size/2,-obj_size/2,obj_size,obj_size);
            popMatrix();
            fill(255);
            text(""+tobj.getSymbolID(), tobj.getScreenX(width), tobj.getScreenY(height));
        }
        //////////////

    }
    
    public String toTitleCase(String sentence) {
        
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : sentence.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
        
    }
    
    public void addTitle(String choice) {
        
        PImage cloud = loadImage("/images/cloud.png");
        int cloudWidth = 600;
        int cloudHeight = 230;
        
        image(cloud, (width/2)-(cloudWidth/2), height/5-(cloudHeight/2)-10, cloudWidth, cloudHeight);
        textFont(selectorFont,60);
        fill(0, 0, 0);
        textAlign(CENTER);
        text(choice, width/2, height/5);
        textFont(font);
        
    }
    
    public void printWord(String word, int size, int letterHeight, int opacity, boolean checkChosen) {
        
        if (checkChosen) {
            
            if (currentSelection.contains(word)) {
                
                String longestWord = "";
                
                for(String sel : currentSelection)
                    if (longestWord.length() < sel.length())
                        longestWord = ""+sel;
                
                fill(255,0,0,(127*opacity)/100);
                
                int letterWidth = (width/2)-(size*(longestWord.length()/2));
                    if ((longestWord.length())%2 != 0) letterWidth -= (size/2);
                
                rect(letterWidth-15,letterHeight-15,(size*longestWord.length())+30,size+30);
                fill(255,255,255);
                
            }
            
        }
        
        for(int i = 0; i < word.length(); i++) { 
            
            char c = word.charAt(i);
            
            if (!Character.isWhitespace(c)) {
            
                float letterWidth = (width/2)-(size*(word.length()/2))+(size*i);
                    if ((word.length())%2 != 0) letterWidth -= (size/2);

                PImage letterBack = loadImage("/images/Letters_Numbers/"+(""+c).toUpperCase()+".png");
                tint(255, (int)((opacity*255)/100));
                image(letterBack, letterWidth, letterHeight, size, size);
                tint(255, 255);
            }
            
        }
        
    }
    
    public void printScores() {
        
        int size = 40;
        int count = 0;
        
        float splitWidth = width/(profilesColours.size()+1);
        
        String longestWord = "";
        for (String profileName : profilesColours.keySet())
            if (longestWord.length() < profileName.length())
                longestWord = ""+profileName;
        
        float letterHeight = height-(size*2)-20;
        float letterWidth = (width/2)-(size*(longestWord.length()/2));
                if ((longestWord.length())%2 != 0) letterWidth -= (size/2);

       
        for (Map.Entry<String, String> entry : profilesColours.entrySet()) {
            
            String profileName = entry.getKey();
            String colour = entry.getValue();
            String points = config.getPoints(profileName);
            
            if (colour.equals("RED"))
                fill(255,0,0,180);
            else if (colour.equals("BLUE"))
                fill(0,0,255,180);
            
            rect(letterWidth-15,letterHeight-15,(size*longestWord.length())+30,height-(letterHeight-15));
            fill(255,255,255,255);
                    
            for(int i = 0; i < profileName.length(); i++) { 
            
                char c = profileName.charAt(i);

                if (!Character.isWhitespace(c)) {
                    
                    letterWidth = (splitWidth*(count+1))-(size*(profileName.length()/2))+(size*i);
                        if ((profileName.length())%2 != 0) letterWidth -= (size/2);

                    PImage letterBack = loadImage("/images/Letters_Numbers/"+(""+c).toUpperCase()+".png");
                    tint(255, 255);
                    image(letterBack, letterWidth, letterHeight, size, size);
                    
                }

            }
            
            for(int i = 0; i < points.length(); i++) { 
            
                char c = points.charAt(i);

                if (!Character.isWhitespace(c)) {
                
                    letterHeight = height-size-10;
                    letterWidth = (splitWidth*(count+1))-(size*(points.length()/2))+(size*i);
                        if ((points.length())%2 != 0) letterWidth -= (size/2);

                    PImage letterBack = loadImage("/images/Letters_Numbers/"+(""+c).toUpperCase()+".png");
                    tint(255, 255);
                    image(letterBack, letterWidth, letterHeight, size, size);
                    
                }

            }
            
            count++;
            
        }
        
    }
    
    public boolean selector(int sel, int minChoice, int maxChoice, boolean addChoices) {
        
        HashMap<List<String>,List<Float>> fiducials = checkFiducials();
        choiceButtons.clear();
        
        List<String> choices = new ArrayList();
        int size = 70;
        float buttonSize = 100;
        
        boolean done = false;
        
        boolean addMenu = false;
        
        if (addChoices) {
            
            PImage add = loadImage("images/add.png");
            image(add, (width/2)-(buttonSize/2), (height/2)-(size/2)-(size*3)-buttonSize, buttonSize, buttonSize);
            choiceButtons.put("add", Arrays.asList((width/2)-(buttonSize/2),(float)(height/2)-(size/2)-(size*3)-buttonSize,buttonSize,buttonSize));
            
            if (checkChoice("add",fiducials)) {
            
                addMenu = true;
                cursorPosition = null;
                createProfile();
            
            } else if (!createProfile.isEmpty()) {
                
                //Save text file with new info
                speak(createProfile);
                config.createProfile(createProfile);
                createProfile = "";
                
            }
            
        }
        
        if (!addMenu) {
        
            switch(sel) {

                case 0: //Select Game
                        addTitle("Select Game");
                        choices = new ArrayList(Arrays.asList("WORD FILL", "DICTIONARY"));
                        break;
                case 1: //Select Profiles
                        addTitle("Select Profiles");
                        currentProfiles.clear();
                        currentProfiles.addAll(currentSelection);
                        choices = new ArrayList(config.loadAllProfiles().keySet());
                        if (choices.isEmpty()) {
                            currentChoice = "";
                            currentSelection.clear();
                            return true;
                        }
                        break;
                case 2: //Select Theme
                        addTitle("Select Theme");
                        choices = new ArrayList(config.loadFillInTheBlanks().keySet());
                        break;

            }

            if (currentChoice.equals(""))
                currentChoice = choices.get(0);
            int pos = choices.indexOf(currentChoice);
            if (pos > 0)
                printWord(choices.get(pos-1),size,(height/2)-(size/2)-(int)((size)*1.5),50,true);
            printWord(currentChoice,size,(height/2)-(size/2),100,true);
            if (pos < choices.size()-1)
                printWord(choices.get(pos+1),size,(height/2)-(size/2)+(int)((size)*1.5),50,true);

            PImage scroll = loadImage("images/scroll.png");
            PImage select = loadImage("images/select.png");
            PImage remove = loadImage("images/remove.png");
            PImage play = loadImage("images/play.png");
            float buttonWidth = (width)-(buttonSize*2);
            float buttonHeight = (height/2)-(buttonSize/2);

            if (!currentSelection.contains(currentChoice)) {
                if(currentSelection.size() < maxChoice) {
                    image(select, buttonWidth, buttonHeight, buttonSize, buttonSize);
                    choiceButtons.put("select", Arrays.asList(buttonWidth,buttonHeight,buttonSize,buttonSize));
                }
            } else {
                image(remove, buttonWidth, buttonHeight, buttonSize, buttonSize);
                choiceButtons.put("remove", Arrays.asList(buttonWidth,buttonHeight,buttonSize,buttonSize));
            }

            buttonWidth = (width)-(buttonSize);
            image(scroll, buttonWidth, buttonHeight, buttonSize, buttonSize);
            choiceButtons.put("scroll", Arrays.asList(buttonWidth,buttonHeight,buttonSize,buttonSize));

            if (checkChoice("scroll",fiducials)) {

                selected = false;
                int scrollUpDown = checkScroll(fiducials); //1 > UP, 2 > DOWN

                if (scrollUpDown == 1) {

                    int newChoice = choices.indexOf(currentChoice)-1;

                    if (newChoice == -1)
                        newChoice = choices.size()-1;

                    currentChoice = choices.get(newChoice);

                } else if (scrollUpDown == 2) {

                    int newChoice = choices.indexOf(currentChoice)+1;

                    if (newChoice == choices.size())
                        newChoice = 0;

                    currentChoice = choices.get(newChoice);

                }

            } else if (!currentSelection.contains(currentChoice) && !selected && checkChoice("select",fiducials)) {

                selected = true;
                cursorPosition = null;
                if (currentSelection.indexOf(currentChoice) == -1)
                    currentSelection.add(currentChoice);

            } else if (currentSelection.contains(currentChoice) && !selected && checkChoice("remove",fiducials)) {

                selected = true;
                cursorPosition = null;
                currentSelection.remove(currentChoice);

            } else {

                selected = false;
                cursorPosition = null;

            }

            if (currentSelection.size() >= minChoice) {
                buttonWidth = (width/2)-(buttonSize/2);
                buttonHeight = (height/2)-(size/2)+(int)((size)*1.5)+150;
                image(play, buttonWidth, buttonHeight, buttonSize, buttonSize);
                choiceButtons.put("play", Arrays.asList(buttonWidth,buttonHeight,buttonSize,buttonSize));
            }

            if ((!done) && (currentSelection.size() >= minChoice) && (checkChoice("play",fiducials)))
                done = true;

            if (done) {

                switch(sel) {

                    case 0: //Select Game
                            game = currentSelection.get(0);
                            currentChoice = "";
                            currentSelection.clear();
                            return true;
                    case 1: //Select Profiles
                            currentChoice = "";
                            currentSelection.clear();
                            return true;
                    case 2: //Select Theme
                            theme = currentSelection.get(0);
                            currentChoice = "";
                            currentSelection.clear();
                            return true;

                }
            }
            
        }
        
        return false;
        
    }

    public void createProfile() {

        //Place fiducials on table to create name
        
        createProfile = "";
        
        HashMap<List<String>,List<Float>> fiducials = checkFiducials();
        
        Map<List<String>,List<Float>> sortedfiducials = fiducials.entrySet().stream()
                                            .sorted(Entry.comparingByValue((v1,v2)->(v1.get(0)).compareTo(v2.get(0))))
                                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                                    (e1, e2) -> e1, LinkedHashMap::new));
        
        
        for (Map.Entry<List<String>,List<Float>> entry : sortedfiducials.entrySet()) {
            
            List<String> fiducial = entry.getKey();
            List<Float> position = entry.getValue();
            
            if (fiducial.get(0).equals("LETTER"))
                createProfile = createProfile.concat(fiducial.get(1));
            
        }
        
        int size = 70;
        
        printWord(createProfile,size,(height/2)-(size/2),100,true);

    }
    
    public void speakNewLetters(HashMap<List<String>,List<Float>> oldFiducials, HashMap<List<String>,List<Float>> fiducials) {
        
        if (!oldFiducials.isEmpty() && (oldFiducials.size() < fiducials.size())) {
                
            Set<List<String>> oldKeys = oldFiducials.keySet();
            Set<List<String>> newKeys = fiducials.keySet();

            List<String> lettersDiff = new ArrayList();

            for(List<String> key : newKeys)
                lettersDiff.add((key.get(1)).trim());

            for(List<String> key : oldKeys)
                lettersDiff.remove((key.get(1)).trim());

            for(String newLetter : lettersDiff)
                speak(newLetter);
                    
        }
        
    }

    public void FillInTheBlanksGame() {
        
        List<String>profiles = new ArrayList(profilesColours.keySet());
        wordGuessed.clear();
        
        if (init) {
            
            fillInTheBlanks_ThemeImageWords = config.loadFillInTheBlanks().get(theme);
            fillInTheBlanks_WordList = config.getIdealWordOrder(profiles,new ArrayList(fillInTheBlanks_ThemeImageWords.keySet())); //List words and images (preferably not already in vocab list)
            init = false;
            
        }
        
        if (next) {
            
            if (wordGif == null) {
            
                if (fillInTheBlanks_WordList.isEmpty() && successMovie == null) {
                    //GAME WIN
                    successMovie = new Movie(this, theme+".mov");
                    successMovie.play();
                } else {
                    word = fillInTheBlanks_WordList.get(0);
                    img = loadImage(fillInTheBlanks_ThemeImageWords.get(word),"jpg");
                    fillInTheBlanks_gapCoords.clear();
                }
                
            } else {
                
                image(wordGif, width/2 - wordGif.width/2, height / 2 - wordGif.height / 2);
                if (!wordGif.isPlaying())
                    wordGif = null;
                
            }         
            
        }
        
        if (successMovie != null) {
            
            if (successMovie.available()) {
                image(successMovie, (width/2)-(successMovie.width/2), (height/2)-(successMovie.height/2));
            } else {
                stage = 1;
                reInit();
            }
            
            
        }
        
        if (stage != 1 && wordGif == null && !fillInTheBlanks_WordList.isEmpty()) {
            
            HashMap<List<String>,List<Float>> fiducials = checkFiducials();
            
            speakNewLetters(oldFiducials,fiducials);
            
            choiceButtons.clear();

            PImage tip = loadImage("images/fillInTheBlanksTip.png");
            image(tip, width-tip.width-(width/7), height-tip.height-(height/7));
            
            PImage close = loadImage("images/remove.png");
            int size = 70;
            float buttonSize = 200;
            float buttonWidth = (width)-buttonSize;
            float buttonHeight = 0;

            image(close, buttonWidth, buttonHeight, buttonSize, buttonSize);
            choiceButtons.put("close", Arrays.asList(buttonWidth,buttonHeight,buttonSize,buttonSize));

            if (checkChoice("close",fiducials)) {

                cursorPosition = null;
                stage = 1;
                
            } else {

                createImageContainer(300,300);

                next = true;

                for(int i = 0; i < word.length(); i++)
                    if ((createLetterContainer(i,word,100,30) != 1) && (next == true)) next = false;

                if (next) { //Check if word is correct

                    for(String profile : profiles) { //If 2P Mode then check which fiducials belong to whom

                        String colour = profilesColours.get(profile);
                        int points = 0;

                        for (String value : wordGuessed.values())
                            if (value.equals(colour)) points++;

                        config.addPointsAddWord(profile,points*10,word);

                    }
                    
                    speak(word);
                    
                    wordGif = new Gif(this, config.getFillInTheBlanksGifPath(theme,word));
                    wordGif.play();
                    wordGif.ignoreRepeat();

                    fillInTheBlanks_WordList.remove(word);

                } //next word
                
            }
            
            oldFiducials.clear();
            oldFiducials = new HashMap(fiducials);
            
        }
        
        
        
        
        //Increment points accordingly and save to text file
        //Next word (10 words per session) + Extra bonus points for completing session
        
    }

    public void StoryModeGame() {

        HashMap<String, HashMap<String, String>> themeImageWords = config.loadFillInTheBlanks();

        //Choose theme
        //Start game
        //Display word and image (preferably not already in vocab list)
        //Check if word is correct
        //If 2P Mode then check which fiducials belong to whom
        //Increment points and save to text file
        //Next word (10 words per session)
    }

    public void VocabList() {

        //User scrolls through his own vocab list
        //He can view associated picture and an example sentence where that word is used
    }
    
    public void createImageContainer(int imageFrameWidth, int imageFrameHeight) {
        
        if (img.width > img.height) imageFrameHeight = (int)((float)((float)imageFrameWidth/img.width)*img.height);
        else if (img.width < img.height) imageFrameWidth = (int)((float)((float)imageFrameHeight/img.height)*img.width);

        image(img, (width/2)-(imageFrameWidth/2), (height/2)-(imageFrameHeight/2)-150, imageFrameWidth, imageFrameHeight);
        
    }
    
    public int checkState(int index, String word) {
        
        HashMap<List<String>,List<Float>> detected = checkFiducials();
        
        for (Map.Entry<List<String>,List<Float>> entry : detected.entrySet()) {
            
            List<String> key = entry.getKey();
            List<Float> value = entry.getValue();
            
            List<Float> gapCoords = fillInTheBlanks_gapCoords.get(index);
                
             if (((value.get(0) > gapCoords.get(0)) && (value.get(0) < (gapCoords.get(0)+gapCoords.get(2)))) && // X Coordinate
                ((value.get(1) > gapCoords.get(1)) && (value.get(1) < (gapCoords.get(1)+gapCoords.get(3))))) { // Y Coordinate

                if (key.get(0).equals("LETTER") && key.get(1).equals(""+word.toUpperCase().charAt(index))) { // Match

                    if ((value.get(2) > 6) || (value.get(2) < 0.3)) { //Proper Alignment
                        
                        if (profilesColours.containsValue(key.get(2))) {
                            wordGuessed.put(key.get(1),key.get(2));
                            return 1;
                        } else return 2; //Colour not used for current profiles
                        
                    } else return 2; //Incorrect letter alignment

                } else return 3;

            }
            
        }
        
        return 0;
        
    }
    
    public int createLetterContainer(int index, String word, int letterBackSize, int letterBackSpacing) {
        
        String imagePath = "";
        
        float size = letterBackSize;
        
        float letterWidth = (width/2)-(size*(word.length()/2))+(size*index);
            if ((word.length())%2 != 0) letterWidth -= (size/2);
        float letterHeight = (height/2)-(size/2)+100;
        
        fillInTheBlanks_gapCoords.put(index, Arrays.asList(letterWidth,letterHeight,size,size));
        
        
        int state = checkState(index,word);
        
        switch(state) {
            
            case 0: imagePath = "/images/letter_background_default.png";
                break;
            case 1: imagePath = "/images/letter_background_correct.png";
                break;
            case 2: imagePath = "/images/letter_background_turn.png";
                break;
            case 3: imagePath = "/images/letter_background_incorrect.png";
                break;
            
        }
        
        PImage letterBack = loadImage(imagePath);
        
        image(letterBack, letterWidth, letterHeight, size, size);
        
        return state;
        
    }

    public HashMap<List<String>,List<Float>> checkFiducials() {
        
        HashMap<List<String>,List<Float>> detected = new HashMap();
        
        ArrayList<TuioObject> tuioObjectList = tuioClient.getTuioObjectList();
        
        for (TuioObject to : tuioObjectList)
            detected.put(fiducialDictionary.get(to.getSymbolID()),Arrays.asList((float)to.getScreenX(width),(float)to.getScreenY(height),to.getAngle()));

        return detected;

    }
    
    public boolean checkChoice(String button, HashMap<List<String>,List<Float>> fiducials) {
        
        List<Float> elementPosition = choiceButtons.get(button);
        List<Float> cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
        
        if ((cursorPosition != null) && (elementPosition != null)) {
            if (((cursorPosition.get(0) > elementPosition.get(0)) && (cursorPosition.get(0) < (elementPosition.get(0)+elementPosition.get(2)))) && // X Coordinate
                ((cursorPosition.get(1) > elementPosition.get(1)) && (cursorPosition.get(1) < (elementPosition.get(1)+elementPosition.get(3))))) { // Y Coordinate

                return true;

            }
        }
        
        return false;
        
    }
    
    public int checkScroll(HashMap<List<String>,List<Float>> fiducials) {
        
        List<Float> newCursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
        
        if (newCursorPosition != null) {
            
            if (cursorPosition == null)
                cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
        
            if (newCursorPosition.get(2) > (cursorPosition.get(2)+0.5)) {
                
                if ((newCursorPosition.get(2)-cursorPosition.get(2)) > 3) {
                    
                    cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
                    return 1;
                    
                }

                cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
                
                //scroll down
                return 2;

            } else if (newCursorPosition.get(2) < (cursorPosition.get(2)-0.5)) {
                
                if ((cursorPosition.get(2)-newCursorPosition.get(2)) > 3) {
                    
                    cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
                    return 2;
                    
                }
                
                cursorPosition = fiducials.get(Arrays.asList("CURSOR", "MENU"));
                    
                //scroll up
                return 1;
                
            }
            
        } else {
            
            cursorPosition = null;
            
        }
        
        return 0;
        
    }
    
    public void speak(String text) {
        File ttsDir = new File(""+new File("").getAbsolutePath()+"\\tts");
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("cmd /c echo "+text+" > \"text.txt\"",new String[]{},ttsDir);
            pr = rt.exec("cscript speak.vbs",new String[]{},ttsDir);
        } catch (Exception e) {e.printStackTrace();}
    }
    
    public void movieEvent(Movie m) {
        m.read();
    }

    
    //// TUIO ////
    
    public void addTuioObject(TuioObject tobj) {
      if (verbose) println("add obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle());
    }

    public void updateTuioObject (TuioObject tobj) {
      if (verbose) println("set obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle()
              +" "+tobj.getMotionSpeed()+" "+tobj.getRotationSpeed()+" "+tobj.getMotionAccel()+" "+tobj.getRotationAccel());
    }

    public void removeTuioObject(TuioObject tobj) {
      if (verbose) println("del obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+")");
    }

    public void addTuioCursor(TuioCursor tcur) {
      if (verbose) println("add cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY());
      //redraw();
    }

    public void updateTuioCursor (TuioCursor tcur) {
      if (verbose) println("set cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY()
              +" "+tcur.getMotionSpeed()+" "+tcur.getMotionAccel());
      //redraw();
    }

    public void removeTuioCursor(TuioCursor tcur) {
      if (verbose) println("del cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+")");
      //redraw()
    }
    
    public void addTuioBlob(TuioBlob tblb) {
      if (verbose) println("add blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+") "+tblb.getX()+" "+tblb.getY()+" "+tblb.getAngle()+" "+tblb.getWidth()+" "+tblb.getHeight()+" "+tblb.getArea());
      //redraw();
    }

    public void updateTuioBlob (TuioBlob tblb) {
      if (verbose) println("set blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+") "+tblb.getX()+" "+tblb.getY()+" "+tblb.getAngle()+" "+tblb.getWidth()+" "+tblb.getHeight()+" "+tblb.getArea()
              +" "+tblb.getMotionSpeed()+" "+tblb.getRotationSpeed()+" "+tblb.getMotionAccel()+" "+tblb.getRotationAccel());
      //redraw()
    }

    public void removeTuioBlob(TuioBlob tblb) {
      if (verbose) println("del blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+")");
      //redraw()
    }

    public void refresh(TuioTime frameTime) {
      if (verbose) println("frame #"+frameTime.getFrameID()+" ("+frameTime.getTotalMilliseconds()+")");
      if (callback) redraw();
    }

    //////////////
    
}
