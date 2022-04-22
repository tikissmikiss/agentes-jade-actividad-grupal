package sensor.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sensor.SensorTemperatura;

public class ListenerConfirmSubscription extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String AGENTE = "Agente ";
    private SensorTemperatura s;
    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchConversationId("subscribe"),
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)));

    public ListenerConfirmSubscription(SensorTemperatura sensor) {
        super(sensor);
        s = sensor;
    }

    @Override
    public void action() {
        ACLMessage msg = s.receive(template);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.CONFIRM) {
                s.getLogger().info(AGENTE + s.getLocalName() + " recibe mensaje CONFIRM subscripcion");
                s.setMatchmakerAID(msg.getSender());
            } else {
                s.getLogger().info(AGENTE + s.getLocalName() + " recibe mensaje REFUSE subscripcion");
            }
            s.getLogger().info(AGENTE + s.getLocalName() + "elimiando comportamiento ListenerConfirmSubscription");
            s.removeBehaviour(this);
        } else {
            block();
        }
    }

}
