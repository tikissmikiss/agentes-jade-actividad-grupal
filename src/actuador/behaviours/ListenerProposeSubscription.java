package actuador.behaviours;

import actuador.ActuadorTermostato;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public final class ListenerProposeSubscription extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String AGENTE = "Agente ";
    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchConversationId("subscribe"));
    private MessageTemplate templateUnsubcribe = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CANCEL),
            MessageTemplate.MatchConversationId("subscription"));
    ActuadorTermostato a;

    public ListenerProposeSubscription(ActuadorTermostato actuador) {
        super(actuador);
        a = actuador;
    }

    @Override
    public void action() {
        ACLMessage msg = a.receive(template);
        if (msg != null) {
            a.getLogger().info(AGENTE + a.getLocalName() + " recibe mensaje PROPOSE");
            ACLMessage reply = msg.createReply();
            if (a.isSubscribed()) {
                reply.setPerformative(ACLMessage.REFUSE);
                a.getLogger().info(AGENTE + a.getLocalName() + " rechaza mensaje PROPOSE");
            } else {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.addUserDefinedParameter("name", a.getLocalName());
                reply.addUserDefinedParameter("type", "Actuador");
                reply.setContent(a.getLocalName());
                a.getLogger().info(AGENTE + a.getLocalName() + " acepta mensaje PROPOSE");
            }
            a.send(reply);
            a.addBehaviour(new ListenerConfirmSubscription(a));
        }
        // Comprobar cancelacion de suscripción
        msg = a.receive(templateUnsubcribe);
        if (msg != null) {
            a.getLogger().info(AGENTE + a.getLocalName() + " recibe mensaje CANCEL suscripcion");
            a.setMatchmakerAID(null);
        } else {
            block();
        }
    }
}
