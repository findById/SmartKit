package org.cn.plugin.rpc.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenning on 16-11-30.
 */

public class ThreadUtil {

    private static final int CORE_POOL_SIZE = 1;
    private static int MAX_POOL_SIZE = 10;

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        AtomicInteger idFactory = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = threadFactory.newThread(runnable);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }
            thread.setName("TASK-THREAD-" + idFactory.getAndIncrement());
            return thread;
        }
    };

    private static ExecutorService instance;

    public static ExecutorService getInstance() {
        synchronized (ThreadUtil.class) {
            if (instance == null) {
                instance = newExecutorService();
            }
            return instance;
        }
    }

    public static ExecutorService newExecutorService() {
        // LinkedTransferQueue
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), DEFAULT_THREAD_FACTORY);
    }

    public static void shutdown() {
        if (instance != null && !instance.isShutdown()) {
            instance.shutdown();
        }
    }
}
