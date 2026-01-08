package org.dopelegend.multiItemDisplayEngine.utils.classes;

import org.joml.Vector3f;

public class Triple {
    public double x;
    public double y;
    public double z;

    public Triple(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Triple remove(Triple triple) {
        this.x -= triple.x;
        this.y -= triple.y;
        this.z -= triple.z;
        return this;
    }

    /**
     *
     * Converts to a Vector3f which is often used but offers less accuracy (float vs double), therefore this conversion is inherently imprecise
     *
     * @return The Vector3F that the Triple was converted to.
     */
    public Vector3f toVector3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }


    
    /**
     *
     * Divides each double in this triple by a double d
     *
     * @param d The number to divide by
     * @return The divided Triple
     */
    public Triple divide(double d) {
        this.x /= d;
        this.y /= d;
        this.z /= d;
        return this;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
