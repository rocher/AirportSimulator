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

import processing.core.PImage;
import processing.core.PApplet;

class Airplane
{
    public enum Size {
        BIG(85),
        MEDIUM_BIG(80),
        MEDIUM(75),
        MEDIUM_SMALL(70),
        SMALL(60);

        Size(float pixels) { this.pixels = pixels; }
        public float get() { return pixels; }
        private final float pixels;
    };

    public enum Color {
        WHITE(0xffffffff),
        RED(0xffff3333),
        GREEN(0xff99ff99),
        BLUE(0xff7777ff),
        CYAN(0xff33ffff),
        MAGENTA(0xffff33ff),
        YELLOW(0xffffff33),
        BLACK(0xff333333);

        Color(int rgb) { this.rgb = rgb; }
        public int get() { return rgb; }
        private final int rgb;
    };

    Airplane(PApplet a, Physics p, int i) {
        app = a;
        physics = p;
        idx = i;
        size = Airplane.randomSize();
        color = Airplane.randomColor();
        lastPos = new Vec2();
        fuselage = null;
        reset();
    }

    public void startAt(Vec3 loc) {
        if(!jetSnd.isPlaying()) {
            jetSnd.play();
            jetSnd.loop();
        }
        if(fuselage != null) remove();
        fuselage = physics.createCircle(loc.x, loc.y, size.get()/2);
        fuselage.setUserData(idx);
        size = Airplane.randomSize();
        color = Airplane.randomColor();
        Airplane.counter++;
        fuselage.setAngle(loc.z);
        reset();
        incVelocity(0.5f);

        trafficSnd.cue(0);
        trafficSnd.play();
    }

    private void reset() {
        velocity = 0.0f;
        selected = false;
        toFligh = false;
        toExplode = false;
        canSpeed = false;
        throttling = false;
        explosionCounter = 0;
    }

    private void remove() {
        if(fuselage != null) {
            // lastPos = physics.worldToScreen(fuselage.getWorldCenter());
            physics.removeBody(fuselage);
            Airplane.counter--;
            fuselage = null;
        }
    }

    public void draw(Vec3 [] throttleArea) {
        if(toExplode) {
            if(explosionCounter == 0) {
                if(throttling)
                    throttleSnd.pause();
                explosionSnd.cue(0);
                explosionSnd.play();
                remove();
            }
            app.image(Airplane.explosions[explosionCounter], lastPos.x, lastPos.y, 142, 142);
            if(++explosionCounter == 16) {
                reset();
            }
            return;
        }

        if(toFligh) {
            remove();
            reset();
            return;
        }

        if(fuselage == null) return; // no plane!!

        if(velocity == 0.0f) Helper.score--;

        Vec2 center = fuselage.getWorldCenter();
        Vec2 pos = physics.worldToScreen(center);
        lastAngle = physics.getAngle(fuselage);

        for(int i = 0; i < throttleArea.length; i++) {
            float d = app.dist(pos.x, pos.y, throttleArea[i].x, throttleArea[i].y);
            if(d < throttleArea[i].z/2)
                canSpeed = true;
        }

        app.pushMatrix();
        {
            float s = size.get();
            app.translate(pos.x, pos.y);
            app.rotate(-lastAngle);

            if(selected) {
                if(canSpeed) {
                    app.stroke(0xffff0000);
                    app.strokeWeight(1.85f);
                    app.fill(128, 66, 66, 50);
                }
                else {
                    if(Helper.night > 128) {
                        app.stroke(0xffcccccc);
                        app.fill(0x00ff00, 250);
                    }
                    else {
                        app.stroke(0xff000000);
                        app.fill(0, 50);
                    }
                    app.strokeWeight(0.85f);
                }
                app.ellipse(0, 0, s/1.5f, s/1.5f);
                app.strokeWeight(1.5f);

                if(Helper.night > 128)
                    app.stroke(0xffcccccc);
                else
                    app.stroke(0xff000000);
                app.line(0, 0, s/2+10, 0);
                app.line(0, 0, -s/2-5, 0);
                app.noFill();
            }

            if(Helper.night > 128) {
                app.noTint();
                app.image(Airplane.night, 0, 0, s, s);
                if(++lightTopCounter >= 63) {
                    app.image(Airplane.lightTop, 0, 0, s, s);
                    if(lightTopCounter == 66) lightTopCounter = 0;
                }
                if(++lightTailCounter >= 55) {
                    app.image(Airplane.lightTail, 0, 0, s, s);
                    if(lightTailCounter == 63) lightTailCounter = 0;
                }
            }
            else {
                app.tint(color.get());
                app.image(Airplane.day, 0, 0, s, s);
                app.noTint();
            }
        }
        app.popMatrix();

        if(throttling)
            throttle();
    }

