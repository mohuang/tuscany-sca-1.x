package org.apache.tuscany.core.wire.jdk;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.apache.tuscany.core.util.MethodHashMap;
import org.apache.tuscany.spi.wire.TargetInvocationChain;
import org.apache.tuscany.spi.wire.TargetWire;
import org.apache.tuscany.spi.wire.WireInvocationHandler;

/**
 * Creates proxies that are returned to non-SCA clients using JDK dynamic proxy facilities and front a wire.
 * The proxies implement the business interface associated with the target service of the wire and are
 * typically returned by a locate operation.
 *
 * @version $Rev: 394431 $ $Date: 2006-04-15 21:27:44 -0700 (Sat, 15 Apr 2006) $
 */
public class JDKTargetWire<T> implements TargetWire<T> {

    private Class[] businessInterfaces;
    private Map<Method, TargetInvocationChain> invocationChains = new MethodHashMap<TargetInvocationChain>();
    private String serviceName;

    @SuppressWarnings("unchecked")
    public T createProxy() {
        WireInvocationHandler handler = new JDKInvocationHandler();
        handler.setConfiguration(invocationChains);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), businessInterfaces, handler);
    }

    public void setBusinessInterface(Class interfaze) {
        businessInterfaces = new Class[]{interfaze};
    }

    @SuppressWarnings("unchecked")
    public Class<T> getBusinessInterface() {
        return businessInterfaces[0];
    }

    public void addInterface(Class claz) {
        throw new UnsupportedOperationException("Additional proxy interfaces not yet supported");
    }

    public Class[] getImplementedInterfaces() {
        return businessInterfaces;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<Method, TargetInvocationChain> getInvocationChains() {
        return invocationChains;
    }

    public void addInvocationChains(Map<Method, TargetInvocationChain> chains) {
        invocationChains.putAll(chains);
    }

    public void addInvocationChain(Method method, TargetInvocationChain chain) {
        invocationChains.put(method, chain);
    }

}
