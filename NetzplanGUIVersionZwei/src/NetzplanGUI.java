import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class Arbeitspaket {
    private int nummer;
    private String name;
    private int dauer;
    private List<Integer> vorgaenger;

    public Arbeitspaket(int nummer, String name, int dauer, List<Integer> vorgaenger) {
        this.nummer = nummer;
        this.name = name;
        this.dauer = dauer;
        this.vorgaenger = vorgaenger;
    }

    public int getNummer() {
        return nummer;
    }

    public String getName() {
        return name;
    }

    public int getDauer() {
        return dauer;
    }

    public List<Integer> getVorgaenger() {
        return vorgaenger;
    }
}

class Netzplan {
    private List<Arbeitspaket> arbeitspakete;

    public Netzplan() {
        arbeitspakete = new ArrayList<>();
    }

    public void addArbeitspaket(Arbeitspaket arbeitspaket) {
        arbeitspakete.add(arbeitspaket);
    }

    public void removeArbeitspaket(int arbeitspaketNummer) {
        arbeitspakete.removeIf(ap -> ap.getNummer() == arbeitspaketNummer);
    }

    public List<Arbeitspaket> getArbeitspakete() {
        return arbeitspakete;
    }

    public int berechneFAZ(int arbeitspaketNummer) {
        Arbeitspaket paket = findArbeitspaket(arbeitspaketNummer);
        int maxFEZ = 0;
        for (int vorgaengerNummer : paket.getVorgaenger()) {
            int fez = berechneFEZ(vorgaengerNummer);
            if (fez > maxFEZ) {
                maxFEZ = fez;
            }
        }
        return maxFEZ;
    }

    public int berechneFEZ(int arbeitspaketNummer) {
        Arbeitspaket paket = findArbeitspaket(arbeitspaketNummer);
        int faz = berechneFAZ(arbeitspaketNummer);
        return faz + paket.getDauer();
    }

    public int berechneSEZ(int arbeitspaketNummer) {
        Arbeitspaket paket = findArbeitspaket(arbeitspaketNummer);
        List<Integer> nachfolger = findeNachfolger(arbeitspaketNummer);
        int sez;
        if (nachfolger.isEmpty()) {
            sez = berechneFEZ(arbeitspaketNummer);
        } else {
            sez = Integer.MAX_VALUE;
            for (int nachfolgerNummer : nachfolger) {
                int saz = berechneSAZ(nachfolgerNummer);
                if (saz < sez) {
                    sez = saz;
                }
            }
        }
        return sez;
    }

    public int berechneSAZ(int arbeitspaketNummer) {
        Arbeitspaket paket = findArbeitspaket(arbeitspaketNummer);
        int sez = berechneSEZ(arbeitspaketNummer);
        return sez - paket.getDauer();
    }

    public int berechneFP(int arbeitspaketNummer) {
        return berechneSAZ(arbeitspaketNummer) - berechneFAZ(arbeitspaketNummer);
    }

    public void alleWerteAusgeben(JTextArea textArea) {
        for (Arbeitspaket paket : arbeitspakete) {
            int nummer = paket.getNummer();
            textArea.append("Arbeitspaket " + nummer + ": " + paket.getName() + "\n");
            textArea.append("FAZ: " + berechneFAZ(nummer) + "\n");
            textArea.append("FEZ: " + berechneFEZ(nummer) + "\n");
            textArea.append("SAZ: " + berechneSAZ(nummer) + "\n");
            textArea.append("SEZ: " + berechneSEZ(nummer) + "\n");
            textArea.append("FP: " + berechneFP(nummer) + "\n");
            textArea.append("\n");
        }
    }

