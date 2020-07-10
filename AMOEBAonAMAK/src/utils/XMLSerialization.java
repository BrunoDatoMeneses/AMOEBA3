package utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Encode/decode serializable object to/from XML
 * @author Hugo
 *
 * @param <T>
 */
public class XMLSerialization<T> {
	
	/**
	 * Encode a serializable object to a XML string
	 * @param obj
	 * @return
	 */
	public String toString(T obj) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    XMLEncoder e = new XMLEncoder(baos);
	    e.writeObject(obj);
	    e.close();
	    return new String(baos.toByteArray());
	}

	/**
	 * Decode a serializable object from a XML string
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T fromString(String str) {
	    XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(str.getBytes()));
	    T obj = (T) d.readObject();
	    d.close();
	    return obj;
	}
}
