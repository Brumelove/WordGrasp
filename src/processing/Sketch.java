package processing;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import processing.core.*;
import TUIO.*;
import java.util.Map;

public class Sketch extends PApplet {

    PImage menuImage;
    PFont welcome;
    int screensizex, screensizey, stage;
    int time;
    int welcomeDelay = 3000;
    
    HashMap<Integer, List<String>> fiducialDictionary = new HashMap();
    
    ConfigLoaderSaver config = new ConfigLoaderSaver();
    HashMap<String, Multimap<String, String>> currentProfiles = config.loadProfiles();
    HashMap<String, String> saves;
    HashMap<String, HashMap<String, String>> fillInTheBlanks_ThemeImageWords = config.loadFillInTheBlanks();
    HashMap<Integer, List<Float>> fillInTheBlanks_gapCoords = new HashMap();
    
    String word = "";
    PImage img = null;
    
    //// TUIO ////
    
    TuioProcessing tuioClient;

    float cursor_size = 15;
    float object_size = 60;
    float table_size = 760;
    float scale_factor = 1;
    PFont font;

    boolean verbose = false;
    boolean callback = true;
    
    //////////////
    
    boolean init = true;

    @Override
    public void settings() {

        fullScreen();

    }

    @Override
    public void setup() {
        stage = 2;
        screensizex = width;
        screensizey = height;
        menuImage = loadImage("menu.jpg");
        image(menuImage, 0, 0, screensizex, screensizey);
        welcome = loadFont("ArialMT-48.vlw");
        textFont(welcome);
        time = millis();
        
        fiducialDictionary.put(0,Arrays.asList("CURSOR", "MENU"));
        
        for(int i = 0; i < 26; i++) { //1 - 52
            fiducialDictionary.put(i+1,Arrays.asList("LETTER", Character.toString((char)(i+65)), "RED"));
            fiducialDictionary.put(i+27,Arrays.asList("LETTER", Character.toString((char)(i+65)), "BLUE"));
        }
        
        //// TUIO ////
        
        if (!callback) {
          frameRate(60);
          loop();
        } else noLoop();
        
        font = createFont("Arial", 18);
        scale_factor = height/table_size;
        tuioClient  = new TuioProcessing(this);
        
        //////////////
        
    }

    @Override
    public void draw() {

        image(menuImage, 0, 0, screensizex, screensizey);
        
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
        
        if (stage == 0) {
            textAlign(CENTER);
            text("WELCOME", width / 2, height / 2);
            if ((millis() - time) >= welcomeDelay) {
                stage = 2;
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

            //if (keyPressed) {
                stage = 2;
            //}

        } else if (stage == 2) {

            //choose1Por2PMode();
            FillInTheBlanksGame();

        }

    }

    public void MainMenu() {

        //choose game
        //1) Fill In the Blanks
        //2) Story Mode
        //3) Vocabulary List
        
        
        /*
        String game;
        
        switch(game) {
        
            case "FillInTheBlanks" : FillInTheBlanksGame();
                
        }
        
        */
    }

    public void choose1Por2PMode() {

        int boxSide = 300;

        rect((width / 2) - boxSide - 40, (height / 2) - (boxSide / 2), boxSide, boxSide, 30);
        rect((width / 2) + 40, (height / 2) - (boxSide / 2), boxSide, boxSide, 30);

    }

    public void chooseProfile() {

        //HashMap<String, Multimap<String, String>> profiles = config.loadProfiles();

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

        int state = 0;
        
        if (init) {
        
            //HashMap<String, HashMap<String, String>> themeImageWords = config.loadFillInTheBlanks();

            //Choose theme
            //Start game

            fillInTheBlanks_gapCoords.clear();
            
            String game = "fillintheblanks";
            String theme = "animals";
            word = "lion";

            HashMap<String, String> wordImages = fillInTheBlanks_ThemeImageWords.get(theme);
            img = loadImage(wordImages.get(word),"jpg");
            
        }
        
        createImageContainer(300,300);
        
        for(int i = 0; i < word.length(); i++)
            createLetterContainer(i,word,100,30);
        
        init = false;
        
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
    
    public void createImageContainer(int imageFrameWidth, int imageFrameHeight) {
        
        if (img.width > img.height) imageFrameHeight = (int)((float)((float)imageFrameWidth/img.width)*img.height);
        else if (img.width < img.height) imageFrameWidth = (int)((float)((float)imageFrameHeight/img.height)*img.width);

        image(img, (width/2)-(imageFrameWidth/2), (height/2)-(imageFrameHeight/2)-150, imageFrameWidth, imageFrameHeight);
        
    }
    
    public void createLetterContainer(int index, String word, int letterBackSize, int letterBackSpacing) {
        
        String imagePath = "";
        
        float size = letterBackSize;
        /*float space = letterBackSpacing;
        
        float centreSpace = 0;
        if ((word.length())%2 == 0) centreSpace = space;
        */
        
        float letterWidth = (width/2)-(size*(word.length()/2))+(size*index);
            if ((word.length())%2 != 0) letterWidth -= (size/2);
        float letterHeight = (height/2)-(size/2)+100;
        
        if (init)
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

                    String colour = key.get(2);

                    if ((value.get(2) > 6) || (value.get(2) < 0.3)) return 1; //Proper Alignment
                    else return 2;

                } else return 3;

            }
            
        }
        
        return 0;
        
    }
    
    public List<String> checkFiducial() {

        HashMap<List<String>,List<Float>> detected = checkFiducials();
        
        
        return Arrays.asList("LETTER", "A", "RED"); //eg

    }

    public HashMap<List<String>,List<Float>> checkFiducials() {
        
        HashMap<List<String>,List<Float>> detected = new HashMap();
        
        ArrayList<TuioObject> tuioObjectList = tuioClient.getTuioObjectList();
        for (TuioObject to : tuioObjectList)
            detected.put(fiducialDictionary.get(to.getSymbolID()),Arrays.asList((float)to.getScreenX(width),(float)to.getScreenY(height),to.getAngle()));

        return detected; //eg

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
