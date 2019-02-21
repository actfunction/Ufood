package com.rh.food.util.thread;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import com.rh.gw.gdjh.exception.BaseException;

   

/**
 * 简易异步执行器<br>
 * 
 * @author zhangwei
 */
public class SimpleAsyncExecutor <T, R>  implements Executor<T, R> {

	private static final String LOG_FORMAT_PREFIX = "[Batch][Concurrent][Total:{}][{}]";
    private static final String LOG_FORMAT_RETURN = LOG_FORMAT_PREFIX + "[Return][No.{}][{}][Result: {}]";
    private static final String LOG_FORMAT_SUBMIT = LOG_FORMAT_PREFIX + "[Submit][{}]{}";

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAsyncExecutor.class);

    private static final String THREAD_NAME = "ConcurrentBlockingExecutor";

    private static ExecutorService executorService = new ThreadPoolExecutor(50, 1000, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(300), new BasicThreadFactory(THREAD_NAME), new CallerRunsPolicy());

    /**
     * 阻塞式并发执行多个任务，当所有任务都执行完成时此方法才返回， 
     * 
     * @param ts 待处理的目标集合
     * @param callback 回调函数
     * @param results 各线程运行结果汇总
     * @return 批量任务全部执行成功则返回true，否则返回false
     */
    @Override
    public boolean execute(Collection<T> ts, Callback<T, R> callback, List<R> results) {
        if (CollectionUtils.isEmpty(ts)) {
            LOGGER.debug("ts is empty, the executor will not execute it.");
            return true;
        }
         int size = ts.size();
        LOGGER.debug(LOG_FORMAT_PREFIX + "[Begin]", size, callback.getName());
         CompletionService<Result<R>> completionService = new ExecutorCompletionService<Result<R>>(executorService);
         Iterator<T> it = ts.iterator();
        int i = 0;
        while (it.hasNext()) {

            T t = it.next();

            String workerName = "Worker-" + size + "-" + (i + 1) + "";
            Message message = new Message(workerName);
             Worker<T, R> worker = new Worker<T, R>(workerName, callback, t, message);
            LOGGER.debug(LOG_FORMAT_SUBMIT, new Object[] { size, callback.getName(), workerName, t });
            completionService.submit(worker);
            i++;
        }

        boolean allSuccess = true;

        // 阻塞并等待子线程返回值，值得注意的是：子线程返回值的顺序不一定是ts的顺序
        for (int j = 0; j < size; j++) {
            try {
                // 最先执行完成的子线程的返回值
                Result<R> workResult = completionService.take().get();
                R result = workResult.getResult();
                if (results != null) {
                    results.add(result);
                }

                boolean success = true;
                if (result instanceof Boolean) {
                    success = BooleanUtils.isTrue((Boolean) result);
                }
                LOGGER.debug(LOG_FORMAT_RETURN,
                        new Object[] { size, callback.getName(), j + 1, workResult.getWorkName(),
                                success ? "success" : "failed" });
                if (!success) {
                    allSuccess = false;
                 }
            } catch (CancellationException ignore) {
                LOGGER.debug(LOG_FORMAT_RETURN,
                        new Object[] { size, callback.getName(), j + 1, "UNKNOWN", "exception" });
                allSuccess = false;
                LOGGER.warn("Ignored a cancellationException, {}", ignore);
            } catch (InterruptedException e) {
                LOGGER.debug(LOG_FORMAT_RETURN,
                        new Object[] { size, callback.getName(), j + 1, "UNKNOWN", "exception" });
                allSuccess = false;
                LOGGER.error("An InterruptedException happened when " + THREAD_NAME + " is running", e);
                 Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.debug(LOG_FORMAT_RETURN,
                        new Object[] { size, callback.getName(), j + 1, "UNKNOWN", "exception" });
                LOGGER.error("An ExecutionException happened when " + THREAD_NAME + " is running, {}", e.getMessage());
                 // 将ExecutionException向外抛出
                throw new BaseException(e.getMessage(), e);
            }
        }

        LOGGER.debug(LOG_FORMAT_PREFIX + "[End: {}]", new Object[] { size, callback.getName(),
                allSuccess ? "success" : "failed" });

        return allSuccess;
    }

    /**
     * 阻塞式并发执行多个任务，当所有任务都执行完成时此方法才返回，如果任一任务失败，则向所有观察者发送通知消息
     * 
     * @param ts 待处理的目标集合
     * @param callback 回调函数
     * @return 批量任务全部执行成功则返回true，否则返回false
     */
    @Override
    public boolean execute(Collection<T> ts, Callback<T, R> callback) {
        return execute(ts, callback, null);
    }
 
//    private static ExecutorService executor = Executors.newFixedThreadPool(20, new BasicThreadFactory(
//            "SimpelAsyncExecutor"));
//
//    public static abstract class Task implements Runnable {
//        private String name;
//
//        public Task(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//    }
//
//    public void exec(Task task) {
//        LOGGER.info(task.getName());
//        executor.execute(task);
//    }

}
