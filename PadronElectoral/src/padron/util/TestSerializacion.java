package padron.util;

import padron.dto.*;

public class TestSerializacion {
    public static void main(String[] args) {

        RespuestaPadron ok = RespuestaPadron.ok(
            new PersonaDTO("1-2345-6789", "JUAN GABRIEL SANDI LOPEZ"),
            new DireccionDTO("San José", "Montes de Oca", "San Pedro", "01-001")
        );

        System.out.println(Serializador.toJson(ok));
        System.out.println(Serializador.toXml(ok));

        RespuestaPadron err = RespuestaPadron.error("NO_ENCONTRADA", "Cédula no encontrada");
        System.out.println(Serializador.toJson(err));
        System.out.println(Serializador.toXml(err));
    }
}