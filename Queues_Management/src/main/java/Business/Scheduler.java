package Business;

import Model.Server;
import Model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    private List<Server> servers;
    private int maxNoServers;
    private int maxTasksPerServer;
    private ConcreteStrategy strategy;

    public Scheduler(int maxNoServers, int maxTasksPerServer) {
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        strategy=new ConcreteStrategy();
        servers=new ArrayList<>();
        //TODO
        //create server object
        //create thread with the object
        for(int i=0; i<maxNoServers; i++){
            Server aux=new Server(new ArrayBlockingQueue<>(maxTasksPerServer),new AtomicInteger(0));
            servers.add(i,aux);
            Thread serverThread=new Thread(aux);
            serverThread.start();
        }
    }
    public void dispatchTask(Task t) {
        //TODO
        //Call the strategy addTask method
        strategy.addTask(servers,t);
    }

    public int getMaxNoServers() {
        return maxNoServers;
    }

    public int getMaxTasksPerServer() {
        return maxTasksPerServer;
    }

    public void setMaxNoServers(int maxNoServers) {
        this.maxNoServers = maxNoServers;
    }

    public void setMaxTasksPerServer(int maxTasksPerServer) {
        this.maxTasksPerServer = maxTasksPerServer;
    }

    public List<Server> getServers() {
        return servers;
    }
}
