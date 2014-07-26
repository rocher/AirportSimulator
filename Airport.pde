/*
 *  Copyright 2014 Francesc Rocher
 *
 *  Aiport Simulator is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Aiport Simulator is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with Airport Simulator.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.jbox2d.util.nonconvex.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.testbed.*;
import org.jbox2d.collision.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.p5.*;
import org.jbox2d.dynamics.*;

import ddf.minim.spi.*;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import ddf.minim.effects.*;

Physics physics;
CollisionDetector detector;

Location [] locations;
Airplane [] airplanes;
Location [] throttleArea;
Location [] flightArea;

PImage airport;
PImage airportNight;

Controller controller;
Helper helper;


int P = -1; // airplane selected, -1 == none
Boolean changeSpeed = false;
Boolean keyShift = false;
Boolean keyControl = false;
Boolean gameStarted = false;

void setup()
{
  size(1200, 800);
  frameRate(60);

  imageMode(CENTER);
  airport = loadImage("airport.png");
  airportNight = loadImage("airport-night.png");

  physics = new Physics(this, width, height, 0, 0, width*2, height*2, width, height, 100);
  physics.setCustomRenderingMethod(this, "airportRenderer");
  physics.setDensity(10.0);

  locations = new Location[9];
  locations[0] = new Location(969,  64, 3.599);
  locations[1] = new Location(566,  70, 5.149);
  locations[2] = new Location(423, 718, 5.969);
  locations[3] = new Location( 86, 579, 5.157);
  locations[4] = new Location(305, 749, 0.444);
  locations[5] = new Location(401, 316, 3.568);
  locations[6] = new Location(200, 405, 3.568);
  locations[7] = new Location(818, 122, 3.568);
  locations[8] = new Location(342, 200, 3.568);


  Airplane.init(this);
  airplanes = new Airplane[9];
  for(int i = 0; i < airplanes.length; i++)
    airplanes[i] = new Airplane(this, physics, i);

  throttleArea = new Location[2];
  throttleArea[0] = new Location(130, 674, 66); // the angle is used as a radius
  throttleArea[1] = new Location(514, 749, 50);

  flightArea = new Location[2];
  flightArea[0] = new Location(1153, 204, 0.43); // dist(1153,204, 1199,188));
  flightArea[1] = new Location(755, 35, 1.25); // dist(755,35, 770,0));

 //  airplanes[0].startAt(locations[4]);
  //.del airplanes[1] = new Airplane(physics, locations[1], Airplane.Size.MEDIUM, Airplane.Color.GREEN);
  //.del airplanes[2] = new Airplane(physics, locations[2], Airplane.Size.SMALL, Airplane.Color.BLUE);
  //.del airplanes[3] = new Airplane(physics, locations[3], Airplane.Size.BIG, Airplane.Color.CYAN);
  //.del airplanes[4] = new Airplane(physics, locations[4], Airplane.Size.MEDIUM, Airplane.Color.YELLOW);
  //.del airplanes[5] = new Airplane(physics, locations[5], Airplane.Size.SMALL, Airplane.Color.MAGENTA);
  //.del airplanes[6] = new Airplane(physics, locations[6], Airplane.Size.BIG, Airplane.Color.BLACK);
  //.del airplanes[7] = new Airplane(physics, locations[7], Airplane.Size.MEDIUM, Airplane.Color.WHITE);
  //.del airplanes[8] = new Airplane(physics, locations[8], Airplane.Size.SMALL, Airplane.Color.WHITE);

  detector = new CollisionDetector (physics, this);

  helper = new Helper(this, physics);
  controller = new Controller(this, physics, locations, airplanes);
  // float dens = physics.getDensity();
  // physics.setDensity(0);
  // controller.start();
  // physics.setDensity(dens);
}

void draw()
{
  if(!gameStarted) {
    tint(128, 128);
    image(airport, width/2, height/2, width, height);
    noTint();
    stroke(0xff333333);
    strokeWeight(1.0f);
    fill(0, 150);
    rect(width*0.25f, height*0.25f, width*0.5f, height*0.5f, 10);
    fill(66, 255, 66);
    textSize(40);
    text("CLICK TO START", width/2-150, height/2+10);
    return;
  }

  // background
  if(Helper.night == 0)
    image(airport, width/2, height/2, width, height);
  else if(Helper.night == 255)
    image(airportNight, width/2, height/2, width, height);
  else {
    image(airport, width/2, height/2, width, height);
    tint(255, Helper.night);
    image(airportNight, width/2, height/2, width, height);
    noTint();

    if(0 < Helper.night && Helper.night < 255)
      Helper.night += Helper.nightStep;
    else
      Helper.nightStep = 0;
  }

  // text meesages and help
  helper.draw();

  // throttle areas
  if(Helper.night < 128) {
    fill(255, 99, 99, 128);
    noStroke();
    // quad(99,652, 132,639, 157,696, 129,713);
    // quad(498,727, 540,742, 532,774, 486,758);
    ellipse(throttleArea[0].x, throttleArea[0].y, throttleArea[0].angle, throttleArea[0].angle);
    ellipse(throttleArea[1].x, throttleArea[1].y, throttleArea[1].angle, throttleArea[1].angle);
  }

  // exit areas
  /**
  fill(99, 99, 255, 128);
  noStroke();
  ellipse(flightArea[0].x, flightArea[0].y, flightArea[0].angle, flightArea[0].angle*1.5);
  ellipse(flightArea[1].x, flightArea[1].y, flightArea[1].angle*1.5, flightArea[1].angle);
  /**/

  // game actions ...
  controller.act();
}

