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

import processing.core.PApplet;

class Controller {

    PApplet app;
    Physics physics;
    Location [] locations;
    Airplane [] airplanes;

    private int [] seconds = { 180, 120, 60 }; // the number of second of each phase
    private int [] rate = { 20, 10, 5 }; // == x : new airplanes / x seconds
    private int phase, tics, secs;
    private Boolean actionPending;

    Controller(PApplet app, Physics physics, Location [] locations, Airplane [] airplanes) {
        this.app = app;
        this.physics = physics;
        this.locations = locations;
        this.airplanes = airplanes;

        phase = 0;
        tics = 0;
        secs = 0;
        actionPending = false;
    }

    public void start() {
        airplanes[0].startAt(locations[5]);
    }

    public void act() {
        if(Airplane.counter == 0 || actionPending) {
            insertAirplane();
            return;
        }

        if(++tics % 60 != 0) return; // act each second

        tics = 0;
        if(phase < seconds.length-1 && ++secs == seconds[phase]) {
            phase++;
            app.println("phase = " + phase + ", new airplane every " + rate[phase] + " seconds");
        }

        if(secs % rate[phase] == 0)
            actionPending = ! insertAirplane();
    }

    public Boolean checkFlight(Airplane airplane, Vec2 pos, Location flightArea, float impulse) {
        if(app.dist(flightArea.x, flightArea.y, pos.x, pos.y) < Airplane.Size.BIG.get()*0.8) {
            // nice discovery: the angle must be checked against the last one before the collision!!!
            if(Math.abs(airplane.lastAngle - flightArea.angle) < 0.1f) {
                if(impulse > airplane.size.get()*1.3f) {
                    Helper.message("Good flight!");
                    airplane.flight();
                    return true;
                }
                else Helper.message("# TOO SLOW");
            }
            else Helper.message("# BAD ANGLE");
        }
        else Helper.message("OUT OF BOUNDS");
        return false;
    }

    private Boolean insertAirplane() {
        int loc = freeLocation();
        int p = freeAirplane();
        if(loc != -1 && p != -1) {
            airplanes[p].startAt(locations[loc]);
            return true;
        }
        return false;
    }

    private int freeLocation() {
        Boolean [] visited = new Boolean[locations.length];
        for(int i = 0; i < visited.length; i++)
            visited[i] = false;

        int count = 0;
        while(count < locations.length) {
            int i = (int)app.random(locations.length);
            while(visited[i])
                i = (int)app.random(locations.length);

            int near = 0;
            for(int p = 0; p < airplanes.length; p++) {
                if(airplanes[p].fuselage != null) {
                    Vec2 pos = physics.worldToScreen(airplanes[p].fuselage.getWorldCenter());
                    if(app.dist(locations[i].x, locations[i].y, pos.x, pos.y) < 250) near++;
                }
            }
            if(near == 0) return i;

            visited[i] = true;
            count++;
        }

        return -1;
    }

    private int freeAirplane() {
        for(int i = 0; i < airplanes.length; i++)
            if(airplanes[i].fuselage == null && ! airplanes[i].toExplode)
                return i;

        return -1;
    }
}
