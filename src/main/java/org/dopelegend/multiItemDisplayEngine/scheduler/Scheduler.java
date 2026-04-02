package org.dopelegend.multiItemDisplayEngine.scheduler;

import org.bukkit.Bukkit;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketSender;

import java.util.*;

public class Scheduler {

    static class Task {
        Runnable runnable;
        int period;
        int priority;

        public Task(Runnable runnable, int period, int priority) {
            this.runnable = runnable;
            this.period = period;
            this.priority = priority;
        }
    }

    List<Task> tasks = new ArrayList<>();
    long tick = 0;

    public Scheduler() {
        loadTasks();

        Bukkit.getScheduler().runTaskTimer(MultiItemDisplayEngine.plugin, () -> {

            for (Task task : tasks) {
                if (task.period <= 0) continue;

                if (tick % task.period == 0) {
                    task.runnable.run();
                }
            }


            tick++;

        }, 0, 1);
    }

    public void addTask(int period, int priority, Runnable task){
        tasks.add(new Task(task, period, priority));
        tasks.sort(Comparator.comparingInt(t -> t.priority));
    }

    private void loadTasks(){
        addTask(1, 100, PacketSender::flushPackets);
        addTask(1, 5, () -> {
            ItemDisplayGroup.getAllItemDisplayGroups().forEach(ItemDisplayGroup::queueAllPackets);
        });
    }
}