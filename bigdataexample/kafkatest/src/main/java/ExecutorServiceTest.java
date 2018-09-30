import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTest {

  public static void main(String[] args) {
    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(
            5, 10, 200, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(10));
    for (int i = 0; i < 15; i++) {
      MyTask myTask = new MyTask(i);
      threadPoolExecutor.submit(myTask);
      System.out.println(
          "线程池中的线程数目"
              + threadPoolExecutor.getPoolSize()
              + ",队列中等待的任务数目:"
              + threadPoolExecutor.getQueue().size()
              + ",已经执行的任务数目： "
              + threadPoolExecutor.getCompletedTaskCount());
    }
    threadPoolExecutor.shutdown();
  }
}

class MyTask implements Runnable {

  private int taskNum = 0;

  public MyTask(int num) {
    this.taskNum = num;
  }

  @Override
  public void run() {
    System.out.println("正在执行task" + taskNum);
    try {
      Thread.currentThread().sleep(4000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("task " + taskNum + "执行完毕");
  }
}
