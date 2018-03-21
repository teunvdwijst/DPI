package appgateway;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.command.ActiveMQTextMessage;

/**
 *
 * @author Teun
 */
public class AppGateway {

    private final MessageSenderGateway sender;
    private final MessageReceiverGateway receiver;
    private final LoanSerializer serializer;

    public AppGateway(String senderChannelName, String receiverChannelName) {
        sender = new MessageSenderGateway(senderChannelName);
        receiver = new MessageReceiverGateway(receiverChannelName);
        serializer = new LoanSerializer();
    }

    public void applyForLoan(LoanRequest request) {
        String gson = serializer.requestToString(request);
        Message msg = sender.createTextMessage(gson);
        sender.send(msg);
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply) {
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                try {
                    ActiveMQTextMessage message = (ActiveMQTextMessage) msg;
                    LoanReply lr = serializer.loanreplyFromString(message.getText());
                    reply.setInterest(lr.getInterest());
                    reply.setQuoteID(lr.getQuoteID());
                } catch (JMSException ex) {
                    Logger.getLogger(AppGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
