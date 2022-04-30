package matchmaker.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Properties;
import matchmaker.Matchmaker;

public final class ListenerSubscriptors extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String AGENTE = "Agente ";
    private Matchmaker m;
    private MessageTemplate template = MessageTemplate.or(
            MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
            MessageTemplate.MatchConversationId("subscribe"));

    public ListenerSubscriptors(Matchmaker matchmaker) {
        super(matchmaker);
        m = matchmaker;
    }

    @Override
    public void action() {
        // Recibir mensajes
        ACLMessage msg = m.receive(template);
        if (msg != null) {
            if (isValidPerformative(msg)) {
                if (isUnsubscribe(msg)) {
                    m.getLogger().info(AGENTE + m.getLocalName() + " recibe mensaje UNSUBSCRIBE");
                    removeSubscriptor(msg);
                } else {
                    m.getLogger().info(AGENTE + m.getLocalName()
                            + " recibe peticion de subscripción de: " + msg.getSender());
                    addSubscriptor(msg);
                }
            } else {
                m.getLogger().info(AGENTE + m.getLocalName()
                        + " recibe mensaje con performativa: " + msg.getPerformative());
            }
        } else {
            block();
        }
    }

    private void removeSubscriptor(ACLMessage msg) {
        if (m.getActuadorList().contains(msg.getSender())) {
            m.getActuadorList().remove(msg.getSender());
            m.updateGUI();
        }
        if (m.getSensorList().contains(msg.getSender())) {
            m.getSensorList().remove(msg.getSender());
            m.updateGUI();
        }
    }

    private static boolean isUnsubscribe(ACLMessage msg) {
        return "unsubscribe".equals(msg.getConversationId());
    }

    /**
     * Para los casos de ConversationID = "subscribe" pueden llegar performativas
     * REFUSE que si no se dejan pasar se acumulan en la cola de mensajes. Con esto
     * podemos dejar que el template les permita pasar y evitar tratarlos como
     * subscriptores.
     */
    private static boolean isValidPerformative(ACLMessage msg) {
        return msg.getPerformative() == ACLMessage.SUBSCRIBE || msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL;
    }

    private void addSubscriptor(ACLMessage msg) {
        // El template no deberia permitir nigun otro ConversationId, aun asi lo
        // comprobamos.
        if ("subscribe".equals(msg.getConversationId())) {
            Properties prop = msg.getAllUserDefinedParameters();
            if (prop.containsKey("type")) {
                // Añadir agente a la lista de agentes conocidos
                if ("Sensor".equals(prop.get("type"))) {
                    m.getSensorList().add(msg.getSender());
                    sendConfirmation(msg);
                } else if ("Actuador".equals(prop.get("type"))) {
                    m.getActuadorList().add(msg.getSender());
                    sendConfirmation(msg);
                } else {
                    m.getLogger().warning(AGENTE
                            + m.getLocalName()
                            + " recibe mensaje de tipo desconocido: "
                            + prop.get("type"));
                }
                m.updateGUI();
            }
        } else {
            String str = AGENTE + m.getLocalName()
                    + " recibe mensaje con ConversationId no esperado: "
                    + msg.getConversationId();
            m.getLogger().warning(str);
        }
    }

    private void sendConfirmation(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.CONFIRM);
        m.send(reply);
    }
}