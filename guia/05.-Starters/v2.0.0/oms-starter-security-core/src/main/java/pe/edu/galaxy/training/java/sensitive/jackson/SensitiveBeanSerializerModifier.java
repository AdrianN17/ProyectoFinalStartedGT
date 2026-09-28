package pe.edu.galaxy.training.java.sensitive.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;

import java.util.ArrayList;
import java.util.List;

public class SensitiveBeanSerializerModifier extends BeanSerializerModifier {

    private final MaskService maskService;
    private final SensitiveSecurityProperties properties;

    public SensitiveBeanSerializerModifier(MaskService maskService,
                                           SensitiveSecurityProperties properties) {
        this.maskService = maskService;
        this.properties = properties;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        if (!properties.getMask().isEnabled()) {
            return beanProperties;
        }

        List<BeanPropertyWriter> writers = new ArrayList<>(beanProperties.size());

        for (BeanPropertyWriter writer : beanProperties) {
            Mask mask = writer.getAnnotation(Mask.class);

            if (mask == null) {
                mask = writer.getContextAnnotation(Mask.class);
            }

            if (mask != null) {
                writers.add(new MaskingBeanPropertyWriter(writer, maskService, mask));
            } else {
                writers.add(writer);
            }
        }

        return writers;
    }
}
