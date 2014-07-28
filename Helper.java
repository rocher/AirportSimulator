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

import ddf.minim.*;

import processing.core.PApplet;
import processing.core.PFont;

class Helper {

    Helper(PApplet app, Physics physics) {
        this.app = app;
        this.font = app.loadFont("CourierNewPS-BoldMT-16.vlw");

        Helper.minim = new Minim(app);

        float dens = physics.getDensity();
        physics.setDensity(0);

        // This is specific to each airport layout, in case Airport is a
        // class to implement several game screens in different airports.
        // The idea is to delimit those areas that cannot be traversed by
        // airplanes.
        this.area = physics.createRect(660, 520, app.width-10, app.height-10);
        physics.setDensity(dens);

        message = new String("");
    }

    public void draw() {
        app.stroke(0xff333333);
        app.strokeWeight(1.0f);
        app.fill(0, 150);
        if(help)
            app.rect(app.width-500, app.height-260, 480, 240, 10);
        else
            app.rect(app.width-260, app.height-260, 240, 240, 10);

        float left = app.width-500+20;
        float margin1 = left + 20;
        float margin2 = left + 260;
        float top = app.height-260+28;
        float lineheight = 18;
        float line;

        if(help) {
            // title
            line = top;
            app.fill(66, 255, 66); //  title color
            app.textFont(font, 18);
            app.text("AIRPORT SIMULATOR", left, line);
            app.fill(33, 180, 33);  // text color
            line += lineheight/2;
            app.textFont(font, 14);

            // keys
            line += lineheight*0.9;
            app.text("KEYS", left, line);
            line += lineheight;
            app.text("h: show/hide this help", margin1, line);
            line += lineheight;
            app.text("s: stop,  d: day/night", margin1, line);
            line += lineheight;
            app.text("space: throttle", margin1, line);
            line += lineheight;
            app.text("1-6: velocity", margin1, line);

            // mouse
            line += lineheight * 1.5;
            app.text("MOUSE", left, line);
            line += lineheight;
            app.text("click: select airplane", margin1, line);
            line += lineheight;
            app.text("wheel: angle, slow", margin1, line);
            line += lineheight;
            app.text("shift wheel: angle", margin1, line);
            line += lineheight;
            app.text("ctrl wheel: velocity", margin1, line);
        }

        // score
        app.textFont(font, 18);
        app.fill(200, 200, 200);  // score color
        app.text("SCORE", margin2, top);
        app.textFont(font, 20);
        line = top + lineheight*1.5f;
        app.text(app.nfs(Helper.score, 8), margin2, line);

        app.textFont(font, 16);
        line += lineheight*2.0f;
        app.text("LOST", margin2, line);
        line += lineheight;
        app.text(app.nfs(Helper.explosions, 4), margin2+20, line);
        line += lineheight*1.5f;
        app.text("TAKE OFF", margin2, line);
        line += lineheight;
        app.text(app.nfs(Helper.flights, 4), margin2+20, line);

        app.fill(255, 66, 66);  // message color
        line += lineheight*2;
        app.textFont(font, 20);
        app.text(message, margin2+20, line);

        /**-/ // Limit the time of the message shown
        if(++msg_counter == 120)
            message = "";
        /**/
    }

    public static void message(String msg) {
        msg_counter = 0;
        message = msg;
        app.println("MSG: " + msg);
    }

    public Boolean help = true;
    private Body area;

    public static Minim minim;
    public static int score = 0;
    public static int explosions = 0;
    public static int flights = 0;
    public static int night = 0;
    public static int nightStep = 0;

    private static PApplet app;
    private static PFont font;
    private static String message= "";
    private static int msg_counter = 0;
}
