package ru.mipt1c.homework;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64.*;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class SerializeUtils {

    public static<K> byte[] serialize(K obj) {
        SerializeWrapper<K> wrapper = new SerializeWrapper<>(obj);
        return SerializationUtils.serialize(wrapper);
    }

    public static<K> String toBase64SerializedString(K obj) {
        return Base64.encodeBase64String(serialize(obj));
    }

    public static<K> K toDeserializedObj(byte[] bytes) {
        SerializeWrapper<K> wrapper = SerializationUtils.deserialize(bytes);
        return wrapper.getObj();
    }

    public static<K> K toDeserializedObj(String base64) {
        return toDeserializedObj(Base64.decodeBase64(base64));
    }
}
