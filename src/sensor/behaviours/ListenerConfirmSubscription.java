package sensor.behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sensor.SensorTemperatura;

public class ListenerConfirmSubscription extends Behaviour {
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

    private boolean confirmado;

    public ListenerConfirmSubscription(SensorTemperatura sensor) {

        super(sensor);

        s = sensor;

    }

    @Override
    public void action() {

        ACLMessage msg = s.receive(template);

        if (msg != null) {

            if (msg.getPerformative() == ACLMessage.CONFIRM) {

                s.getLogger().info(AGENTE + s.getLocalName()
                        + " recibe mensaje CONFIRM subscripcion");

                s.setMatchmakerAID(msg.getSender());

            } else {

                s.getLogger().info(AGENTE + s.getLocalName()
                        + " recibe mensaje REFUSE subscripcion");

            }

            s.getLogger().info(AGENTE + s.getLocalName()
                    + "elimiando comportamiento ListenerConfirmSubscription");

            this.confirmado = true;

        } else {

            block();

        }
    }

    /**
     * Se ejecuta siempre despues de action(). Si devuelve true el comportamiento
     * finaliza, si devuelve false se ejecuta de nuevo action()
     */
    @Override
    public boolean done() {

        return confirmado;

    }

}
