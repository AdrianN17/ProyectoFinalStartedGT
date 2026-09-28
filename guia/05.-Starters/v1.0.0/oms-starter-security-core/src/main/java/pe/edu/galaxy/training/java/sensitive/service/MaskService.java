package pe.edu.galaxy.training.java.sensitive.service;

import pe.edu.galaxy.training.java.sensitive.annotation.Mask;

public interface MaskService {

    String mask(String value, Mask mask);
}
