package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import jade.core.Agent;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Dashboard extends Agent {

    /**
     *
     */
    private static final String DIALOG = "Dialog";
    private static final String STR_ERROR_CREAR_AGENTE = "Error al crear el agente: ";
    private static final int ANCHO_VENTANA = 450;
    private static final int ALTO_VENTANA = 250;

    private static Dashboard unicaInstancia;

    private int nSensor;
    private int nActuador;

    // logging
    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    private static void setSingleton(Dashboard app) {
        unicaInstancia = app;
    }

    @Override
    protected void setup() {
        if (unicaInstancia == null) {
            setSingleton(this);
            runApp();
            super.setup();
        } else {
            logger.info("Aplicación ya en ejecución. Solo se permite una instancia.");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        logger.log(Logger.INFO, getName() + ": Dashboard terminated");
    }

    public void runApp() {
        JFrame frame = new JFrame("APP Dashboard");
        frame.getContentPane().setLayout(new BorderLayout());
        windowListeners(frame);
        frame.add(initPanel());
        frame.pack();

        showGUI(frame);
    }

    private void showGUI(JFrame f) {
        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        f.setSize(ANCHO_VENTANA, ALTO_VENTANA);
        f.setMinimumSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) d.getWidth() / 2;
        int centerY = (int) d.getHeight() / 2;
        // f.setLocation(centerX - f.getWidth() / 2, centerY / 2 - f.getHeight() / 2);
        f.setLocation(centerX - f.getWidth() / 2, centerY / 3 - f.getHeight() / 2);
        f.setVisible(true);
    }

    private JPanel initPanel() {
        JPanel mainPanel = new JPanel();
        GridLayout l = new GridLayout(4, 1);
        l.setVgap(3);
        mainPanel.setLayout(l);

        mainPanel.add(initHeaderPanel());
        mainPanel.add(initSubPanel1());
        mainPanel.add(initSubPanel2());
        mainPanel.add(initSubPanel3());

        return mainPanel;
    }

    private JPanel initSubPanel1() {
        JPanel p1 = new JPanel();
        JButton btnPlataforma = new JButton("\nAbrir Agente Matchmaker\n");
        btnPlataforma.setFont(new Font(DIALOG, Font.BOLD, 13));
        btnPlataforma.setMargin(new Insets(6, 6, 6, 6));

        p1.add(btnPlataforma);
        // Clic en el boton de Matchmaker
        String strMatchmaker = "Matchmaker";
        btnPlataforma.addActionListener(new AbstractAction(strMatchmaker) {
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente Matchmaker");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent(strMatchmaker, "matchmaker.Matchmaker", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });
        return p1;
    }

    private JPanel initSubPanel2() {
        JPanel p2 = new JPanel();
        JButton btnSensor = new JButton("Crear un agente sensor");
        JButton btnActuador = new JButton("Crear un agente actuador");
        Font font = new Font(DIALOG, Font.BOLD, 13);
        btnSensor.setFont(font);
        btnActuador.setFont(font);
        Insets m = new Insets(6, 6, 6, 6);
        btnSensor.setMargin(m);
        btnActuador.setMargin(m);

        // Clic en el boton para lanzar el agente Sensor
        String strSensor = "Sensor";
        btnSensor.addActionListener(new AbstractAction("newAgentSensor") {
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente sensor");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent(strSensor + ++nSensor, "sensor.SensorTemperatura", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });

        // Clic en el boton para lanzar el agente Actuador
        String strActuador = "Actuador";
        btnActuador.addActionListener(new AbstractAction("newAgentActuador") {
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente actuador");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent(strActuador + ++nActuador, "actuador.ActuadorTermostato", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });

        p2.add(btnSensor);
        p2.add(new JLabel("        "));
        p2.add(btnActuador);
        return p2;
    }

    private Component initSubPanel3() {
        Font fuenteDialogo = new Font(DIALOG, Font.PLAIN, 11);
        JPanel panel = new JPanel();

        panel.add(new JLabel("Herramientas:"));

        JButton btnDummy = new JButton("Agente Dummy");
        btnDummy.setMargin(new Insets(0, 0, 0, 0));
        btnDummy.setFont(fuenteDialogo);
        btnDummy.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente Dummy");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent("Dummy", "jade.tools.DummyAgent.DummyAgent", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });
        panel.add(btnDummy);

        JButton btnSniffer = new JButton("Agente Sniffer");
        btnSniffer.setMargin(new Insets(0, 0, 0, 0));
        btnSniffer.setFont(fuenteDialogo);
        btnSniffer.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente Sniffer");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent("Sniffer", "jade.tools.sniffer.Sniffer", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });
        panel.add(btnSniffer);

        JButton btnInstrospector = new JButton("Agente Instrospector");
        btnInstrospector.setMargin(new Insets(0, 0, 0, 0));
        btnInstrospector.setFont(fuenteDialogo);
        btnInstrospector.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Creando agente Instrospector");
                try {
                    AgentController ac = getContainerController()
                            .createNewAgent("Instrospector", "jade.tools.introspector.Introspector", null);
                    ac.start();
                } catch (StaleProxyException e1) {
                    logger.warning(STR_ERROR_CREAR_AGENTE + e1);
                }
            }
        });
        panel.add(btnInstrospector);

        return panel;
    }

    private JTextField initHeaderPanel() {
        JTextField f1 = new JTextField("SMPC - Dashboard Agentes Jade - Actv Grupal", 3);
        f1.setHorizontalAlignment(0);
        f1.setEditable(false);
        f1.setBackground(new Color(214, 255, 214));
        return f1;
    }

    private void windowListeners(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                logger.log(Logger.INFO, "Dashboard GUI Closed");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                logger.log(Logger.INFO, "Inténto de cerrar Dashboard GUI. Se requiere confirmación.");
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
                        "AVISO: Se va a cerrar la ventana del Dashboard.\n\nEsta accion detendra la plataforma Jade. Para volver a iniciar agentes desde la GUI deberá reiniciar la aplicación.\n¿Desea cerrar el dashboard?",
                        "Intentando cerrar el Dashboard",
                        JOptionPane.YES_NO_OPTION)) {
                    shutdown(frame);
                } else {
                    logger.log(Logger.INFO, "Cancelado: Mantener abierto Dashboard GUI");
                }
            }

            private void shutdown(JFrame frame) {
                logger.log(Logger.INFO, "Aceptado: Cerrando Dashboard GUI");
                // Finalizar agente
                doDelete();
                frame.dispose();
                try {
                    getContainerController().getPlatformController().kill();
                } catch (ControllerException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

}
