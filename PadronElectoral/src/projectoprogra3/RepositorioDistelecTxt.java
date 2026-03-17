/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectoprogra3;

/**
 *
 * @author wdtai
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class RepositorioDistelecTxt {

    private Map<String, Direccion> mapa = new HashMap<>();

    private static final String RUTA =
    "C:\\Users\\wdtai\\Downloads\\padron_completo\\distelec.txt";

    public void cargar() {

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(",");

                String codElec = datos[0];
                String provincia = datos[1];
                String canton = datos[2];
                String distrito = datos[3];
                

                mapa.put(codElec,
                        new Direccion(provincia, canton, distrito));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Direccion obtenerDireccion(String codElec) {
        return mapa.get(codElec);
    }
    
   
}