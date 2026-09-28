package pe.edu.galaxy.training.java.sensitive.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;

public class MaskingBeanPropertyWriter extends BeanPropertyWriter {

    private final MaskService maskService;
    private final Mask mask;

    public MaskingBeanPropertyWriter(BeanPropertyWriter base,
                                     MaskService maskService,
                                     Mask mask) {
        super(base);
        this.maskService = maskService;
        this.mask = mask;
    }

    @Override
    public void serializeAsField(Object bean,
                                 JsonGenerator gen,
                                 SerializerProvider prov) throws Exception {
        Object value = get(bean);

        if (value instanceof String text) {
            gen.writeStringField(getName(), maskService.mask(text, mask));
            return;
        }

        super.serializeAsField(bean, gen, prov);
    }
}
