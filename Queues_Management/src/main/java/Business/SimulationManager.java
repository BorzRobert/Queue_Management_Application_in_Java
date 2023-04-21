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

public class SimulationManager implements Runnable {
    public int timeLimit;
    public int maxProcessingTime;
    public int minProcessingTime;
    public int maxArrivalTime;
    public int minArrivalTime;
    public int numberOfServers;
    public int numberOfClients;
    public boolean ready;

    private Scheduler scheduler;
    private SimulationFrame frame;
    private List<Task> generatedTasks = new ArrayList<>();

    public SimulationManager() {
        frame = new SimulationFrame(null);
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
                    ready = true;
                    scheduler = new Scheduler(numberOfServers, numberOfClients);
                    //System.out.println(timeLimit + " " + maxProcessingTime + " " + minProcessingTime + " " + maxArrivalTime + " " + minArrivalTime + " " + numberOfClients + " " + numberOfServers);
                }
                for (int i = 0; i < numberOfClients; i++) {
                    int id = i + 1;
                    int processingTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
                    int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
                    generatedTasks.add(new Task(id, arrivalTime, processingTime));
                }
                Collections.sort(generatedTasks, new ClientsComparator());
                //for(int i=0; i<numberOfClients; i++) { //TODO:DELETE AFTERWARDS
                //System.out.println(generatedTasks.get(i).getID() + " " + generatedTasks.get(i).getArrivalTime() + " " + generatedTasks.get(i).getServiceTime());
            }
        });
    }

    @Override
    public void run() {
        int currentTime = 0;
        while (!ready) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Erase the log for the previous simulation
        try {
            FileWriter writer = new FileWriter("result.txt", false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (currentTime < timeLimit) {
            List<Task> deletedTasks=new ArrayList<>();
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
                for (Task task : generatedTasks) {
                    bufferedWriter.write("(" + task.getID() + "," + task.getArrivalTime() + "," + task.getServiceTime() + ");");
                }
                bufferedWriter.write("\n");
                for (int i = 0; i < scheduler.getServers().size(); i++) {
                    int aux = i + 1;
                    bufferedWriter.write("Queue " + aux + " :");
                    if (scheduler.getServers().get(i).getTasks().size() == 0)
                        bufferedWriter.write("closed\n");
                    else
                    {
                        bufferedWriter.write("in use\n");
                    }

                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Writing in the GUI report
            frame.reportTextArea.append("Time:" + currentTime + "\n");
            frame.reportTextArea.append("Waiting Clients:\n");
            for (Task task : generatedTasks) {
                frame.reportTextArea.append("(" + task.getID() + "," + task.getArrivalTime() + "," + task.getServiceTime() + ");");
            }
            frame.reportTextArea.append("\n");
            for (int i = 0; i < scheduler.getServers().size(); i++) {
                int aux = i + 1;
                frame.reportTextArea.append("Queue " + aux + " :");
                if (scheduler.getServers().get(i).getTasks().size() == 0)
                    frame.reportTextArea.append("closed\n");
                else
                    frame.reportTextArea.append("in use\n");
            }
            currentTime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

