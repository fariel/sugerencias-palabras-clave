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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.FSDirectory;
/**
 * Sistema Sugerencias de palabras claves
 * @author Lic. Natalia L. Mitzig
 * @author Lic. Mónica S. Mitzig
 */
@WebService(serviceName = "WSSugerencias")
@Stateless()
public class WSSugerencias {

    /**
     * Web service obtenerSugerencias
     * Procedimiento que dado un texto retorna las tres mejores sugerencias de palabras 
     * clave, en orden de relevancia, realizando búsqueda por similitud en el índice de recursos  
     */
    @WebMethod(operationName = "obtenerSugerencias")
    public String obtenerSugerencias(@WebParam(name = "textoSeleccionado") String textoSeleccionado) {
         try{
            String sugerencias = "";
            File indexPath = new File ("indiceRecords");
            IndexSearcher searcher = new IndexSearcher(FSDirectory.open(indexPath));
            IndexReader ir =  IndexReader.open(FSDirectory.open(indexPath)); 
            MoreLikeThis mlt = new MoreLikeThis(ir);
            File stopW = new File("stopwords.txt"); 
            StandardAnalyzer analyzer = new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_30, stopW);
            
            mlt.setAnalyzer(analyzer);
            //mlt.setMinTermFreq(2);
            //mlt.setMinDocFreq(1); 
            
            StopWords stopWords = new StopWords("stopwords.txt");
            mlt.setStopWords(stopWords.getStopWords());
            mlt.setMinWordLen(3);
            mlt.setFieldNames(new String[] {"dc:title", "dc:subject" , "dc:description" , "dc:language" , "dc:identifier"});    
            Reader reader = new StringReader(textoSeleccionado);
            Query query = mlt.like(reader); 
            
            ScoreDoc[] hits = searcher.search(query, null, 3).scoreDocs;
            
            if (hits.length == 0){
                return "No se ha encontrado material similar al texto seleccionado (puede que el texto seleccionado sea insuficiente)";
            }
              
            int j= 1;
            for(ScoreDoc d: hits){
                Document doc = searcher.doc(d.doc);
                if (j!=1){
                    sugerencias = sugerencias + "@";
                }
                String[] listsubjects = doc.getValues("dc:subject");
                  
                int i=0;
                
                while (i < listsubjects.length ){                      
                    if (listsubjects[i].length() > 0) {
                        String ultimo = "";
                        if (sugerencias.length() > 0){
                            ultimo = sugerencias.substring(sugerencias.length()-1, sugerencias.length());
                        }
                                                 
                        String sug = listsubjects[i];                    
                        String mayuscula=sug.charAt(0) + "";
                        mayuscula=mayuscula.toUpperCase();
                        sug=sug.replaceFirst(sug.charAt(0)+"", mayuscula);
                        //Pasamos a myúscula la primer letra de la palabra clave
                          
                        if ((i > 0) && (!ultimo.equals("@"))) {
                            sugerencias = sugerencias +"; " + sug;
                        } else {
                            sugerencias = sugerencias + sug;
                        }
                     }
                     i++;
                      
                }
                  
                sugerencias = sugerencias + "::";
                sugerencias =  sugerencias + doc.get("dc:identifier");
                j++;
            }
            ir.close();
            return sugerencias; 
            
        }catch(Exception e){
            String msg = e.getMessage();
            if (msg==null) msg ="";
            return "No se ha seleccionado texto o se ha seleccionado texto con símbolos extraños: " + msg;
        }
    }
}