void airportRenderer(World world)
{
  for(int i = 0; i < airplanes.length; i++) {
    airplanes[i].draw(throttleArea);
  }
}

void mouseDragged()
{
  /**-/ // for the tweaking phase only:
  if(P == -1) return;
  airplanes[P].fuselage.setPosition(physics.screenToWorld(new Vec2(mouseX, mouseY)));
  airplanes[P].fuselage.setLinearVelocity(physics.screenToWorld(new Vec2(mouseX, mouseY)));
  /**/
}

void mouseMoved()
{
}

void mouseClicked()
{
  if(!gameStarted) {
    controller.start();
    gameStarted = true;
  }
  else {
    // println("x = " + mouseX + ", y = " + mouseY);
    for(int i = 0; i < airplanes.length; i++) {
      if(airplanes[i].fuselage == null) continue;
      Vec2 pos = physics.worldToScreen(airplanes[i].fuselage.getWorldCenter());
      float d = dist(mouseX, mouseY, pos.x, pos.y);
      if(d < airplanes[i].size.get()/2) {
        if(P != -1) airplanes[P].selected = false;
        P = i;
        airplanes[P].selected = true;
        /**-/ // debug only:
        println("airplane " + P + ", pos = (" + pos.x + ", "  + pos.y + "), " +
                "angle = " + airplanes[P].fuselage.getAngle());
        /**/
        return;
      }
    }
  }
}

void mouseWheel(MouseEvent event) {
  if(P == -1) return;

  if(keyControl) { // change velocity
    airplanes[P].incVelocity( -(event.getCount() / 4.0f));
  }
  else { // change angle
    float factor = (keyShift ? 3.0f : 0.5f);
    float inc = radians(event.getCount()*factor);
    airplanes[P].angle(inc);
  }
}

void keyPressed(KeyEvent event) {
  // println("pressed keyCode = " + keyCode );
  switch(keyCode) {
  case 16: // shift
    keyShift = true;
    break;
  case 17: // control
    keyControl = true;
    break;
  case 32: // space
    if(P != -1) {
      airplanes[P].throttle();
    }
    break;
  }

  switch(key) {
  case 'd':
  case 'D':
    if(Helper.night == 0)
      Helper.nightStep = 5;
    else if(Helper.night == 255)
      Helper.nightStep = -5;
    if(Helper.nightStep != 0)
      Helper.night += Helper.nightStep;
    println("Helper.night = " + Helper.night);
    break;
  case 's':
  case 'S':
    if(P != -1) {
      airplanes[P].setVelocity(0);
    }
    break;
  case 'h':
  case 'H':
    helper.help = ! helper.help;
    break;
  case '1':
  case '2':
  case '3':
  case '4':
  case '5':
  case '6':
    if(P != -1) {
      float v = map((int)(key-'1'), 0, 5, Airplane.minVelocity+0.25f, Airplane.maxVelocity);
      airplanes[P].setVelocity(v);
    }
    break;
  }
}

void keyReleased(KeyEvent event) {
  switch(keyCode) {
  case 16: // shift
    keyShift = false;
    break;
  case 17: // control
    keyControl = false;
    break;
  }
}

void collision(Body b1, Body b2, float impulse)
{
  int idx1 = -1;
  int idx2 = -1;
  Vec2 p1 = physics.worldToScreen(b1.getWorldCenter());
  Vec2 p2 = physics.worldToScreen(b2.getWorldCenter());

  // airplanes have user data = airplanes index
  if(b1.getUserData() != null) idx1 = (Integer) b1.getUserData();
  if(b2.getUserData() != null) idx2 = (Integer) b2.getUserData();

  /**/ // Collision info
  println("COLLISION " +
          "b1[" + idx1 + "]@(" + p1.x + "," + p1.y + ") * " +
          "b2[" + idx2 + "]@(" + p2.x + "," + p2.y + ") = " + impulse);
  /**/

  // collision between two airplanes
  if(idx1 >= 0 && idx2 >= 0 ) {
    airplanes[idx1].explode(p1);
    airplanes[idx2].explode(p2);
    if(P == idx1 || P == idx2) P = -1;
    return;
  }

  // agains right wall  (walls are always in  Body b1)
  if(abs(p1.x - width) < 0.01f && abs(p1.y - height/2) < 0.01f) {
    if(controller.checkFlight(airplanes[idx2], p2, flightArea[0], impulse)) {
      if(P == idx2) P = -1;
      return;
    }
  }

  //against top wall
  if(abs(p1.x - width/2) < 0.01f && abs(p1.y) < 0.01f) {
    if(controller.checkFlight(airplanes[idx2], p2, flightArea[1], impulse)) {
      if(P == idx2) P = -1;
      return;
    }
  }

  // airplanes explode in any other case
  if(idx1 >= 0) {
    println("BOOOM #1!!");
    airplanes[idx1].explode(p1);
    if(P == idx1) P = -1;
  }

  if(idx2 >= 0) {
    println("BOOOM #2!!");
    airplanes[idx2].explode(p2);
    if(P == idx2) P = -1;
  }
}
