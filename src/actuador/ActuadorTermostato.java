package actuador;

import java.time.LocalDate;

import actuador.behaviours.ListenerConfirmSubscription;
import actuador.behaviours.ListenerProposeSubscription;
import commun.behaviours.ListenerProposePair;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class ActuadorTermostato extends Agent {

    private static final long serialVersionUID = 1L;

    private static final String TYPE = "Actuador";

    private static int contInstancias = 0;

    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    private ActuadorTermostatoGUI gui;

    private int numInstancia;

    private double presion;

    private double humedad;

    private double temperatura;

    private AID matchmakerAID;

    private AID sensorAID;

    private boolean status;

    private static void incrementContInstancias() {
        contInstancias++;
    }

    @Override
    protected void setup() {
        logger.info("Agente Actuador Termostato " + getAID().getName() + " en línea.");

        // Inicializar atributos
        initAtributos();

        // registro en el DF
        registrarse();

        // iniciar interfaz de ususario
        gui = new ActuadorTermostatoGUI(this);

        // Buscar servicio de Matchmaker en DF
        DFAgentDescription[] m = findMatchmaker();
        if (m != null && m.length > 0) {
            subscribeMatchmaker(m[0].getName());
            // Comportamiento para escuchar confirmaciones de subscripcion de Matchmaker
            addBehaviour(new ListenerConfirmSubscription(this));
        }

        // Comportamiento para escuchar propuestas de subscripcion de Matchmaker
        addBehaviour(new ListenerProposeSubscription(this));

        // Comportamiento para escuchar propuestas de emparejado
        addBehaviour(new ListenerProposePair(this));

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
                logger.info("Agente " + getLocalName() + " ha encontrado el agente "
                        + res[0].getName() + " que implementa el servicio Machmaker. ("
                        + getAID().getName() + ")");
                // subscribeMatchmaker();
            } else {
                logger.info("Agente " + getLocalName()
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

    private void registrarse() {
        logger.info("Agente " + getLocalName() + " registrando el servicio en el DF (Definition Facilitator).");

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

    public AID getMatchmakerAID() {
        return matchmakerAID;
    }

    public void setMatchmakerAID(AID matchmakerAID) {
        this.matchmakerAID = matchmakerAID;
    }

    public Logger getLogger() {
        return logger;
    }

    private void initAtributos() {
        numInstancia = contInstancias;
        incrementContInstancias();
    }

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

    public double getTemperatura() {
        return temperatura;
    }

    public double getHumedad() {
        return humedad;
    }

    public double getPresion() {
        return presion;
    }

    public int getNumInstancia() {
        return numInstancia;
    }

    public boolean isSubscribed() {
        return matchmakerAID != null;
    }

    public void setSensorAID(AID sensorAID) {
        this.sensorAID = sensorAID;
        addBehaviour(new ActuatorToSensorConversation(this));
    }

    public AID getSensorAID() {
        return sensorAID;
    }

    public void updateAndReplySensor(ACLMessage msg) {
        // TODO: verificar que el mensaje sea del sensor
        String nameSensor = msg.getSender().getName();
        logger.info("Recibiendo datos sensor: " + nameSensor);

        temperatura = Double.parseDouble(msg.getUserDefinedParameter("temperatura"));
        humedad = Double.parseDouble(msg.getUserDefinedParameter("humedad"));
        presion = Double.parseDouble(msg.getUserDefinedParameter("presion"));
        status = temperatura <= gui.getThreshold();
        gui.updateGUI(nameSensor, temperatura, humedad, presion, status);

        ACLMessage reply = msg.createReply();
        reply.addUserDefinedParameter("estado", String.valueOf(status));
        send(reply);
    }

}
