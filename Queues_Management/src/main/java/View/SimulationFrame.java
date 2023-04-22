package View;

import javax.swing.*;
import java.awt.*;

public class SimulationFrame extends JDialog {
    private JPanel mainPanel;
    public JButton simulateButton;
    public JTextField numberOfClientsTextField;
    public JTextField numberOfQueuesTextField;
    public JTextField simulationTimeTextField;
    public JTextField minimumArrivalTimeTextFieldF;
    public JTextField maximumArrivalTimeTextFieldF;
    public JTextField minimumProcessingTimeTextField;
    public JTextField maximumProcessingTimeTextField;
    public JTextArea reportTextArea;

    public SimulationFrame(JFrame parent) {
        super(parent);
        setTitle("SimulationFrame");
        setContentPane(mainPanel);
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
