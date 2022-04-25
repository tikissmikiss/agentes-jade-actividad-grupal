package sensor;

import java.time.LocalDate;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import sensor.behaviours.ListenerConfirmSubscription;
import sensor.behaviours.ListenerProposeSubscription;
import sensor.behaviours.PeriodicGUIUpdater;

public class SensorTemperatura extends Agent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Umbrales media y desviacion estandar para la temperatura, humedad y presion
     */
    private static final int[] AVG_PRESS = { 885, 1077 };

    private static final int[] SD_PRESS = { 5, 15 };

    private static final int[] SD_HUM = { 5, 20 };

    private static final int[] AVG_HUM = { 60, 80 };

    private static final int[] SD_TEMP = { 5, 10 };

    private static final int[] AVG_TEMP = { 18, 30 };

    private static final int UPDATE_PERIOD_MS = 1000;

    private static final String AGENTE = "Agente ";

    private static final String TYPE = "Sensor";

    private static int contInstancias = 0;

    private static Random rdm = new Random();

    private int numInstancia;

    private double temperatura;

    private double humedad;

    private double presion;

    private int mediaTemperatura;

    private int desviacionTemperatura;

    private int mediaHumedad;

    private int desviacionHumedad;

    private int desvPresion;

    private int mediaPresion;

    public Logger logger = Logger.getMyLogger(this.getClass().getName());

    public SensorTemperaturaGUI gui;

    private AID actuadorAID;

    private AID matchmakerAID;

    private static void incrementContInstancias() {
        contInstancias++;
    }

    @Override
    protected void setup() {
        logger.info(AGENTE + getLocalName() + " en línea. (" + getAID().getName() + ")");

        initAtributos();

        // registro en el DF
        registrarse();

        // iniciar interfaz de ususario
        gui = new SensorTemperaturaGUI(this);

        // Comportamiento para actualizar GUI cada cierto tiempo
        addBehaviour(new PeriodicGUIUpdater(this, UPDATE_PERIOD_MS));

        // Buscar servicio de Matchmaker en DF
        DFAgentDescription[] m = findMatchmaker();
        if (m != null && m.length > 0) {
            subscribeMatchmaker(m[0].getName());
            // Comportamiento para escuchar confirmaciones de subscripcion de Matchmaker
            addBehaviour(new ListenerConfirmSubscription(this));
        }

        // Comportamiento para escuchar propuestas de subscripcion de Matchmaker
        addBehaviour(new ListenerProposeSubscription(this));

        // TODO: Comportamiento TickerBehaviour para comunicar datos con actuador.
        // TODO: Alternativa - Se puede incluir junto a la actualizacion del GUI
        // addBehaviour(new SensorUpdaterBehaviour(this, UPDATE_PERIOD_MS));
    }

    private DFAgentDescription[] findMatchmaker() {
        DFAgentDescription[] res = null;
        logger.info("Buscando Matchmaker...");
        // Descripcion del servicio buscado
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Matchmaker");
        DFAgentDescription ad = new DFAgentDescription();
        ad.addServices(sd);
        try {
            res = DFService.search(this, ad);
            // Maximo habrá un solo agente que implemente el servicio Machmaker
            if (res.length != 0) {
                logger.info(AGENTE + getLocalName() + " ha encontrado el agente "
                        + res[0].getName() + " que implementa el servicio Machmaker. ("
                        + getAID().getName() + ")");
            } else {
                logger.info(AGENTE + getLocalName()
                        + " no ha encontrado agente que implemente el servicio Machmaker. ("
                        + getAID().getName() + ")");
            }
        } catch (FIPAException fe) {
            logger.log(Logger.SEVERE, "Error en la busqueda de agentes", fe);
            fe.printStackTrace();
        }
        return res;
    }

    private void subscribeMatchmaker(AID matchmaker) {
        // Envio de un mensaje
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.addReceiver(matchmaker);
        msg.addUserDefinedParameter("name", getLocalName());
        msg.addUserDefinedParameter("type", TYPE);
        msg.setConversationId("subscribe");
        msg.setContent(getLocalName());
        send(msg);
    }

    private void unsubscribeMatchmaker(AID matchmaker) {
        // Envio de un mensaje
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.addReceiver(matchmaker);
        msg.addUserDefinedParameter("name", getLocalName());
        msg.addUserDefinedParameter("type", TYPE);
        msg.setConversationId("unsubscribe");
        msg.setContent(getLocalName());
        send(msg);
    }

    private void initAtributos() {
        numInstancia = contInstancias;
        incrementContInstancias();
        desvPresion = rdm.nextInt((SD_PRESS[1] - SD_PRESS[0]) + 1) + SD_PRESS[0];
        mediaPresion = rdm.nextInt((AVG_PRESS[1] - AVG_PRESS[0]) + 1) + AVG_PRESS[0];
        presion = updatePresion();
        mediaHumedad = rdm.nextInt((AVG_HUM[1] - AVG_HUM[0]) + 1) + AVG_HUM[0];
        desviacionHumedad = rdm.nextInt((SD_HUM[1] - SD_HUM[0]) + 1) + SD_HUM[0];
        humedad = updateHumedad();
        mediaTemperatura = rdm.nextInt((AVG_TEMP[1] - AVG_TEMP[0]) + 1) + AVG_TEMP[0];
        desviacionTemperatura = rdm.nextInt((SD_TEMP[1] - SD_TEMP[0]) + 1) + SD_TEMP[0];
        temperatura = updateTemperatura();
    }

    /** Crear Registro del servicio en el DF (Definition Facilitator) */
    private void registrarse() {
        logger.info(AGENTE + getLocalName() + " registrando el servicio en el DF (Definition Facilitator).");

        // Crear Descripcion del servicio que ofrece el agente
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TYPE);
        sd.setName(this.getLocalName());
        sd.setOwnership("Grupo-SMPC");

        // Crear Descripcion del Agente
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(this.getAID());
        // Añadir el servicio a la descripcion del agente
        dfd.addServices(sd);

        try {
            // Comprobar si ya está registrado
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length > 0) {
                logger.log(Logger.WARNING, "Agente ya registrado en el DF como " + getLocalName());
                doDelete();
            } else {
                DFService.register(this, dfd);
            }
        } catch (FIPAException e) {
            logger.log(Logger.SEVERE, "Error al registrar el agente en el DF", e);
            e.printStackTrace();
        }
    }

    /**
     * Se ejecuta cuando el agente muere
     */
    @Override
    protected void takeDown() {
        // Si esta registrado en un matchmaker, se desuscribe
        if (matchmakerAID != null) {
            unsubscribeMatchmaker(matchmakerAID);
        }
        // Dar de baja el registro en el DF
        try {
            DFService.deregister(this);
            logger.info("Baja en registro DF exitosa.");
        } catch (FIPAException fe) {
            logger.log(Logger.SEVERE, "Error al deregistrar agente", fe);
            fe.printStackTrace();
        }
        System.out.println(LocalDate.now().toString() + " Agente " + this.getName() + " finalizado.");
    }

    private String getDataString() {
        return "{" +
                "\"sensor\":\"" + this.getLocalName() + "\"," +
                "\"temperatura\":\"" + getTemperatura() + "\"," +
                "\"humedad\":\"" + getHumedad() + "\"," +
                "\"presion\":\"" + getPresion() + "\"" +
                "}";
    }

    public void sendDataToActuador() {
        if (actuadorAID != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(actuadorAID);
            msg.addUserDefinedParameter("temperatura", String.valueOf(getTemperatura()));
            msg.addUserDefinedParameter("humedad", String.valueOf(getHumedad()));
            msg.addUserDefinedParameter("presion", String.valueOf(getPresion()));
            msg.setContent(getDataString());
            send(msg);
        } else {
            logger.log(Logger.WARNING, "No se ha encontrado el actuador");
            throw new NoPairRuntimeException("No se ha emparejado con un actuador");
        }
    }

    private double updatePresion() {
        return senoidal(mediaPresion, desvPresion);
    }

    private double updateHumedad() {
        return senoidal(mediaHumedad, desviacionHumedad);
    }

    private double updateTemperatura() {
        return senoidal(mediaTemperatura, desviacionTemperatura);
    }

    private static double senoidal(double ref, double amplitud) {
        double nanoToDecenaSeg = 10e10;
        return ref + amplitud - Math.sin(System.nanoTime() / nanoToDecenaSeg) * amplitud;
    }

    public int getNumInstancia() {
        return numInstancia;
    }

    public double getTemperatura() {
        temperatura = updateTemperatura();
        return temperatura;
    }

    public double getHumedad() {
        humedad = updateHumedad();
        return humedad;
    }

    public double getPresion() {
        presion = updatePresion();
        return presion;
    }

    public AID getActuadorAID() {
        return actuadorAID;
    }

    public void setActuadorAID(AID actuadorAID) {
        this.actuadorAID = actuadorAID;
    }

    public AID getMatchmakerAID() {
        return matchmakerAID;
    }

    public void setMatchmakerAID(AID matchmakerAID) {
        this.matchmakerAID = matchmakerAID;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isSubscribed() {
        return matchmakerAID != null;
    }

}
