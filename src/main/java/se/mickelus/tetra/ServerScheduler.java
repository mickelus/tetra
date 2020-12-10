package se.mickelus.tetra;

import com.google.common.collect.Queues;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;
import java.util.Queue;

public class ServerScheduler {
    private static final Queue<TickDelayedTask> queue = Queues.newConcurrentLinkedQueue();
    private static int counter;

    public static void schedule(int delay, Runnable task) {
        queue.add(new TickDelayedTask(counter + delay, task));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (Iterator<TickDelayedTask> it = queue.iterator(); it.hasNext();) {
            TickDelayedTask task = it.next();
            if (task.getScheduledTime() < counter) {
                task.run();
                it.remove();
            }
        }

        counter++;
    }
}
