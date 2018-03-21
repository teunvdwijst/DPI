package message;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Message;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import loanclient.LoanClientFrame;
import model.loan.LoanRequest;

/**
 *
 * @author Teun
 */
public class ClientToBrokerSender {

    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and
    Destination sendDestination; // reference to a queue/topic destination
    MessageProducer producer; // for sending messages

    public ClientToBrokerSender(LoanRequest message, LoanClientFrame frame) {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue.ClientToBroker"), "ClientToBroker");

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            sendDestination = (Destination) jndiContext.lookup("ClientToBroker");
            producer = session.createProducer(sendDestination);

            Message msg = session.createTextMessage(message.toString());
            
            // send the message     
            producer.send(msg);
            frame.addCorrelation(msg.getJMSMessageID(), message);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}
