package padron.util;

import padron.dto.DireccionDTO;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;

public class TestSerializacion {

    public static void main(String[] args) {

        RespuestaPadron ok = RespuestaPadron.ok(
            new PersonaDTO("1-2345-6789", "JUAN GABRIEL", "SANDI", "LOPEZ", "104015"),
            new DireccionDTO("San José", "Montes de Oca", "San Pedro", "01-001")
        );

        System.out.println("=== JSON OK ===");
        System.out.println(Serializador.toJson(ok));
        System.out.println();

        System.out.println("=== XML OK ===");
        System.out.println(Serializador.toXml(ok));
        System.out.println();

        RespuestaPadron err = RespuestaPadron.error("NO_ENCONTRADA", "Cédula no encontrada");

        System.out.println("=== JSON ERROR ===");
        System.out.println(Serializador.toJson(err));
        System.out.println();

        System.out.println("=== XML ERROR ===");
        System.out.println(Serializador.toXml(err));
    }
}