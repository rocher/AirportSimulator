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

class Vec3 {

    public float x;
    public float y;
    public float z;

    Vec3() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }

    Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

