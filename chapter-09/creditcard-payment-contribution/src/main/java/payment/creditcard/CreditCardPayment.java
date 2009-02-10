
package payment.creditcard;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.3-b02-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "CreditCardPayment", targetNamespace = "http://www.example.org/CreditCardPayment/")
public interface CreditCardPayment {


    /**
     * 
     * @param amount
     * @param creditCard
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "http://www.example.org/CreditCardPayment/authorize")
    @WebResult(name = "Status", targetNamespace = "")
    @RequestWrapper(localName = "authorize", targetNamespace = "http://www.example.org/CreditCardPayment/", className = "payment.creditcard.AuthorizeType")
    @ResponseWrapper(localName = "authorizeResponse", targetNamespace = "http://www.example.org/CreditCardPayment/", className = "payment.creditcard.AuthorizeResponseType")
    public String authorize(
        @WebParam(name = "CreditCard", targetNamespace = "")
        CreditCardDetailsType creditCard,
        @WebParam(name = "Amount", targetNamespace = "")
        float amount);

}
