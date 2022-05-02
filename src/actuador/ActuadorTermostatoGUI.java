package actuador;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class ActuadorTermostatoGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final String FORMATO_SENSOR = "Sensor: %s";

    private static final String FORMATO_PRESION = "Presión: %s hPa";

    private static final String FORMATO_HUMEDAD = "Humedad: %s %%";

    private static final String FORMATO_TEMPERATURA = "Temperatura: %s ºC";

    private static final int ANCHO_VENTANA = 500;

    private static final int ALTO_VENTANA = 140;

    private static final double ESCALAR_POSICION_VERTICAL = 0.15;

    private static final double ESCALAR_POSICION_HORIZONTAL = 0.90;

    private static final double VERTICAL_UTIL_PANTALLA = 0.75;

    private ActuadorTermostato myAgent;

    private JLabel lblSensor = new JLabel(String.format(FORMATO_SENSOR, "< No conectado >"));

    private JLabel lblTemperatura = new JLabel(String.format(FORMATO_TEMPERATURA, "~~~"));

    private JLabel lblHumedad = new JLabel(String.format(FORMATO_HUMEDAD, "~~~"));

    private JLabel lblPresion = new JLabel(String.format(FORMATO_PRESION, "~~~"));

    private double temperatura;

    private double humedad;

    private double presion;

    private boolean status;

    private JSpinner spinnerTemperatura;

    private JTextField txtStatus;

    public ActuadorTermostatoGUI(ActuadorTermostato a) {
        super("Agente Actuador Termostato - " + a.getLocalName());
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
        setLocation((int) (d.getWidth() * ESCALAR_POSICION_HORIZONTAL - getWidth()),
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
        // Main Panel
        JPanel pMain = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 3));
        JPanel pC1 = new JPanel(new GridLayout(4, 1, 0, 6));
        JPanel pC2 = new JPanel(new GridLayout(2, 1, 0, 3));
        JPanel pC3 = new JPanel(new GridLayout(2, 1, 0, 3));
        pMain.add(pC1);
        pMain.add(pC2);
        pMain.add(pC3);
        // C1
        pC1.add(lblSensor);
        pC1.add(lblTemperatura);
        pC1.add(lblHumedad);
        pC1.add(lblPresion);
        // C2
        pC2.add(new JLabel("Temperatura Consigna:"));
        spinnerTemperatura = new JSpinner();
        spinnerTemperatura.setValue(25);
        pC2.add(spinnerTemperatura);
        // C3
        pC3.add(new JLabel("Estado:"));
        txtStatus = new JTextField(this.status ? "ON" : "OFF", 3);
        txtStatus.setHorizontalAlignment(0);
        txtStatus.setEditable(false);
        // txtStatus.setBackground(new Color(214, 255, 214));
        setStatusGUI();
        pC3.add(txtStatus);

        this.add(pMain);
    }

    private String strPresion() {
        return String.format("%.0f", presion);
    }

    private String strHumedad() {
        return String.format("%.0f", humedad);
    }

    private String strTemperatura() {
        return String.format("%.1f", temperatura);
    }

    public void updateGUI(String sensor, double temp, double humedad, double presion, boolean status) {
        this.temperatura = temp;
        this.humedad = humedad;
        this.presion = presion;
        this.status = status;
        lblSensor.setText(String.format(FORMATO_SENSOR, sensor));
        lblTemperatura.setText(String.format(FORMATO_TEMPERATURA, strTemperatura()));
        lblHumedad.setText(String.format(FORMATO_HUMEDAD, strHumedad()));
        lblPresion.setText(String.format(FORMATO_PRESION, strPresion()));
        setStatusGUI();
    }

    private void setStatusGUI() {
        txtStatus.setText(this.status ? "ON" : "OFF");
        txtStatus.setBackground(this.status ? Color.RED : Color.GREEN);
    }

    public int getThreshold() {
        return (int) spinnerTemperatura.getValue();
    }

}
