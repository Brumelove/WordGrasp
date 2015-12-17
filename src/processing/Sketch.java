package processing;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import processing.core.*;

public class Sketch extends PApplet {

    PImage menuImage;
    PFont welcome;
    int screensizex, screensizey, stage;
    int time;
    int welcomeDelay = 3000;
    ConfigLoaderSaver config = new ConfigLoaderSaver();
    HashMap<String, Multimap<String, String>> currentProfiles = new HashMap<>();
    HashMap<String, String> saves;

    @Override
    public void settings() {

        fullScreen();

    }

    @Override
    public void setup() {
        stage = 0;
        screensizex = width;
        screensizey = height;
        menuImage = loadImage("menu.jpg");
        image(menuImage, 0, 0, screensizex, screensizey);
        welcome = loadFont("ArialMT-48.vlw");
        textFont(welcome);
        time = millis();
    }

    @Override
    public void draw() {

        if (stage == 0) {
            textAlign(CENTER);
            text("WELCOME", width / 2, height / 2);
            if ((millis() - time) >= welcomeDelay) {
                stage = 1;
                image(menuImage, 0, 0, screensizex, screensizey);
                textFont(welcome, 24);
                time = millis();//also update the stored time
                //background(1);
                //image(menuImage,0,0,screensizex,screensizey);
            }
        } else if (stage == 1) {
            /*saves = (new ConfigLoader()).load();
             while(totalx<screensizeX && totaly<screensizeY) {


             }*/

        //rect(1,2,200,300);    
        //rect = width/90;
        //rect()
            textAlign(CENTER);
            text("WORDSEARCH \nPress any key to START GAME", width / 2, height / 2);

            if (keyPressed) {
                stage = 2;
            }

        } else if (stage == 2) {

            choose1Por2PMode();

        }

    }

    public void MainMenu() {

        //choose game
        //1) Fill In the Blanks
        //2) Story Mode
        //3) Vocabulary List
    }

    public void choose1Por2PMode() {

        int boxSide = 300;

        rect((width / 2) - boxSide - 40, (height / 2) - (boxSide / 2), boxSide, boxSide, 30);
        rect((width / 2) + 40, (height / 2) - (boxSide / 2), boxSide, boxSide, 30);

    }

    public void chooseProfile() {

        HashMap<String, Multimap<String, String>> profiles = config.loadProfiles();

        //print profile names with letters
        //user can scroll through profile names to choose or create a new profile
        //points will be written under each profile
        
        currentProfiles.clear();
        //for each chosen profile do -> currentProfiles.put(chosenProfile,profiles.get(chosenProfile));
    }

    public void createProfile() {

        //Place fiducials on table to create name
        String profile = "";

        //Save text file with new info
        config.createProfile(profile);

    }

    public void FillInTheBlanksGame() {

        HashMap<String, HashMap<String, String>> themeImageWords = config.loadFillInTheBlanks();
        
        //Choose theme
        //Start game
        
        String game = "fillintheblanks";
        String theme = "animals";
        
        HashMap<String, String> wordImages = themeImageWords.get(theme);

        String word = "cat";
        
        int imageFrameWidth = 300;
        int imageFrameHeight = 300;
        
        PImage img = loadImage(wordImages.get(word));
        if (img.width > img.height) imageFrameHeight = (int)((float)((float)imageFrameWidth/img.width)*img.height);
        else if (img.width < img.height) imageFrameWidth = (int)((float)((float)imageFrameHeight/img.height)*img.width);
        
        image(img, (width/2)-imageFrameWidth, (height/2)-(imageFrameHeight/2)-80, imageFrameWidth, imageFrameHeight);
        
        PImage letterBack = loadImage("/images/letter_background_default.png");
        
        int letterBackSize = 100;
        
        for(int i = 0; i < word.length(); i++) {
            
            image(letterBack, (width/2)-(letterBackSize/2), (height/2)-(letterBackSize/2)+20, 100, 100);
            
        }
        
        
        //Display word and image (preferably not already in vocab list)
        //Check if word is correct
        //If 2P Mode then check which fiducials belong to whom
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

    public List<String> checkFiducial() {

        //check it
        return Arrays.asList("LETTER", "A", "RED"); //eg

    }

    public HashMap<int[], List<String>> checkFiducials() {

        //check it
        int[] coords = new int[2];

        coords[0] = 0; //width
        coords[1] = 0; //height

        List<String> fiducial = Arrays.asList("LETTER", "A", "RED");

        HashMap<int[], List<String>> result = new HashMap<>();
        result.put(coords, fiducial);

        return result; //eg

    }

}
