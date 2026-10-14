package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.FileReader;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.Animation;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.KeyFrame;
import org.dopelegend.multiItemDisplayEngine.bone.Bone;
import org.dopelegend.multiItemDisplayEngine.movement.TeleportSmooth;
import org.dopelegend.multiItemDisplayEngine.rotation.RotateSmooth;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDGAnimationHandler {
    private final ItemDisplayGroup group;

    public enum AnimationState{RUNNING, HOLDING, PAUSED, FREE}

    private AnimationState animationState;

    /**
     * All the animations this ItemDisplayGroup has as a map where the key is a string name of the animation and the value is an Animation class.
     */
    private Map<String, Animation> animations = new HashMap<>();

    public IDGAnimationHandler(ItemDisplayGroup group) {
        this.group = group;
        JsonObject rootJson = group.getRootJson();
        if (rootJson == null) {
            MultiItemDisplayEngine.plugin.getLogger().severe("Failed to make animations (used empty animation list instead) for ItemDisplayGroup using model: " + group.getModelName());
            return;
        }

        JsonArray animationsArray =  rootJson.getAsJsonArray("animations");
        for (JsonElement element : animationsArray) {
            if (element instanceof JsonObject animation) {
                animations.put(animation.get("name").getAsString(), new Animation(animation, group.getRootBone()));
            }
        }
    }

    /**
     *
     * Makes this itemDisplayGroup play an animation with the given name.
     *
     * @param animationName The name of the animation, this is the same as it's called in BlockBench.
     * @return False if the animation couldn't be found, or there's already an animation running, otherwise true.
     */
    public boolean playAnimation(String animationName){
        if (!this.animations.containsKey(animationName)) return false;
        if (animationState == AnimationState.RUNNING) return false;

        if(animationState == AnimationState.PAUSED || animationState == AnimationState.HOLDING){
            this.group.resetBonesPosAndRot();
        }

        animationState = AnimationState.RUNNING;

        Animation animation = this.animations.get(animationName);
        Map<Bone, List<KeyFrame>> keyframeSet = animation.getKeyFrames();

        Animation.LoopMode mode = animation.getLoopMode();
        Bone[] boneArray = keyframeSet.keySet().toArray(new Bone[0]);

        for (int i = 0; i < boneArray.length; i++) {
            List<KeyFrame> keyFrames = keyframeSet.get(boneArray[i]);

            for (int j = 0; j < keyFrames.size(); j++) {
                KeyFrame keyFrame = keyFrames.get(j);
                int finalJ = j;
                int finalI = i;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        KeyFrame validKeyframe = null;
                        switch (keyFrame.getType()) {
                            case "rotation":
                                for (int k = finalJ + 1; k < keyFrames.size(); k++) {
                                    if (validKeyframe != null) break;
                                    if (keyFrames.get(k).getType().equals("rotation")) {
                                        validKeyframe = keyFrames.get(k);
                                    }
                                }
                                if (validKeyframe != null) {
                                    int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                    RotateSmooth.SetBoneRotationWithChildrenSmooth(boneArray[finalI], validKeyframe.getXyz(), smoothDuration);
                                }
                                break;
                            case "position":
                                for (int k = finalJ + 1; k < keyFrames.size(); k++) {
                                    if (validKeyframe != null) break;
                                    if (keyFrames.get(k).getType().equals("position")) {
                                        validKeyframe = keyFrames.get(k);
                                    }
                                }

                                if (validKeyframe != null) {
                                    Triple relPos = validKeyframe.getXyz().clone();
                                    relPos.remove(keyFrame.getXyz());
                                    relPos.divide(16);

                                    int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                    TeleportSmooth.TeleportBoneRelativeWithChildrenSmooth(boneArray[finalI], relPos, smoothDuration);
                                }
                                break;
                            case "scale":
                                //TODO SCALE
                                break;
                        }
                    }
                }.runTaskLater(MultiItemDisplayEngine.plugin, Math.round(keyFrame.getTimeStamp() * 20));
            }
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (mode == Animation.LoopMode.LOOP) {
                    animationState = AnimationState.FREE;
                    //resetBonesPosAndRot();
                    playAnimation(animationName);
                } else if (mode == Animation.LoopMode.ONCE) {
                    animationState = AnimationState.FREE;
                    //resetBonesPosAndRot();
                } else if (mode == Animation.LoopMode.HOLD) {
                    animationState = AnimationState.HOLDING;
                }
            }
        }.runTaskLater(MultiItemDisplayEngine.plugin, Math.round(animation.getLength() * 20) + 1);

        return true;
    }

    public AnimationState getAnimationState() {return this.animationState;}

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }
}