    public void allePaketeVisualisieren(JPanel panel) {
        panel.removeAll();
        panel.setLayout(new FlowLayout()); // Änderung des Layout-Managers
        for (Arbeitspaket paket : arbeitspakete) {
            String buttonText = paket.getNummer() + " " + paket.getName();
            JButton paketButton = new JButton(buttonText);

            // Kritische Pfadpakete rot färben
            if (berechneFP(paket.getNummer()) == 0) {
                paketButton.setBackground(Color.RED);
            }

            paketButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int nummer = paket.getNummer();
                    String details = "Arbeitspaket " + nummer + ": " + paket.getName() + "\n" +
                            "Dauer: " + paket.getDauer() + "\n" +
                            "FAZ: " + berechneFAZ(nummer) + "\n" +
                            "FEZ: " + berechneFEZ(nummer) + "\n" +
                            "SAZ: " + berechneSAZ(nummer) + "\n" +
                            "SEZ: " + berechneSEZ(nummer) + "\n" +
                            "FP: " + berechneFP(nummer) + "\n";
                    JOptionPane.showMessageDialog(null, details, "Details", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            panel.add(paketButton);
        }
        panel.revalidate();
        panel.repaint();
    }

    private Arbeitspaket findArbeitspaket(int nummer) {
        return arbeitspakete.stream()
                .filter(ap -> ap.getNummer() == nummer)
                .findFirst()
                .orElseThrow();
    }

    private List<Integer> findeNachfolger(int arbeitspaketNummer) {
        List<Integer> nachfolger = new ArrayList<>();
        for (Arbeitspaket paket : arbeitspakete) {
            if (paket.getVorgaenger().contains(arbeitspaketNummer)) {
                nachfolger.add(paket.getNummer());
            }
        }
        return nachfolger;
    }
}

public class NetzplanGUI {
    private JFrame frame;
    private JTextField nameField;
    private JTextField dauerField;
    private JTextField vorgaengerField;
    private JTextArea outputArea;
    private JPanel visualPanel;
    private Netzplan netzplan;
    private int currentNummer = 1;

    public NetzplanGUI() {
        netzplan = new Netzplan();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Netzplan");
        frame.setBounds(100, 100, 1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        inputPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel lblName = new JLabel("Name:");
        inputPanel.add(lblName);

        nameField = new JTextField();
        inputPanel.add(nameField);
        nameField.setColumns(10);

        JLabel lblDauer = new JLabel("Dauer:");
        inputPanel.add(lblDauer);

        dauerField = new JTextField();
        inputPanel.add(dauerField);
        dauerField.setColumns(10);

        JLabel lblVorgaenger = new JLabel("Vorgänger (kommagetrennt):");
        inputPanel.add(lblVorgaenger);

        vorgaengerField = new JTextField();
        inputPanel.add(vorgaengerField);
        vorgaengerField.setColumns(10);

        JButton addButton = new JButton("Arbeitspaket hinzufügen");
        inputPanel.add(addButton);

        JButton removeButton = new JButton("Arbeitspaket löschen");
        inputPanel.add(removeButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        visualPanel = new JPanel();
        visualPanel.setBackground(Color.WHITE);
        JScrollPane visualScrollPane = new JScrollPane(visualPanel);
        visualScrollPane.setPreferredSize(new Dimension(400, 400));
        frame.getContentPane().add(visualScrollPane, BorderLayout.SOUTH);

        JButton displayButton = new JButton("Arbeitspakete anzeigen");
        frame.getContentPane().add(displayButton, BorderLayout.EAST);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                int dauer = Integer.parseInt(dauerField.getText());
                String[] vorgaengerStr = vorgaengerField.getText().split(",");
                List<Integer> vorgaenger = new ArrayList<>();
                for (String s : vorgaengerStr) {
                    if (!s.trim().isEmpty()) {
                        vorgaenger.add(Integer.parseInt(s.trim()));
                    }
                }
                Arbeitspaket paket = new Arbeitspaket(currentNummer++, name, dauer, vorgaenger);
                netzplan.addArbeitspaket(paket);
                nameField.setText("");
                dauerField.setText("");
                vorgaengerField.setText("");
                outputArea.append("Arbeitspaket " + paket.getNummer() + " " + paket.getName() + " wurde erstellt.\n");
                netzplan.allePaketeVisualisieren(visualPanel);
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                for (Arbeitspaket paket : new ArrayList<>(netzplan.getArbeitspakete())) {
                    if (paket.getName().equals(name)) {
                        netzplan.removeArbeitspaket(paket.getNummer());
                    }
                }
                nameField.setText("");
                dauerField.setText("");
                vorgaengerField.setText("");
                outputArea.append("Arbeitspaket " + name + " wurde gelöscht.\n");
                netzplan.allePaketeVisualisieren(visualPanel);
            }
        });

        displayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                netzplan.alleWerteAusgeben(outputArea);
                netzplan.allePaketeVisualisieren(visualPanel);
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    NetzplanGUI window = new NetzplanGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
