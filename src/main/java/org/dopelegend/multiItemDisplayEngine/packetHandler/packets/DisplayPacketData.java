package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public class DisplayPacketData implements PacketData{
    int interpolationDelay = Integer.MIN_VALUE;
    int positionInterpolationDuration = Integer.MIN_VALUE;
    int rotationInterpolationDuration = Integer.MIN_VALUE;

    Vector3fc translation = null;
    Vector3fc scale = null;

    Quaternionfc rotationLeft = null;
    Quaternionfc rotationRight = null;

    Byte billboardConstraint = Byte.MIN_VALUE;
    int brightnessOverride = Integer.MIN_VALUE;

    float viewRange = Float.MIN_VALUE;
    float shadowRadius = Float.MIN_VALUE;
    float shadowStrength = Float.MIN_VALUE;

    float width = Float.MIN_VALUE;
    float height = Float.MIN_VALUE;

    int glowColorOverride = Integer.MIN_VALUE;

    public int getInterpolationDelay() {
        return interpolationDelay;
    }

    public void setInterpolationDelay(int interpolationDelay) {
        this.interpolationDelay = interpolationDelay;
    }

    public int getPositionInterpolationDuration() {
        return positionInterpolationDuration;
    }

    public void setPositionInterpolationDuration(int positionInterpolationDuration) {
        this.positionInterpolationDuration = positionInterpolationDuration;
    }

    public int getRotationInterpolationDuration() {
        return rotationInterpolationDuration;
    }

    public void setRotationInterpolationDuration(int rotationInterpolationDuration) {
        this.rotationInterpolationDuration = rotationInterpolationDuration;
    }

    public Vector3fc getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3fc translation) {
        this.translation = translation;
    }

    public Vector3fc getScale() {
        return scale;
    }

    public void setScale(Vector3fc scale) {
        this.scale = scale;
    }

    public Quaternionfc getRotationLeft() {
        return rotationLeft;
    }

    public void setRotationLeft(Quaternionfc rotationLeft) {
        this.rotationLeft = rotationLeft;
    }

    public Quaternionfc getRotationRight() {
        return rotationRight;
    }

    public void setRotationRight(Quaternionfc rotationRight) {
        this.rotationRight = rotationRight;
    }

    public Byte getBillboardConstraint() {
        return billboardConstraint;
    }

    public void setBillboardConstraint(Byte billboardConstraint) {
        this.billboardConstraint = billboardConstraint;
    }

    public int getBrightnessOverride() {
        return brightnessOverride;
    }

    public void setBrightnessOverride(int brightnessOverride) {
        this.brightnessOverride = brightnessOverride;
    }

    public float getViewRange() {
        return viewRange;
    }

    public void setViewRange(float viewRange) {
        this.viewRange = viewRange;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public void setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }

    public float getShadowStrength() {
        return shadowStrength;
    }

    public void setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getGlowColorOverride() {
        return glowColorOverride;
    }

    public void setGlowColorOverride(int glowColorOverride) {
        this.glowColorOverride = glowColorOverride;
    }

    @Override
    public List<SynchedEntityData.DataValue<?>> getPacketData(){
        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>();

        if(interpolationDelay != Integer.MIN_VALUE){
            EntityDataSerializer<Integer> serializer = EntityDataSerializers.INT;

            SynchedEntityData.DataValue<Integer> item =
                    new SynchedEntityData.DataValue<>(
                            8,
                            serializer,
                            interpolationDelay);

            data.add(item);
        }
        if(positionInterpolationDuration != Integer.MIN_VALUE){
            EntityDataSerializer<Integer> serializer = EntityDataSerializers.INT;

            SynchedEntityData.DataValue<Integer> item =
                    new SynchedEntityData.DataValue<>(
                            9,
                            serializer,
                            positionInterpolationDuration);

            data.add(item);
        }
        if(rotationInterpolationDuration != Integer.MIN_VALUE){
            EntityDataSerializer<Integer> serializer = EntityDataSerializers.INT;

            SynchedEntityData.DataValue<Integer> item =
                    new SynchedEntityData.DataValue<>(
                            10,
                            serializer,
                            rotationInterpolationDuration);

            data.add(item);
        }
        if(translation != null){
            EntityDataSerializer<Vector3fc> serializer = EntityDataSerializers.VECTOR3;

            SynchedEntityData.DataValue<Vector3fc> item =
                    new SynchedEntityData.DataValue<>(
                            11,
                            serializer,
                            translation);

            data.add(item);
        }
        if(scale != null){
            EntityDataSerializer<Vector3fc> serializer = EntityDataSerializers.VECTOR3;

            SynchedEntityData.DataValue<Vector3fc> item =
                    new SynchedEntityData.DataValue<>(
                            12,
                            serializer,
                            scale);

            data.add(item);
        }
        if(rotationLeft != null){
            EntityDataSerializer<Quaternionfc> serializer = EntityDataSerializers.QUATERNION;

            SynchedEntityData.DataValue<Quaternionfc> item =
                    new SynchedEntityData.DataValue<>(
                            13,
                            serializer,
                            rotationLeft);

            data.add(item);
        }
        if(rotationRight != null){
            EntityDataSerializer<Quaternionfc> serializer = EntityDataSerializers.QUATERNION;

            SynchedEntityData.DataValue<Quaternionfc> item =
                    new SynchedEntityData.DataValue<>(
                            14,
                            serializer,
                            rotationRight);

            data.add(item);
        }
        if(billboardConstraint != Byte.MIN_VALUE){
            EntityDataSerializer<Byte> serializer = EntityDataSerializers.BYTE;

            SynchedEntityData.DataValue<Byte> item =
                    new SynchedEntityData.DataValue<>(
                            15,
                            serializer,
                            billboardConstraint);

            data.add(item);
        }
        if(brightnessOverride != Integer.MIN_VALUE){
            EntityDataSerializer<Integer> serializer = EntityDataSerializers.INT;

            SynchedEntityData.DataValue<Integer> item =
                    new SynchedEntityData.DataValue<>(
                            16,
                            serializer,
                            brightnessOverride);

            data.add(item);
        }
        if(viewRange != Float.MIN_VALUE){
            EntityDataSerializer<Float> serializer = EntityDataSerializers.FLOAT;

            SynchedEntityData.DataValue<Float> item =
                    new SynchedEntityData.DataValue<>(
                            17,
                            serializer,
                            viewRange);

            data.add(item);
        }
        if(shadowRadius != Float.MIN_VALUE){
            EntityDataSerializer<Float> serializer = EntityDataSerializers.FLOAT;

            SynchedEntityData.DataValue<Float> item =
                    new SynchedEntityData.DataValue<>(
                            18,
                            serializer,
                            shadowRadius);

            data.add(item);
        }
        if(shadowStrength != Float.MIN_VALUE){
            EntityDataSerializer<Float> serializer = EntityDataSerializers.FLOAT;

            SynchedEntityData.DataValue<Float> item =
                    new SynchedEntityData.DataValue<>(
                            19,
                            serializer,
                            shadowStrength);

            data.add(item);
        }
        if(width != Float.MIN_VALUE){
            EntityDataSerializer<Float> serializer = EntityDataSerializers.FLOAT;

            SynchedEntityData.DataValue<Float> item =
                    new SynchedEntityData.DataValue<>(
                            20,
                            serializer,
                            width);

            data.add(item);
        }
        if(height != Float.MIN_VALUE){
            EntityDataSerializer<Float> serializer = EntityDataSerializers.FLOAT;

            SynchedEntityData.DataValue<Float> item =
                    new SynchedEntityData.DataValue<>(
                            21,
                            serializer,
                            height);

            data.add(item);
        }
        if(glowColorOverride != Integer.MIN_VALUE){
            EntityDataSerializer<Integer> serializer = EntityDataSerializers.INT;

            SynchedEntityData.DataValue<Integer> item =
                    new SynchedEntityData.DataValue<>(
                            22,
                            serializer,
                            glowColorOverride);

            data.add(item);
        }

        return data;
    }
}
