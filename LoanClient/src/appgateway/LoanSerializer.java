package appgateway;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import java.text.SimpleDateFormat;
import model.loan.LoanReply;
import model.loan.LoanRequest;

/**
 *
 * @author Teun
 */
public class LoanSerializer {

    public LoanSerializer() {
    }

    public String requestToString(LoanRequest request) {
        return new Genson().serialize(request);

    }

    public LoanRequest loanrequestFromString(String str) {
        return new Genson().deserialize(str, LoanRequest.class);

    }

    public String replyToString(LoanReply reply) {
        return new Genson().serialize(reply);
    }

    public LoanReply loanreplyFromString(String str) {
        return new Genson().deserialize(str, LoanReply.class);
    }
}
