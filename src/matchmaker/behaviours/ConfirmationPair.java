package matchmaker.behaviours;

import java.util.List;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import matchmaker.Matchmaker;

public final class ConfirmationPair extends Behaviour {
    /**
     *
     */
    private final Matchmaker matchmaker;
    private AID targetAID;
    private boolean replied;
    private List<AID> list;
    private MessageTemplate template;

    public ConfirmationPair(Matchmaker matchmaker, AID targetAID, List<AID> list) {
        super(matchmaker);
        this.matchmaker = matchmaker;
        this.targetAID = targetAID;
        this.list = list;
        template = MessageTemplate.and(
                MessageTemplate.MatchSender(this.targetAID),
                MessageTemplate.and(
                        MessageTemplate.MatchConversationId("pair"),
                        MessageTemplate.or(
                                MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                                MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL))));
    }

    @Override
    public void action() {
        ACLMessage msg = this.matchmaker.receive(template);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                list.remove(targetAID);
                matchmaker.updateGUI();
                replied = true;
            } else {
                replied = true;
            }
            block(1000);
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return replied;
    }
}