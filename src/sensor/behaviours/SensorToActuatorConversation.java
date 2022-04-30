package sensor.behaviours;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sensor.SensorTemperatura;

/**
 * Se ejecuta periodicamente cada un tiempo predefinido
 */
public final class SensorToActuatorConversation extends Behaviour {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int SENDING = 0, RECEIVING = 1;

    private final SensorTemperatura sensor;

    private long period;

    private int state;

    public SensorToActuatorConversation(SensorTemperatura sensor, long period) {
        super(sensor);
        this.period = period;
        this.sensor = sensor;
        state = SENDING;
    }

    @Override
    public void action() {
        switch (state) {
            case SENDING:
                sendMessage();
                break;
            case RECEIVING:
                waitMessage();
                break;
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
        block(period);
    }

    private void waitMessage() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchConversationId("data"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msg = sensor.receive(template);
        if (msg != null) {
            String s = msg.getUserDefinedParameter("estado");
            sensor.getLogger().info("Estado del sensor: " + s);
            sensor.setStatus(!"false".equals(s));
            state = SENDING;
        } else {
            block();
        }
        block();
    }

    private void sendMessage() {
        sensor.getLogger().info("Agente " + myAgent.getLocalName() + ": Enviando datos...");
        sensor.sendDataToActuador();
        state = RECEIVING;
    }

    @Override
    public boolean done() {
        return sensor.getActuadorAID() == null;
    }
}
