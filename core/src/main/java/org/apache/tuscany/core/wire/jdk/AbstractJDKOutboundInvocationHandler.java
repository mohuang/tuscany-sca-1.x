package org.apache.tuscany.core.wire.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.tuscany.spi.wire.Interceptor;
import org.apache.tuscany.spi.wire.Message;
import org.apache.tuscany.spi.wire.MessageImpl;
import org.apache.tuscany.spi.wire.OutboundInvocationChain;
import org.apache.tuscany.spi.wire.TargetInvoker;
import org.apache.tuscany.spi.wire.WireInvocationHandler;

/**
 * Base class for performing invocations on an outbound chain. Subclasses are responsible for retrieving and supplying
 * the appropriate chain, target invoker and invocation arguments.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractJDKOutboundInvocationHandler implements WireInvocationHandler, InvocationHandler {

    public AbstractJDKOutboundInvocationHandler() {
    }

    protected Object invoke(OutboundInvocationChain chain, TargetInvoker invoker, Object[] args) throws Throwable {
        Interceptor headInterceptor = chain.getHeadInterceptor();
        Method method = chain.getMethod();
        if (chain.getTargetRequestChannel() == null && chain.getTargetResponseChannel() == null
            && headInterceptor == null) {
            try {
                // short-circuit the dispatch and invoke the target directly
                if (chain.getTargetInvoker() == null) {
                    throw new AssertionError("No target invoker [" + method.getName() + "]");
                }
                return chain.getTargetInvoker().invokeTarget(args);
            } catch (InvocationTargetException e) {
                // the cause was thrown by the target so throw it
                throw e.getCause();
            }
        } else {
            Message msg = new MessageImpl();
            msg.setTargetInvoker(invoker);
            msg.setBody(args);
            // dispatch the wire down the chain and get the response
            if (chain.getTargetRequestChannel() != null) {
                chain.getTargetRequestChannel().send(msg);
                Object body = msg.getRelatedCallbackMessage().getBody();
                if (body instanceof Throwable) {
                    throw (Throwable) body;
                }
                return body;

            } else if (headInterceptor == null) {
                throw new AssertionError("No target interceptor configured [" + method.getName() + "]");

            } else {
                Message resp = headInterceptor.invoke(msg);
                if (chain.getTargetResponseChannel() != null) {
                    chain.getTargetResponseChannel().send(resp);
                    resp = resp.getRelatedCallbackMessage();
                }
                Object body = resp.getBody();
                if (body instanceof Throwable) {
                    throw (Throwable) body;
                }
                return body;
            }
        }
    }

    public Object invoke(Method method, Object[] args) throws Throwable {
        return invoke(null, method, args);
    }


}
