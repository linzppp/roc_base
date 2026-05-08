package org.roc.practice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.roc.practice.annotation.SensitiveType;

import java.io.IOException;

public class SensitiveFieldSerializer extends JsonSerializer<Object> {
    private final SensitiveType type;

    public SensitiveFieldSerializer(SensitiveType type) {
        this.type = type;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(mask(String.valueOf(value)));
    }

    private String mask(String value) {
        return switch (type) {
            case PHONE -> maskPhone(value);
            case EMAIL -> maskEmail(value);
            case PASSWORD -> "***";
        };
    }

    private String maskPhone(String phone) {
        if (phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        String domain = email.substring(atIndex); // 含 @
        String local = email.substring(0, atIndex);
        return local.charAt(0) + "***" + domain;
    }
}
