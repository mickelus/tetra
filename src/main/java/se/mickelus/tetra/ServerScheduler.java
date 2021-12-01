package se.mickelus.tetra;

import com.google.common.collect.Queues;
import net.minecraft.server.TickTask;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;
import java.util.Queue;

public class ServerScheduler {
    private static final Queue<Task> queue = Queues.newConcurrentLinkedQueue();
    private static int counter;

    public static void schedule(int delay, Runnable task) {
        queue.add(new Task(counter + delay, task));
    }

    public static void schedule(String id, int delay, Runnable task) {
        queue.removeIf(t -> id.equals(t.id));
        queue.add(new Task(id, counter + delay, task));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (Iterator<Task> it = queue.iterator(); it.hasNext();) {
            Task task = it.next();
            if (task.getTick() < counter) {
                task.run();
                it.remove();
            }
        }

        counter++;
    }

    static class Task extends TickTask {
        private String id;

        public Task(int timestamp, Runnable task) {
            super(timestamp, task);
        }

        public Task(String id, int timestamp, Runnable task) {
            this(timestamp, task);

            this.id = id;
        }
    }
}
