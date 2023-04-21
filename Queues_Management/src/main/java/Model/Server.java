package Model;

import Model.Task;

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
        //TODO
        //add task to queue
        //increment waiting period
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    public void run() {
        //TODO
        //take next task from queue
        //stop thread for a time equal with the tasks processing time
        //decrement the waiting period
        while(true){
            try {
                Thread.sleep(tasks.take().getServiceTime() * 1000);
                waitingPeriod.addAndGet(-tasks.take().getServiceTime());
            }catch (InterruptedException e) {
                System.out.println("The queue is empty");
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
