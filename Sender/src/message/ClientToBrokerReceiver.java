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
import loanbroker.LoanBrokerFrame;
import model.bank.BankInterestRequest;
import model.loan.LoanRequest;
import org.apache.activemq.command.ActiveMQTextMessage;

/**
 *
 * @author Teun
 */
public class ClientToBrokerReceiver {

    public ClientToBrokerReceiver(LoanBrokerFrame frame) {
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
            props.put(("queue.ClientToBroker"), "ClientToBroker");

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup("ClientToBroker");
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
                        LoanRequest lr = jsonToLoanRequest(json);
                        BankInterestRequest bir = new BankInterestRequest(lr.getAmount(), lr.getTime());
                        BrokerToBankSender sender = new BrokerToBankSender(bir.toString(), msg.getJMSMessageID());
                        frame.add(lr, msg.getJMSMessageID());
                    } catch (JMSException ex) {
                        Logger.getLogger(ClientToBrokerReceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private LoanRequest jsonToLoanRequest(String json) {
        int ssn = Integer.parseInt(json.substring(json.indexOf("ssn=") + 4, json.indexOf(" amount")));
        int amount = Integer.parseInt(json.substring(json.indexOf("amount=") + 7, json.indexOf(" time")));
        int time = Integer.parseInt(json.substring(json.indexOf("time=") + 5));

        return new LoanRequest(ssn, amount, time);
    }
}
