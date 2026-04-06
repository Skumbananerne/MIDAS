package org.dopelegend.multiItemDisplayEngine.utils.classes;

import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Triple {
    public double x;
    public double y;
    public double z;

    public Triple(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     *
     * Adds a triple to this triple.
     *
     * @param triple The triple to add.
     * @return A new copy with the triple added (the triple this method is called on will also be updated).
     */
    public Triple add(Triple triple){
        this.x += triple.x;
        this.y += triple.y;
        this.z += triple.z;
        return this.clone();
    }
    /**
     *
     * Subtracts a triple from this triple.
     *
     * @param triple The triple to subtract.
     * @return A new copy with the triple subtracted (the triple this method is called on will also be updated).
     */
    public Triple remove(Triple triple) {
        this.x -= triple.x;
        this.y -= triple.y;
        this.z -= triple.z;
        return this.clone();
    }

    @Override
    public String toString() {
        return "Triple{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    /**
     * 
     * Makes a Vec3 (Minecraft 3d vector).
     * 
     * @return The new Vec3
     */
    public Vec3 toVec3(){
        return new Vec3(x, y, z);
    }

    /**
     *
     * Converts all values in this to Radians assuming they're degrees (multiplies by pi/180).
     *
     * @return A new triple with the converted values.
     */
    public Triple toRadians() {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);
        return new Triple(x, y, z);
    }

    /**
     *
     * Uses the modulo operator on each double in this triple using a double m
     *
     * @param m The number modulo by.
     * @return The remainder as a new Triple. (the result)
     */
    public Triple modulo(double m){
        this.x = (x % m);
        this.y = (y % m);
        this.z = (z % m);
        return new Triple(x, y, z);
    }

    /**
     *
     * Makes a new triple of the x, y and z coordinates in a location
     *
     * @param location The location to get the coordinates from.
     */
    public Triple(Location location) {
         this.x = location.getX();
         this.y = location.getY();
         this.z = location.getZ();
    }

    /**
     *
     * Makes a new triple of the x, y and z values in a vector3fc
     *
     * @param vector3fc The vector3fc to get the coordinates from. If this is null an empty Triple will be returned.
     */
    public Triple(Vector3fc vector3fc) {
        if (vector3fc==null){
            this.x = 0;
            this.y = 0;
            this.z = 0;
            return;
        }
        this.x = vector3fc.x();
        this.y = vector3fc.y();
        this.z = vector3fc.z();
    }

    /**
     *
     * Gets the squared (to the power of two) distance between this Triple and another treated as points in 3d space.
     *
     * @param triple The triple to get the distance to.
     * @return The distance squared.
     */
    public double getDistanceSquared(Triple triple){
        return (triple.x-this.x)*(triple.x-this.x)
                +(triple.y-this.y)*(triple.y-this.y)
                +(triple.z-this.z)*(triple.z-this.z);
    }

    /**
     *
     * Returns true if and only if this Triple has 3 values equal to 0.
     *
     * @return whether this triple is empty.
     */
    public boolean isEmpty(){
        return (x == 0.0 && y == 0.0 && z == 0.0);
    }

    /**
     *
     * Makes a new Triple equal to the translation (distance on three axes / displacement) from a to b, or more formally the translation which 'from' needs to be translated to be at the exact position of 'to'.
     * The output is calculated following the following formula: A + T = B, T = B - A. Where A is 'from', B is 'to' and T is the returned Triple.
     *
     * @param from The Triple you're coming from
     * @param to The Triple you're going to.
     * @return A new triple with all three values being equal to the translation from a (from) to b (to).
     */
    public static Triple difference(Triple from, Triple to) {
        return new Triple(to.x - from.x, to.y - from.y, to.z - from.z);
    }

    /**
     *
     * Makes a new Triple equal to the translation (distance on three axes / displacement) from a to b, or more formally the translation which 'from' needs to be translated to be at the exact position of 'to'.
     * The output is calculated following the following formula: A + T = B, T = B - A. Where A is 'from', B is 'to' and T is the returned Triple.
     *
     * @param from The Triple you're coming from
     * @param to The Location you're going to.
     * @return A new triple with all three values being equal to the translation from a (from) to b (to).
     */
    public static Triple difference(Triple from, Location to) {
        return new Triple(to.x() - from.x, to.y() - from.y, to.z() - from.z);
    }

    /**
     *
     * Makes a new Triple equal to the translation (distance on three axes / displacement) from a to b, or more formally the translation which 'from' needs to be translated to be at the exact position of 'to'.
     * The output is calculated following the following formula: A + T = B, T = B - A. Where A is 'from', B is 'to' and T is the returned Triple.
     *
     * @param from The Location you're coming from
     * @param to The Triple you're going to.
     * @return A new triple with all three values being equal to the translation from a (from) to b (to).
     */
    public static Triple difference(Location from, Triple to) {
        return new Triple(to.x - from.x(), to.y - from.y(), to.z - from.z());
    }

    /**
     *
     * Makes a new Triple equal to the translation (distance on three axes / displacement) from a to b, or more formally the translation which 'from' needs to be translated to be at the exact position of 'to'.
     * The output is calculated following the following formula: A + T = B, T = B - A. Where A is 'from', B is 'to' and T is the returned Triple.
     *
     * @param from The Location you're coming from
     * @param to The Location you're going to.
     * @return A new triple with all three values being equal to the translation from a (from) to b (to).
     */
    public static Triple difference(Location from,  Location to) {
        return new Triple(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
    }


    /**
     *
     * Makes all values in the triple inverted (negative to positive, positive to negative)
     *
     * @return A new triple also with the inverted values.
     */
    public Triple invert() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return new Triple(x, y, z);
    }

    /**
     *
     * Makes a copy of this Triple.
     *
     * @return The copy
     */
    public Triple clone(){
        return new Triple(x, y, z);
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


    public double squaredSum() {
        return x * x + y * y + z * z;
    }

    public boolean equals(Triple other){
        return x == other.x && y == other.y && z == other.z;
    }

    public void set(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Sets every value in this Triple to zero.
     */
    public void clear(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
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
        return new Triple(x, y, z);
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
