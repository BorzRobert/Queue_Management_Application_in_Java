package Business;

import Model.Server;
import Model.Task;
import View.SimulationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    public int timeLimit;
    public int maxProcessingTime;
    public int minProcessingTime;
    public int maxArrivalTime;
    public int minArrivalTime;
    public int numberOfServers;
    public int numberOfClients;
    public boolean ready;
    private float averageServiceTime;
    private float averageWaitingTime;

    private Scheduler scheduler;
    private SimulationFrame frame;
    private List<Task> generatedTasks;

    public SimulationManager() {
        frame = new SimulationFrame(null);
        generatedTasks = new ArrayList<>();
        generateRandomTasks();
    }

    public void generateRandomTasks() {
        Random random = new Random();
        String aux = "";
        ready = false;
        frame.simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //CHECK THE INPUTS FROM THE USER TO BE VALID
                if (checkInput(frame.simulationTimeTextField) && checkInput(frame.maximumProcessingTimeTextField) && checkInput(frame.minimumProcessingTimeTextField) && checkInput(frame.maximumArrivalTimeTextFieldF) && checkInput(frame.minimumArrivalTimeTextFieldF) && checkInput(frame.numberOfClientsTextField) && checkInput(frame.numberOfQueuesTextField)) {
                    generatedTasks.clear();
                    frame.reportTextArea.setText(null);
                    timeLimit = Integer.parseInt(frame.simulationTimeTextField.getText());
                    maxProcessingTime = Integer.parseInt(frame.maximumProcessingTimeTextField.getText());
                    minProcessingTime = Integer.parseInt(frame.minimumProcessingTimeTextField.getText());
                    maxArrivalTime = Integer.parseInt(frame.maximumArrivalTimeTextFieldF.getText());
                    minArrivalTime = Integer.parseInt(frame.minimumArrivalTimeTextFieldF.getText());
                    numberOfClients = Integer.parseInt(frame.numberOfClientsTextField.getText());
                    numberOfServers = Integer.parseInt(frame.numberOfQueuesTextField.getText());
                    float sumOfProcessingTime = 0;
                    for (int i = 0; i < numberOfClients; i++) {
                        int id = i + 1;
                        int processingTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
                        int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
                        generatedTasks.add(new Task(id, arrivalTime, processingTime));
                        sumOfProcessingTime += processingTime;
                    }
                    Collections.sort(generatedTasks, new ClientsComparator());
                    averageServiceTime = sumOfProcessingTime / numberOfClients;
                    ready = true;
                }
            }
        });
    }

    @Override
    public void run() {
        int currentTime = 0;
        int peak=0,customersInQueues, maxCustomerInQueues = 0;
        while (!ready) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //INITIALISING scheduler AND STARTING THE THREADS FOR THE QUEUES
        scheduler = new Scheduler(numberOfServers, numberOfClients);
        Thread[] serverThread = new Thread[numberOfServers];
        for (int i = 0; i < numberOfServers; i++) {
            Server aux = new Server(new ArrayBlockingQueue<>(scheduler.getMaxTasksPerServer()), new AtomicInteger(0));
            scheduler.getServers().add(i, aux);
            serverThread[i] = new Thread(aux);
            serverThread[i].setDaemon(true);
            System.out.println("Starting the thread for the queue:" + (i + 1));
            serverThread[i].start();
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
        }
        System.out.println("START!");
        //Erase the log for the previous simulation
        try {
            FileWriter writer = new FileWriter("result.txt", false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (currentTime <= timeLimit) {
            customersInQueues = 0;
            boolean emptyQueues = true;
            List<Task> deletedTasks = new ArrayList<>();
            for (Task task : generatedTasks) {
                if (task.getArrivalTime() == currentTime) {
                    scheduler.dispatchTask(task);
                    deletedTasks.add(task);
                }
            }
            for (Task task : deletedTasks) {
                generatedTasks.remove(task);
            }
            deletedTasks.clear();
            //Generating log file
            try {
                FileWriter writer = new FileWriter("result.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write("Time:" + currentTime + "\n");
                bufferedWriter.write("Waiting Clients:\n");
                if (generatedTasks.size() != 0) {
                    for (Task task : generatedTasks) {
                        bufferedWriter.write("(" + task.getID() + "," + task.getArrivalTime() + "," + task.getServiceTime() + ");");
                    }
                } else
                    bufferedWriter.write("No clients are expected to arrive!");
                bufferedWriter.write("\n");
                for (int i = 0; i < scheduler.getServers().size(); i++) {
                    Task[] printedTasks = new Task[numberOfClients];
                    scheduler.getServers().get(i).getTasks().toArray(printedTasks);
                    int aux = i + 1;
                    bufferedWriter.write("Queue " + aux + " :");
                    if (scheduler.getServers().get(i).getTasks().size() == 0)
                        bufferedWriter.write("closed\n");
                    else {
                        for (Task printedTask : printedTasks) {
                            if (printedTask != null)
                                bufferedWriter.write("(" + printedTask.getID() + "," + printedTask.getArrivalTime() + "," + printedTask.getServiceTime() + "); ");
                        }
                        bufferedWriter.write("\n");
                    }
                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Writing in the GUI report
            frame.reportTextArea.append("Time:" + currentTime + "\n");
            frame.reportTextArea.append("Waiting Clients:\n");
            if (generatedTasks.size() != 0) {
                for (Task task : generatedTasks) {
                    frame.reportTextArea.append("(" + task.getID() + "," + task.getArrivalTime() + "," + task.getServiceTime() + ");");
                }
            } else
                frame.reportTextArea.append("No clients are expected to arrive!");
            frame.reportTextArea.append("\n");
            for (int i = 0; i < scheduler.getServers().size(); i++) {
                Task[] printedTasks = new Task[numberOfClients];
                scheduler.getServers().get(i).getTasks().toArray(printedTasks);
                int aux = i + 1;
                frame.reportTextArea.append("Queue " + aux + " :");
                if (scheduler.getServers().get(i).getTasks().size() == 0)
                    frame.reportTextArea.append("closed\n");
                else {
                    for (Task printedTask : printedTasks) {
                        if (printedTask != null)
                            frame.reportTextArea.append("(" + printedTask.getID() + "," + printedTask.getArrivalTime() + "," + printedTask.getServiceTime() + "); ");
                    }
                    frame.reportTextArea.append("\n");
                }
            }
            //Computing the number of clients in each queue at current time+ getting the peak hour
            for (int i = 0; i < numberOfServers; i++) {
                customersInQueues += scheduler.getServers().get(i).getTasks().size();
            }
            if (customersInQueues > maxCustomerInQueues) {
                maxCustomerInQueues = customersInQueues;
                peak = currentTime;
            }
            //Check if queues are empty or not
            for (int i = 0; i < numberOfServers; i++) {
                if (scheduler.getServers().get(i).getTasks().size() != 0) {
                    emptyQueues = false;
                    break;
                }
            }
            //Exit condition(Waiting list empty+queues empty)
            if (generatedTasks.size() == 0 && emptyQueues){
                break;
            }
            currentTime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int i=0; i<numberOfServers; i++)
            scheduler.getServers().get(i).ok=false;
        //Computing averageWaitingTime
        averageWaitingTime = scheduler.getStrategy().waitingTime / numberOfClients;
        //Update log file
        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter("result.txt", true));
            out.write("Average waiting time:" + averageWaitingTime + "s\n");
            out.write("Average service time:" + averageServiceTime + "s\n");
            out.write("Peak hour:" + peak + "\n");

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Update GUI interface
        frame.reportTextArea.append("Average waiting time:" + averageWaitingTime + "s\n");
        frame.reportTextArea.append("Average service time:" + averageServiceTime + "s\n");
        frame.reportTextArea.append("Peak hour:" + peak + "\n");
        System.out.println("DONE!");
    }

    public boolean checkInput(JTextField textField) {
        if (!textField.getText().isEmpty()) {
            try {
                int number = Integer.parseInt(textField.getText());
                if (number < 0) {
                    frame.showMessage("Wrong input!");
                    return false;
                }

            } catch (NumberFormatException ex) {
                frame.showMessage("Wrong input!");
                return false;
            }
        } else {
            frame.showMessage("Incomplete input!");
            return false;
        }
        return true;
    }

    class ClientsComparator implements java.util.Comparator<Task> {
        @Override
        public int compare(Task a, Task b) {
            return a.getArrivalTime() - b.getArrivalTime();
        }
    }

    public static void main(String[] args) {
        SimulationManager gen = new SimulationManager();
        Thread t = new Thread(gen);
        t.start();
    }
}

