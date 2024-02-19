package me.rhys.anticheat.base.thread;

import lombok.Getter;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.math.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class ThreadManager {
    private final int threads = Runtime.getRuntime().availableProcessors();
    private final int maxThreads = threads * 5;
    private final List<Thread> userThreads = new ArrayList<>();

    public boolean isThreadsOver() {
        return this.userThreads.size() > (maxThreads - 1);
    }

    public void shutdownThread(User user) {
        user.getThread().count--;

        if (user.getThread().getCount() < 1) {
            user.getThread().getExecutorService().shutdownNow();
            this.userThreads.remove(user.getThread());
        }
    }

    public Thread generate(User user) {
        int size = this.userThreads.size();

        if (size > (maxThreads - 1)) {
            Thread randomThread = this.getUserThreads()
                    .stream()
                    .min(Comparator.comparing(Thread::getCount))
                    .orElse(MathUtil.randomElement(this.getUserThreads()));

            if (randomThread != null) {
                randomThread.count++;
                return randomThread;
            } else {
                return new Thread();
            }
        } else {
            Thread thread = new Thread();
            thread.count++;
            this.userThreads.add(thread);
            return thread;
        }
    }

    public ScheduledExecutorService generateServiceScheduled() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
