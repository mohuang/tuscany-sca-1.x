package org.osoa.sca;

/**
 * @version $Rev$ $Date$
 */
public interface Conversation {
    /**
     * Returns the identifier for this conversation.
     * If a user-defined identity had been supplied for this reference then its value will be returned;
     * otherwise the identity generated by the system when the conversation was initiated will be returned.
     *
     * @return the identifier for this conversation
     */
    Object getConversionID();

    /**
     * End this conversation.
     */
    void end();
}
