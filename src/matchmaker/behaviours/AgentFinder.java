package matchmaker.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import matchmaker.Matchmaker;

/**
 * Clase OneShotBehaviour para descubrimiento de agentes Sensor y Actuador.
 * Envia una propuesta de subscripcion a todos los agentes encontrados.
 */
public final class AgentFinder extends OneShotBehaviour {
    /**
     *
     */
    private final Matchmaker matchmaker;

    public AgentFinder(Matchmaker matchmaker) {
        super(matchmaker);
        this.matchmaker = matchmaker;
    }

    @Override
    public void action() {
        findAgents("Sensor");
        findAgents("Actuador");
        this.matchmaker.updateGUI();
    }

    @Override
    public int onEnd() {
        this.reset();
        return super.onEnd();
    }

    /**
     * Actualiza la lista de agentes conocidos
     * 
     * @param type tipo de agente que se quiere descubrir
     */
    private void findAgents(String type) {
        // Descripcion del servicio buscado
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        DFAgentDescription plantilla = new DFAgentDescription();
        plantilla.addServices(sd);
        try {
            DFAgentDescription[] res = DFService.search(myAgent, plantilla);
            for (int i = 0; i < res.length; ++i) {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.addReceiver(res[i].getName());
                msg.setConversationId("subscribe");
                matchmaker.send(msg);
            }
        } catch (FIPAException fe) {
            this.matchmaker.getLogger().log(Logger.WARNING, "Error en la busqueda de agentes", fe);
        }
    }
}
