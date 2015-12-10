/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.reporting.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.amazonaws.util.StringInputStream;

/**
 * SerializerUtil
 * 
 * @author dangleton
 * 
 */
public final class SerializerUtil {

    /**
     * no-arg private constructor to implement Util Pattern
     */
    private SerializerUtil() {
    }

    /**
     * Marshalls the object to a String. <br/>
     * Example: <code>
     * String marshalled = JaxbUtil.marshall(env);
     * </code>
     * 
     * @param toMarshall
     *            the object to Marshall
     * @return the String representation
     * @throws JAXBException
     *             if there is an error marshalling the object
     */
    public static final InputStream marshall(Object toMarshall) throws IOException {
        StringWriter sw = null;
        try {
            JAXBContext ctx = JAXBContext.newInstance(toMarshall.getClass().getPackage().getName());
            sw = new StringWriter();
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(toMarshall, sw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new StringInputStream(sw.toString());
    }

    /**
     * UnMarshalls a String to the specidfied object type. <br/>
     * Example: <code>
     * Environment unmarshalled = JaxbUtil.<Environment>unmarshall(marshalled, Environment.class);
     * </code>
     * 
     * @param <T>
     *            the type of object being unmarshalled
     * @param toUnmarshall
     *            the String to unmarshall
     * @param clazz
     *            the class of the unMarshsalled object
     * @return the Object
     * @throws JAXBException
     *             if there is an error unmarshalling the String
     */
    @SuppressWarnings("unchecked")
    public static final <T> T unmarshall(InputStream toUnmarshall, Class<T> clazz) throws IOException {
        Object unmarshalled = null;
        try {
            JAXBContext ctx = JAXBContext.newInstance(clazz.getPackage().getName());
            unmarshalled = ctx.createUnmarshaller().unmarshal(new InputStreamReader(toUnmarshall));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) unmarshalled;
    }
}
