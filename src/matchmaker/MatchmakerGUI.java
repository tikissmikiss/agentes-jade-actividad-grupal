package matchmaker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import jade.core.AID;

public class MatchmakerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int ANCHO_VENTANA = 450;

    private static final int ALTO_VENTANA = 110;

    private static final double ESCALAR_POSICION_VERTICAL = 0.85;

    private static final double ESCALAR_POSICION_HORIZONTAL = 0.5;
    
    private Matchmaker matchmaker;

    private JButton btnPair = new JButton("Emparejar");

    private JButton btnUpdate = new JButton("Actualizar Listas");

    private JComboBox<Object> cmbSensores = new JComboBox<>();

    private JComboBox<Object> cmbActuadores = new JComboBox<>();

    public MatchmakerGUI(Matchmaker matchmaker) {
        super("Agente Matchmaker - " + matchmaker.getLocalName());
        this.matchmaker = matchmaker;
        initView();
        initComponents();
        showGUI();
    }

    private void initComponents() {
        // Main Panel
        JPanel pMain = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 3));
        JPanel pSub1 = new JPanel(new BorderLayout(0, 3));
        JPanel pSub2 = new JPanel(new BorderLayout(0, 3));
        JPanel pSub3 = new JPanel(new BorderLayout(0, 3));
        pMain.add(pSub1);
        pMain.add(pSub2);
        pMain.add(pSub3);
        // pSub1
        pSub1.add(new JLabel("Lista Sensores:"), BorderLayout.NORTH);
        pSub1.add(cmbSensores, BorderLayout.SOUTH);
        // pSub2
        pSub2.add(btnUpdate, BorderLayout.NORTH);
        pSub2.add(btnPair, BorderLayout.SOUTH);
        // pSub3
        pSub3.add(new JLabel("Lista Actuadores:"), BorderLayout.NORTH);
        pSub3.add(cmbActuadores, BorderLayout.SOUTH);

        initListeners();
        btnUpdate.setVisible(false);

        this.add(pMain);
    }

    private void initListeners() {
        btnPair.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                btnPairClicked();
            }
        });

        btnUpdate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                btnUpdateClicked();
            }
        });
    }

    protected void btnUpdateClicked() {
        matchmaker.runUpdateLists();
    }

    protected void btnPairClicked() {
        if (cmbSensores.getSelectedItem() != null && cmbActuadores.getSelectedItem() != null) {
            String sensor = (String) cmbSensores.getSelectedItem();
            String actuator = (String) cmbActuadores.getSelectedItem();
            matchmaker.runPair(sensor, actuator);
        }
    }

    private void initView() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setEventoCierreVentana();
    }

    private void setEventoCierreVentana() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Finalizar agente
                matchmaker.doDelete();
                super.windowClosed(e);
            }
        });
    }

    private void showGUI() {
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setMinimumSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (d.getWidth() * ESCALAR_POSICION_HORIZONTAL - ANCHO_VENTANA * 0.5),
                (int) (d.getHeight() * ESCALAR_POSICION_VERTICAL - this.getHeight()));
        setVisible(true);
    }

    public void addSensor(Object sensor) {
        cmbSensores.addItem(sensor);
    }

    public JComboBox<Object> getCmbSensores() {
        return cmbSensores;
    }

    public JComboBox<Object> getCmbActuadores() {
        return cmbActuadores;
    }

    public void updateComboBoxSensores(Iterable<AID> sensores) {
        cmbSensores.removeAllItems();
        for (AID sensor : sensores) {
            cmbSensores.addItem(sensor.getLocalName());
        }
    }

    public void updateComboBoxActuadores(Iterable<AID> actuadores) {
        cmbActuadores.removeAllItems();
        for (AID actuador : actuadores) {
            cmbActuadores.addItem(actuador.getLocalName());
        }
    }

}
