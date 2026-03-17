/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectoprogra3;

/**
 *
 * @author wdtai
 */
public class Direccion {

    String provincia;
    String canton;
    String distrito;

    public Direccion(String provincia, String canton, String distrito) {
        this.provincia = provincia;
        this.canton = canton;
        this.distrito = distrito;
    }

    public String toString() {
        return provincia + ", " + canton + ", " + distrito;
    }
}