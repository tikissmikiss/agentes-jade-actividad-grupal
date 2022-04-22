package sensor.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sensor.SensorTemperatura;

public final class ListenerProposeSubscription extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String AGENTE = "Agente ";
    private MessageTemplate templateSubcribe = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchConversationId("subscribe"));
    private MessageTemplate templateUnsubcribe = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CANCEL),
            MessageTemplate.MatchConversationId("subscription"));
    SensorTemperatura s;

    public ListenerProposeSubscription(SensorTemperatura sensor) {
        super(sensor);
        s = sensor;
    }

    @Override
    public void action() {
        ACLMessage msg = s.receive(templateSubcribe);
        if (msg != null) {
            s.getLogger().info(AGENTE + s.getLocalName() + " recibe mensaje PROPOSE");
            ACLMessage reply = msg.createReply();
            if (s.isSubscribed()) {
                reply.setPerformative(ACLMessage.REFUSE);
                s.getLogger().info(AGENTE + s.getLocalName() + " rechaza mensaje PROPOSE");
            } else {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.addUserDefinedParameter("name", s.getLocalName());
                reply.addUserDefinedParameter("type", "Sensor");
                reply.setContent(s.getLocalName());
                s.getLogger().info(AGENTE + s.getLocalName() + " acepta mensaje PROPOSE");
            }
            s.send(reply);
            s.addBehaviour(new ListenerConfirmSubscription(s));
        }
        // Comprobar cancelacion de suscripción
        msg = s.receive(templateUnsubcribe);
        if (msg != null) {
            s.getLogger().info(AGENTE + s.getLocalName() + " recibe mensaje CANCEL suscripcion");
            s.setMatchmakerAID(null);
        } else {
            block();
        }
    }
}
