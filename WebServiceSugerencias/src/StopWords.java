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
package org.melliz.sugerencias;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Sistema Sugerencias de palabras claves
 * @author Lic. Natalia L. Mitzig
 * @author Lic. Mónica S. Mitzig
 */
public class StopWords {
    
     /**
     * variable que contiene el conjunto de palabras stop words
     */
    private Set words = new HashSet();
     /**
     * Procedimiento que se encarga de generar el conjunto de stop words
     * tomadas de un archivo que las contiene
     * @param archivo nombre del archivo que contiene los stop words
     */
    public StopWords(String archivo)
    {
        try{
            File file = new File(archivo);
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
            String word = "";
            while ((word = bf.readLine())!=null) {
                if (!words.contains(word)){
                    words.add(word);
                }
            } 
        }catch(FileNotFoundException e){
            System.out.println("File not found " + e.getMessage());
        }    
        catch(IOException io){
            System.out.println("Exception IO " + io.getMessage());
        }
    }
    /**
     * Procedimiento que retorna un conjunto de stop words
     */
    public Set getStopWords()
    {
        return words;
    }
    /**
     * Procedimiento que retorna true si la palabra dadda es un stop word y false en caso contrario
     * @return boolean
     */
    public boolean esStopWords(String pal)
    {
        return this.words.contains(pal);
    }
    
}
