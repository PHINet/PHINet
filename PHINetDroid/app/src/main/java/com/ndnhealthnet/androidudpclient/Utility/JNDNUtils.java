package com.ndnhealthnet.androidudpclient.Utility;

import net.named_data.jndn.ContentType;
import net.named_data.jndn.Data;
import net.named_data.jndn.Exclude;
import net.named_data.jndn.Interest;
import net.named_data.jndn.KeyLocator;
import net.named_data.jndn.KeyLocatorType;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.Sha256WithRsaSignature;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.util.Blob;

import java.nio.ByteBuffer;

/**
 * Uses jNDN library to create and manipulate NDN-compliant packets
 * Source: https://github.com/named-data/jndn
 */
public class JNDNUtils {

    public JNDNUtils() {}

    static final double DEFAULT_FRESHNESS_PERIOD = 1000 * 60 * 60; // an hour

    /**
     * Returns decoded Interest packet
     *
     * @param byteBuffer - an encoded Interest packet
     * @return Interest packet if input valid; otherwise, false
     */
    public static Interest decodeInterest(ByteBuffer byteBuffer) {

        Interest interest = new Interest();

        try {

            interest.wireDecode(byteBuffer);

        } catch (EncodingException e) {

            interest = null; // failed to decode; assign null value
        }

        return interest;
    }

    /**
     * Returns decoded Interest packet
     *
     * @param blob - an encoded Interest Packet
     * @return Interest packet if input valid; otherwise, false
     */
    public static Interest decodeInterest(Blob blob) {
        return decodeInterest(blob.buf());
    }

    /**
     * Returns decoded Data packet
     *
     * @param byteBuffer - an encoded Data packet
     * @return Data packet if input valid; otherwise, false
     */
    public static Data decodeData(ByteBuffer byteBuffer) {

        Data data = new Data();

        try {
            data.wireDecode(byteBuffer);
        } catch (EncodingException e) {

            data = null; // failed to decode; assign null value
        }

        return data;
    }

    /**
     * Returns decoded Data packet
     *
     * @param blob - an encoded Data packet
     * @return Data packet if input valid; otherwise, false
     */
    public static Data decodeData(Blob blob) {
        return decodeData(blob.buf());
    }

    /**
     * TODO - document
     *
     * @param data
     * @param name
     * @param locatorType
     * @return
     */
    public static KeyLocator createKeyLocator(Blob data, String name, KeyLocatorType locatorType) {
        KeyLocator keyLocator = new KeyLocator();

        keyLocator.setKeyData(data);
        keyLocator.setKeyName(new Name(name));
        keyLocator.setType(locatorType);

        return keyLocator;
    }

    /**
     * TODO - document
     *
     * @param finalBlockID
     * @param freshnessPeriod
     * @param cType
     * @return
     */
    public static MetaInfo createMetaInfo(Name.Component finalBlockID, double freshnessPeriod, ContentType cType) {

        MetaInfo metaInfo = new MetaInfo();

        metaInfo.setFinalBlockId(finalBlockID);
        metaInfo.setFreshnessPeriod(freshnessPeriod);
        metaInfo.setType(cType);

        return metaInfo;
    }

    /**
     * Uses default values
     *
     * @return
     */
    public static MetaInfo createMetaInfo() {
        MetaInfo metaInfo = new MetaInfo();

      // TODO - rework with this included; this field is option - we ignore for now
                        //   metaInfo.setFinalBlockId(finalBlockID);
        metaInfo.setFreshnessPeriod(DEFAULT_FRESHNESS_PERIOD);



        metaInfo.setType(ContentType.BLOB); // blob is the default type

        return metaInfo;
    }
    /**
     * Creates applications-specific name
     *
     * TODO - doc
     *
     * @param userID
     * @param sensorID
     * @param timeString
     * @param processID
     * @return
     */
    public static Name createName(String userID, String sensorID, String timeString, String processID) {
       String name = "/ndn/" + userID + "/" + sensorID + "/" + timeString + "/" + processID;

        return new Name(name);
    }

    /**
     *
     * TODO - doc
     *
     * @param content
     * @param metaInfo
     * @param name
     * @return
     */
    public static Data createDataPacket(String content, MetaInfo metaInfo, String name) {

        // TODO - how to use signature?

        Sha256WithRsaSignature signature = new Sha256WithRsaSignature();

        Data data = new Data();

        data.setContent(new Blob(content));
        data.setMetaInfo(metaInfo);
        data.setName(new Name(name));
        data.setSignature(signature);

        return data;
    }

    /**
     * uses the default meta info
     *
     * TODO - doc
     *
     * @param content
     * @param name
     * @return
     */
    public static Data createDataPacket(String content, String name) {

        // TODO - how to use signature?

        Sha256WithRsaSignature signature = new Sha256WithRsaSignature();

        Data data = new Data();

        data.setContent(new Blob(content));
        data.setMetaInfo(createMetaInfo());
        data.setName(new Name(name));
        data.setSignature(signature);

        return data;
    }

    /**
     * uses the default meta info
     *
     * TODO - doc
     *
     * @param content
     * @param name
     * @return
     */
    public static Data createDataPacket(String content, Name name) {

        return createDataPacket(content, name.toUri());
    }

    /**
     *
     *  TODO - doc
     *
     * @param content
     * @param metaInfo
     * @param name
     * @return
     */
    public static Data createDataPacket(String content, MetaInfo metaInfo, Name name) {


        return createDataPacket(content, metaInfo, name.toUri());
    }

    /**
     *
     *  TODO - doc
     *
     * @param childSelector
     * @param interestLifetimeMillis
     * @param keyLocator
     * @param mustBeFresh
     * @param maxSuffixComponents
     * @param minSuffixComponents
     * @param name
     * @param scope
     * @return
     */
    public static Interest createInterestPacket(int childSelector, double interestLifetimeMillis,
                                                KeyLocator keyLocator, boolean mustBeFresh, int maxSuffixComponents,
                                                int minSuffixComponents, String name, int scope) {

        Exclude exclude = new Exclude(); // TODO - how to use exclude

        Interest interest = new Interest();

        interest.setChildSelector(childSelector);
        interest.setExclude(exclude);
        interest.setInterestLifetimeMilliseconds(interestLifetimeMillis);
        interest.setKeyLocator(keyLocator);
        interest.setMustBeFresh(mustBeFresh);
        interest.setMaxSuffixComponents(maxSuffixComponents);
        interest.setMinSuffixComponents(minSuffixComponents);
        interest.setName(new Name(name));
        interest.setScope(scope);

        return interest;
    }

    /**
     * the default interest
     *
     * TODO - doc
     *
     * @param name
     * @return
     */
    public static Interest createInterestPacket(String name) {

        return new Interest(new Name(name));

    }

    /**
     * the default interest
     *
     * TODO - doc
     *
     * @param name
     * @return
     */
    public static Interest createInterestPacket(Name name) {

        return createInterestPacket(name.toUri());

    }
}
