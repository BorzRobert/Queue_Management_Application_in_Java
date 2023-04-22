package Model;

import Model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;

    public Server(BlockingQueue<Task> tasks, AtomicInteger waitingPeriod) {
        this.tasks = tasks;
        this.waitingPeriod = waitingPeriod;
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    public void run() {
        //TODO
        //take next task from queue
        //stop thread for a time equal with the tasks processing time
        //decrement the waiting period
        List<Task> serverTasks = new ArrayList<>();
        while (true) {
            while (tasks.isEmpty()) {
                try {
                    //System.out.println("inca e goala");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!tasks.isEmpty()) {
                waitingPeriod.decrementAndGet();
                tasks.peek().setServiceTime(tasks.peek().getServiceTime() - 1);
                if (tasks.peek().getServiceTime() == 0)
                    tasks.remove(tasks.peek());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setTasks(BlockingDeque<Task> tasks) {
        this.tasks = tasks;
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

}
