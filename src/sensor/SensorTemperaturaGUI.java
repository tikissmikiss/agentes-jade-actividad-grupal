package sensor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class SensorTemperaturaGUI extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int ANCHO_VENTANA = 450;
    private static final int ALTO_VENTANA = 80;
    private static final double ESCALAR_POSICION_VERTICAL = 0.15;
    private static final double ESCALAR_POSICION_HORIZONTAL = 0.10;
    private static final double VERTICAL_UTIL_PANTALLA = 0.75;

    private SensorTemperatura myAgent;

    private double temperatura;
    private double humedad;
    private double presion;
    private boolean status;


    private JLabel lblTemperatura;
    private JLabel lblHumedad;
    private JLabel lblPresion;

    private JTextField txtStatus;

    public SensorTemperaturaGUI(SensorTemperatura a) {
        super("Agente Sensor Temperatura - " + a.getLocalName());
        myAgent = a;
        initView();
        initComponents();
        showGUI();
    }

    private void initView() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setEventoCierreVentana();
    }

    private void showGUI() {
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setMinimumSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (d.getWidth() * ESCALAR_POSICION_HORIZONTAL),
                (ALTO_VENTANA * myAgent.getNumInstancia() % (int) (d.getHeight() * VERTICAL_UTIL_PANTALLA)
                        + (int) (d.getHeight() * ESCALAR_POSICION_VERTICAL)));
        setVisible(true);
    }

    private void setEventoCierreVentana() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Finalizar agente
                myAgent.doDelete();
                super.windowClosed(e);
            }
        });
    }

    private void initComponents() {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 23, 12));
        temperatura = myAgent.getTemperatura();
        humedad = myAgent.getHumedad();
        presion = myAgent.getPresion();
        txtStatus = new JTextField("ON", 3);
        txtStatus.setHorizontalAlignment(0);
        txtStatus.setEditable(false);
        setStatusGUI();
        p.add(txtStatus);
        lblTemperatura = new JLabel(String.format("Temp: %.1f ºC", temperatura));
        lblHumedad = new JLabel(String.format("Humedad: %.0f %%", humedad));
        lblPresion = new JLabel(String.format("Presión: %.0f hPa", presion));
        p.add(lblTemperatura);
        p.add(lblHumedad);
        p.add(lblPresion);
        this.add(p);
    }

    public void update() {
        temperatura = myAgent.getTemperatura();
        humedad = myAgent.getHumedad();
        presion = myAgent.getPresion();
        status = myAgent.isStatus();
        lblTemperatura.setText(String.format("Temp: %.1f ºC", temperatura));
        lblHumedad.setText(String.format("Humedad: %.0f %%", humedad));
        lblPresion.setText(String.format("Presión: %.0f hPa", presion));
        setStatusGUI();
    }

    private void setStatusGUI() {
        txtStatus.setText(this.status ? "ON" : "OFF");
        txtStatus.setBackground(this.status ? Color.RED : Color.GREEN);
    }

}
