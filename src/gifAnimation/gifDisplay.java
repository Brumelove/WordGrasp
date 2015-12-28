/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gifAnimation;

import gifAnimation.*;
import java.io.File;
import processing.core.*;

public class gifDisplay extends PApplet {
 
    /*
* Demonstrates the use of the GifAnimation library.
* the left animation is looping, the one in the middle 
* plays once on mouse click and the one in the right
* is a PImage array. 
* the first two pause if you hit the spacebar.
*/



PImage[] animation;
Gif loopingGif;
Gif nonLoopingGif;
boolean pause = false;

String projectDir = new File("").getAbsolutePath();
String gifDir = projectDir + "\\src\\gifAnimation\\";

public void settings() {
  size(400, 200);
}

public void setup() {
  frameRate(100);
  
  println("gifAnimation " + Gif.version());
  // create the GifAnimation object for playback
  loopingGif = new Gif(this, gifDir+"lavalamp.gif");
  loopingGif.loop();
  nonLoopingGif = new Gif(this, gifDir+"lavalamp.gif");
  nonLoopingGif.play();
  nonLoopingGif.ignoreRepeat();
  // create the PImage array for the interactive display
  animation = Gif.getPImages(this, gifDir+"lavalamp.gif");
}

public void draw() {
    background(255 / (float)height * mouseY);
    image(loopingGif, 10, height / 2 - loopingGif.height / 2);
    image(nonLoopingGif, width/2 - nonLoopingGif.width/2, height / 2 - nonLoopingGif.height / 2);
    image(animation[(int) (animation.length / (float) (width) * mouseX)], width - 10 - animation[0].width, height / 2 - animation[0].height / 2);
}

public void mousePressed() {
  nonLoopingGif.play();
}

public void keyPressed() {
  if (key == ' ') {
    if (pause) {
      nonLoopingGif.play();
      loopingGif.play();
      pause = false;
    } 
    else {
      nonLoopingGif.pause();
      loopingGif.pause();
      pause = true;
    }
  }
}
    
    
}
