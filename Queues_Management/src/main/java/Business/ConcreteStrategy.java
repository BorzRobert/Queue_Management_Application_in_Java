package Business;

import Model.Server;
import Model.Task;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcreteStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task t) {
        //Case in which there exists an empty queue
        int minimum=0;
        if(servers.size()!=0)
            minimum= servers.get(0).getWaitingPeriod().get();
        for (Server server : servers) {
            if (server.getTasks().isEmpty()) {
                server.addTask(t);
                return;
            }
        }
        //No empty queue, search for the queue with the smallest waiting time
        for (Server server : servers) {
            if (server.getWaitingPeriod().get() < minimum)
                minimum=server.getWaitingPeriod().get();
        }
        for (Server server : servers) {
            if (server.getWaitingPeriod().get() == minimum){
                server.addTask(t);
                return;
            }
        }
    }
}
