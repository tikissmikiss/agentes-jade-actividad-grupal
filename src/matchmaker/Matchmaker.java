package matchmaker;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import matchmaker.behaviours.AgentFinder;
import matchmaker.behaviours.ConfirmationPair;
import matchmaker.behaviours.ListenerSubscriptors;

public class Matchmaker extends Agent {
    /**
     *
     */
    private static final String AGENTE = "Agente ";

    private static final long serialVersionUID = 1L;

    private static Matchmaker unicaInstancia;

    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    // Listas de los agentes conocidos
    private ArrayList<AID> sensores = new ArrayList<>();
    private ArrayList<AID> actuadores = new ArrayList<>();

    private MatchmakerGUI gui;

    private AgentFinder listUpdater = new AgentFinder(this);

    private static void setSingleton(Matchmaker matchmaker) {
        unicaInstancia = matchmaker;
    }

    @Override
    protected void setup() {
        logger.info("Agente Matchmaker " + getAID().getName() + " en línea.");

        initSigleton();

        // Iniciar escucha subscriptores
        addBehaviour(new ListenerSubscriptors(this));

        // registro en el DF
        registrarse();

        // iniciar interfaz de ususario
        gui = new MatchmakerGUI(this);

        // Añadir comportamiento para actualizar listas
        addBehaviour(listUpdater);
    }

    private void registrarse() {
        logger.info(AGENTE + getLocalName() + " registrando el servicio en el DF (Definition Facilitator).");

        // Crear Descripcion del servicio que ofrece el agente
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Matchmaker");
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

    private void initSigleton() {
        if (unicaInstancia == null) {
            setSingleton(this);
        } else {
            logger.info("Aplicación ya en ejecución. Solo se permite una instancia.");
            doDelete();
        }
    }

    public void runUpdateLists() {
        addBehaviour(listUpdater);
    }

    public void updateGUI() {
        gui.updateComboBoxSensores(sensores);
        gui.updateComboBoxActuadores(actuadores);
    }

    public List<AID> getSensorList() {
        return sensores;
    }

    public List<AID> getActuadorList() {
        return actuadores;
    }

    @Override
    protected void takeDown() {
        // Dar de baja a todos los subscriptores
        removeSubscriptors();
        // Dar de baja el registro en el DF
        unsubscribeDF();
        unicaInstancia = null;
        logger.info(AGENTE + getLocalName() + " desconectado.");
        super.takeDown();
    }

    private void removeSubscriptors() {
        ArrayList<AID> destinatarios = new ArrayList<>();
        destinatarios.addAll(sensores);
        destinatarios.addAll(actuadores);
        for (AID d : destinatarios) {
            ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
            msg.addReceiver(d);
            msg.setConversationId("subscription");
            logger.info(AGENTE + getLocalName() + " cancelando suscripción a " + d.getName());
            send(msg);
        }
    }

    private void unsubscribeDF() {
        try {
            DFService.deregister(this);
            logger.info("Baja en registro DF exitosa.");
        } catch (FIPAException fe) {
            logger.log(Logger.SEVERE, "Error al deregistrar agente", fe);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void runPair(String sensor, String actuator) {
        AID sensorAID = null;
        AID actuadorAID = null;
        for (AID s : sensores) {
            if (s.getLocalName().equals(sensor)) {
                sensorAID = s;
            }
        }
        for (AID a : actuadores) {
            if (a.getLocalName().equals(actuator)) {
                actuadorAID = a;
            }
        }
        if (sensorAID != null && actuadorAID != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.addReceiver(sensorAID);
            msg.addReceiver(actuadorAID);
            msg.setConversationId("pair");
            msg.addUserDefinedParameter("sensor", sensorAID.getName());
            msg.addUserDefinedParameter("actuador", actuadorAID.getName());
            logger.info(AGENTE + getLocalName() + " enviando mensaje de pareo a "
                    + sensorAID.getName() + " y " + actuadorAID.getName());
            send(msg);

            addBehaviour(new ConfirmationPair(this, sensorAID, sensores));
            addBehaviour(new ConfirmationPair(this, actuadorAID, actuadores));
        } else {
            logger.log(Logger.SEVERE, "Error al enviar mensaje de pareo. Alguno de los agentes no existe.");
        }
    }

}
