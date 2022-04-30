package commun.behaviours;

import actuador.ActuadorTermostato;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sensor.SensorTemperatura;

public class ListenerProposePair extends CyclicBehaviour {

    private ActuadorTermostato actuador;
    private SensorTemperatura sensor;
    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchConversationId("pair"),
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));

    public ListenerProposePair(SensorTemperatura sensor) {
        super(sensor);
        this.sensor = sensor;
    }

    public ListenerProposePair(ActuadorTermostato actuador) {
        super(actuador);
        this.actuador = actuador;
    }

    @Override
    public void action() {
        ACLMessage msg = this.myAgent.receive(template);
        if (msg != null) {
            if (actuador != null) {
                String name = msg.getUserDefinedParameter("sensor");
                AID aid = new AID(name, AID.ISGUID);
                actuador.setSensorAID(aid);
            } else {
                String name = msg.getUserDefinedParameter("actuador");
                AID aid = new AID(name, AID.ISGUID);
                sensor.setActuadorAID(aid);
            }
            sendConfirmation(msg);
        } else {
            block();
        }
    }

    private void sendConfirmation(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        this.myAgent.send(reply);
    }

}
