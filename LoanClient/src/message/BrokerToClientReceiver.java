package message;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import loanclient.LoanClientFrame;
import model.bank.BankInterestReply;
import model.loan.LoanReply;
import org.apache.activemq.command.ActiveMQTextMessage;

/**
 *
 * @author Teun
 */
public class BrokerToClientReceiver {
    
    public BrokerToClientReceiver(LoanClientFrame frame) {
        Connection connection; // to connect to the JMS
        Session session; // session for creating consumers
        Destination receiveDestination; //reference to a queue/topic destination
        MessageConsumer consumer = null; // for receiving messages

        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue.BrokerToClient"), "BrokerToClient");
            
            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup("BrokerToClient");
            consumer = session.createConsumer(receiveDestination);
            connection.start(); // this is needed to start receiving messages
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
        
        try {
            consumer.setMessageListener(new MessageListener() {
                
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("received message: " + msg);
                        ActiveMQTextMessage message = (ActiveMQTextMessage) msg;
                        String json = message.getText();
                        LoanReply lr = jsonToLoanReply(json);
                        frame.addRequestReply(msg.getJMSCorrelationID(), lr);
                    } catch (JMSException ex) {
                        Logger.getLogger(BrokerToClientReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    private LoanReply jsonToLoanReply(String json) {
        String quoteid = json.substring(json.indexOf("quote=") + 6, json.indexOf("interest="));
        double interest = Double.parseDouble(json.substring(json.indexOf("interest=") + 9));
        
        return new LoanReply(interest, quoteid);
    }
}
