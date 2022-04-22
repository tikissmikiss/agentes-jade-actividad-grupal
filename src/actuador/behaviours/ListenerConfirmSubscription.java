package actuador.behaviours;

import actuador.ActuadorTermostato;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ListenerConfirmSubscription extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String AGENTE = "Agente ";
    private ActuadorTermostato a;
    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchConversationId("subscribe"),
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)));

    public ListenerConfirmSubscription(ActuadorTermostato actuador) {
        super(actuador);
        a = actuador;
    }

    @Override
    public void action() {
        ACLMessage msg = a.receive(template);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.CONFIRM) {
                a.getLogger().info(AGENTE + a.getLocalName() + " recibe mensaje CONFIRM subscripcion");
                a.setMatchmakerAID(msg.getSender());
            } else {
                a.getLogger().info(AGENTE + a.getLocalName() + " recibe mensaje REFUSE subscripcion");
            }
            a.getLogger().info(AGENTE + a.getLocalName() + "elimiando comportamiento ListenerConfirmSubscription");
            a.removeBehaviour(this);
        } else {
            block();
        }
    }

}
