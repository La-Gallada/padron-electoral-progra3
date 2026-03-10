/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectoprogra3;

import java.io.BufferedReader;
import java.io.FileReader;

public class RepositorioPadronTxt {


    private static final String RUTAORIGEN =
    "C:\\Users\\wdtai\\Downloads\\padron_completo\\PADRON_COMPLETO.txt";


    // BUSCAR POR CEDULA
    public String buscarPorCedula(String cedula) {

        try (BufferedReader br = new BufferedReader(new FileReader(RUTAORIGEN))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(",");

                if (datos[0].equals(cedula)) {

                    String nombre = datos[5].trim() + " "
                            + datos[6].trim() + " "
                            + datos[7].trim();

                    String codElec = datos[1].trim();

                    return nombre + "," + codElec;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


    // BUSCAR POR NOMBRE
    public void buscarPorNombre(String nombreBuscar,
            RepositorioDistelecTxt repoDir,
            javax.swing.JTextArea txtArea) {

        txtArea.setText("Buscando...\n");

        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(RUTAORIGEN))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(",");

                String nombreCompleto = datos[5].trim() + " "
                        + datos[6].trim() + " "
                        + datos[7].trim();

                if (nombreCompleto.toLowerCase().contains(nombreBuscar.toLowerCase())) {

                    String cedula = datos[0].trim();
                    String codElec = datos[1].trim();

                    Direccion dir = repoDir.obtenerDireccion(codElec);

                    txtArea.append("Nombre: " + nombreCompleto + "\n");
                    txtArea.append("Cedula: " + cedula + "\n");
                    txtArea.append("Direccion: " + dir + "\n");
                    txtArea.append("----------------------\n");

                    encontrado = true;
                }
            }

            if (!encontrado) {
                txtArea.append("Resultado no encontrado\n");
            }

        } catch (Exception e) {
            txtArea.append("Error: " + e.getMessage());
        }
    }


    // BUSCAR POR NOMBRE Y LUGAR
    public void buscarPorNombreYLugar(String nombreBuscar,
            String lugarBuscar,
            RepositorioDistelecTxt repoDir,
            javax.swing.JTextArea txtArea) {

        txtArea.setText("Buscando...\n");

        boolean encontrado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(RUTAORIGEN))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(",");

                String nombreCompleto = datos[5].trim() + " "
                        + datos[6].trim() + " "
                        + datos[7].trim();

                String codElec = datos[1].trim();

                Direccion dir = repoDir.obtenerDireccion(codElec);

                if (dir != null &&
                        nombreCompleto.toLowerCase().contains(nombreBuscar.toLowerCase()) &&
                        dir.toString().toLowerCase().contains(lugarBuscar.toLowerCase())) {

                    txtArea.append("Nombre: " + nombreCompleto + "\n");
                    txtArea.append("Cedula: " + datos[0] + "\n");
                    txtArea.append("Direccion: " + dir + "\n");
                    txtArea.append("----------------------\n");

                    encontrado = true;
                }
            }

            if (!encontrado) {
                txtArea.append("Resultado no encontrado\n");
            }

        } catch (Exception e) {
            txtArea.append("Error: " + e.getMessage());
        }
    }

}