    public void angle(float inc) {

        if(velocity == 0.0) {
            fuselage.setAngularVelocity(0);
//      return;
        }
        float pi2 = 6.283185307179586f;
        float angle = fuselage.getAngle() + inc;
        if(angle < 0) angle += pi2;
        if(pi2 < angle) angle -= pi2;
        Vec2 impulse = new Vec2(0, 0);

        fuselage.setAngularVelocity(0);
        fuselage.setLinearVelocity(impulse);
        fuselage.setAngle(angle);
        impulse.set(velocity * (float)Math.cos(angle), velocity * (float)Math.sin(angle));
        fuselage.applyImpulse(impulse, fuselage.getWorldCenter());
    }

    public void incVelocity(float inc) {
        if(throttling) return;
        float v = velocity + inc;
        setVelocity(v);
    }

    public void setVelocity(float v) {
        if(throttling) return;
        velocity = v;
        if(velocity < Airplane.minVelocity) velocity = Airplane.minVelocity;
        if(Airplane.maxVelocity < velocity) velocity = Airplane.maxVelocity;

        float angle = fuselage.getAngle();
        Vec2 impulse = new Vec2(0, 0);
 
        if(velocity == Airplane.minVelocity)
            fuselage.setAngularVelocity(0);

        fuselage.setLinearVelocity(impulse);
        impulse.set(velocity*(float)Math.cos(angle), velocity*(float)Math.sin(angle));
        fuselage.applyImpulse(impulse, fuselage.getWorldCenter());
    }

    public void flight() {
        if(toFligh) return;

        toFligh = true;
        fuselage.putToSleep();
        Helper.flights++;
        Helper.score += 10*size.get();
    }

    public void explode(Vec2 pos) {
        if(toExplode) return;

        lastPos.set(pos);
        toExplode = true;
        // setVelocity(0);
        fuselage.putToSleep();
        Helper.explosions++;
        Helper.score -= 12*size.get();
    }

    public void throttle() {
        if(! canSpeed || velocity == 0.0f) return;

        if(!throttling) {
            throttleSnd.cue(0);
            throttleSnd.play();
        }
        throttling = true;
        float angle = fuselage.getAngle();
        Vec2 impulse = new Vec2(0, 0);

        velocity *= 1.063f;

        fuselage.setLinearVelocity(impulse);
        impulse.set(velocity*(float)Math.cos(angle), velocity*(float)Math.sin(angle));
        fuselage.applyImpulse(impulse, fuselage.getWorldCenter());
    }

    private PApplet app;
    private Physics physics;

    public int idx;
    public Size size;
    public Color color;
    public Body fuselage;
    public float velocity;
    public Boolean selected;
    public Boolean toFligh;
    public Boolean toExplode;
    public Boolean canSpeed;
    public Boolean throttling;
    public int explosionCounter;
    public Vec2 lastPos;
    public float lastAngle;
    private int lightTopCounter = 0;
    private int lightTailCounter = 0;

    // Static stuff

    public static int counter;
    private static PImage day;
    private static PImage night;
    private static PImage lightTop;
    private static PImage lightTail;
    private static PImage [] explosions;
    private static Minim minim;
    private static AudioPlayer trafficSnd;
    private static AudioPlayer explosionSnd;
    private static AudioPlayer jetSnd;
    private static AudioPlayer throttleSnd;
    public static float minVelocity = 0.0f;
    public static float maxVelocity = 4.0f;

    public static void init(PApplet app) {
        // images
        Airplane.counter = 0;
        Airplane.day = app.loadImage("airplane.png");
        Airplane.night = app.loadImage("airplane-night.png");
        Airplane.lightTop = app.loadImage("light-top.png");
        Airplane.lightTail = app.loadImage("light-tail.png");

        Airplane.explosions = new PImage[16];
        for(int i = 0; i < Airplane.explosions.length; i++) {
            String filename = "explosion-" + i + ".png";
            Airplane.explosions[i] = app.loadImage(filename);
        }

        // sound
        minim = new Minim(app);
        trafficSnd = minim.loadFile("traffic.wav");
        explosionSnd = minim.loadFile("explosion.wav");
        jetSnd = minim.loadFile("jet.wav");
        throttleSnd = minim.loadFile("throttle.wav");
    }

    public static Airplane.Size randomSize() {
        Airplane.Size size = Airplane.Size.SMALL;
        switch((int)Math.floor(Math.random()*5)) {
        case 0: size = Airplane.Size.SMALL; break; 
        case 1: size = Airplane.Size.MEDIUM_SMALL; break;
        case 2: size = Airplane.Size.MEDIUM; break;
        case 3: size = Airplane.Size.MEDIUM_BIG; break;
        case 4: size = Airplane.Size.BIG; break;
        }
        return size;
    }

    public static Airplane.Color randomColor() {
        Airplane.Color color = Airplane.Color.WHITE;
        switch((int)Math.floor(Math.random()*8)) {
        case 0: color = Airplane.Color.WHITE; break;
        case 1: color = Airplane.Color.WHITE; break;
        case 2: color = Airplane.Color.WHITE; break;
        case 3: color = Airplane.Color.BLACK; break;
        case 4: color = Airplane.Color.WHITE; break;
        case 5: color = Airplane.Color.YELLOW; break;
        case 6: color = Airplane.Color.BLACK; break;
        case 7: color = Airplane.Color.BLACK; break;
        }
        return color;
    }
}
