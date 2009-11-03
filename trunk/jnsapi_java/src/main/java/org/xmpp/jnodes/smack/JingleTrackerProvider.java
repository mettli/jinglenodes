package org.xmpp.jnodes.smack;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmpp.jnodes.RelayChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.IllegalFormatException;
import java.io.IOException;

public class JingleTrackerProvider implements IQProvider {

    public JingleTrackerIQ parseIQ(final XmlPullParser parser) throws Exception {

        JingleTrackerIQ iq = new JingleTrackerIQ();

        boolean done = false;
        int eventType;
        String elementName;
        String namespace;

        while (!done) {

            eventType = parser.getEventType();
            elementName = parser.getName();
            namespace = parser.getNamespace();

            if (eventType == XmlPullParser.START_TAG) {
                final TrackerEntry.Type type;
                if (elementName.equals(TrackerEntry.Type.relay.toString())) {
                    type = TrackerEntry.Type.relay;
                } else if (elementName.equals(TrackerEntry.Type.tracker.toString())) {
                    type = TrackerEntry.Type.tracker;
                } else {
                    parser.next();
                    continue;
                }

                final JingleChannelIQ.Protocol protocol = JingleChannelIQ.Protocol.valueOf(parser.getAttributeValue(null, "protocol"));
                final TrackerEntry.Policy policy = TrackerEntry.Policy.valueOf("_" + parser.getAttributeValue(null, "policy"));
                final String address = parser.getAttributeValue(null, "address");
                final String verified = parser.getAttributeValue(null, "verified");

                if (address != null && address.length() > 0) {
                    final TrackerEntry entry = new TrackerEntry(type, policy, address, protocol);
                    if (verified != null && verified.equals("true")) {
                        entry.setVerified(true);
                    }
                    iq.addEntry(entry);
                }

            } else if (eventType == XmlPullParser.END_TAG) {
                if (elementName.equals(JingleTrackerIQ.NAME)) {
                    done = true;
                }
            }
            if (!done)
                parser.next();
        }

        return iq;
    }
}