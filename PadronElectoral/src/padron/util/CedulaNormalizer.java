package padron.util;

public class CedulaNormalizer {

    private CedulaNormalizer() {}

    /**
     * Recibe una cédula en cualquier formato (con guiones, espacios, ceros
     * de más o de menos) y devuelve el formato normalizado de 9 dígitos: 
     * X-XXXX-XXXX  →  "109990001" (sin guiones, con ceros a la izquierda).
     *
     * Retorna null si la entrada es inválida.
     */
    public static String normalizar(String input) {
        if (input == null) return null;

        // Quitar todo lo que no sea dígito
        String soloDigitos = input.replaceAll("[^0-9]", "");

        // La cédula costarricense tiene entre 8 y 9 dígitos reales
        if (soloDigitos.length() < 8 || soloDigitos.length() > 9) return null;

        // Rellenar con cero a la izquierda si tiene 8 dígitos
        return String.format("%09d", Long.parseLong(soloDigitos));
    }

    /**
     * Valida que la cédula normalizada tenga exactamente 9 dígitos
     * y que el primer dígito (provincia) esté entre 1 y 9.
     */
    public static boolean esValida(String cedulaNormalizada) {
        if (cedulaNormalizada == null || cedulaNormalizada.length() != 9) return false;
        if (!cedulaNormalizada.matches("[0-9]{9}")) return false;

        int provincia = Character.getNumericValue(cedulaNormalizada.charAt(0));
        return provincia >= 1 && provincia <= 9;
    }
}