/*
 *  Sistema Sugerencias de palabras clave: retorna las tres mejores 
 *	sugerencias de palabras clave, según un texto dado. Utiliza busqueda 
 *	por similitud consultando un índice de recursos.
 *  Copyright (C) 2013  Lic Natalia Mitzig, Lic Mónica Mitzig, 
 *	Lic Fernando Martinez, Lic Ricardo Piriz.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package generadorxml;

import java.util.ArrayList;

/**
 * Sistema Sugerencias de palabras clave
 * @author Lic. Natalia L. Mitzig
 * @author Lic. Mónica S. Mitzig
 */
public class MainGeneradorXML {

    /**
     * Programa principal:
     * Por cada recurso cosechado se crea un archivo XML que se indexará 
     * para crear el índice de consulta por búsqueda por similitud de palabras clae
     * @param args the command line arguments
     * @exception Excepcion que ocurre al generar los archivos XML que se indexan
     */
    public static void main(String[] args) {
        try{
            GeneradorXML generador = new GeneradorXML();
            generador.ProcesarRegistrosDublinCore();
            LuceneDigesterRecords.Indexar();
//            String mejoresTematicas = LuceneDigesterRecords.buscarSimilares("A partir de los nuevos hallazgos realizados en la región pedemontana de la provincia de Jujuy en los últimos 15 años, se discuten las particulares condiciones de inhumación y las prácticas mortuorias asociadas con los grupos adscriptos a la llamada Tradición San Francisco (800 a.C.- 400 d.C). Aunque aún escasas y parciales, estas nuevas evidencias nos permiten discutir algunas de las modalidades en relación a las prácticas mortuorias llevadas a cabo por estas poblaciones tradicionalmente consideradas como agroalfareras y que hoy están siendo pensadas como sociedades cazadoras, recolectoras y pescadoras.");
//            System.out.println(mejoresTematicas);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
