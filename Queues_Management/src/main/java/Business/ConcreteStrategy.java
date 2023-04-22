package Business;

import Model.Server;
import Model.Task;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcreteStrategy implements Strategy {
    public float waitingTime;

    public ConcreteStrategy(float waitingTime) {
        this.waitingTime = waitingTime;
    }

    @Override
    public void addTask(List<Server> servers, Task t) {
        int minimum = 0;
        if (servers.size() != 0 && servers.get(0).getWaitingPeriod() != null)
            minimum = servers.get(0).getWaitingPeriod().get();
        //Case in which there exists an empty queue
        for (Server server : servers) {
            if (server.getTasks().size() == 0) {
                server.addTask(t);
                return;
            }
        }
        //No empty queue, search for the queue with the smallest waiting time
        for (Server server : servers) {
            if (server.getWaitingPeriod().get() < minimum)
                minimum = server.getWaitingPeriod().get();
        }
        for (Server server : servers) {
            if (server.getWaitingPeriod().get() == minimum) {
                server.addTask(t);
                waitingTime+=minimum;
                return;
            }
        }
    }
}
