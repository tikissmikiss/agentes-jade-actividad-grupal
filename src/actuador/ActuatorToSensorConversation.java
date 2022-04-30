package actuador;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ActuatorToSensorConversation extends CyclicBehaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final ActuadorTermostato actuador;

    public ActuatorToSensorConversation(ActuadorTermostato actuador) {
        super(actuador);
        this.actuador = actuador;
    }

    @Override
    public void action() {
        waitMessage();
    }

    private void waitMessage() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchConversationId("data"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msg = actuador.receive(template);

        if (msg != null) {
            actuador.updateAndReplySensor(msg);
        } else {
            block();
        }
    }

}
