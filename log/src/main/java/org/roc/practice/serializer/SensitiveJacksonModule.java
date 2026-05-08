package org.roc.practice.serializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.roc.practice.annotation.Sensitive;

import java.util.List;

public class SensitiveJacksonModule extends SimpleModule {
    public SensitiveJacksonModule() {
        setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription desc,
                    List<BeanPropertyWriter> beanPropertyWriters
            ) {
                for (BeanPropertyWriter writer : beanPropertyWriters) {
                    if (writer.getAnnotation(Sensitive.class) != null) {
                        writer.assignSerializer(new SensitiveFieldSerializer());
                    }
                }
                return beanPropertyWriters;
            }
        });
    }
}
