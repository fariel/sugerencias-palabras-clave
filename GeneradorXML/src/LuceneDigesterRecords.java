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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.digester.Digester;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

/**
 * Sistema Sugerencias de palabras clave
 * @author Lic. Natalia L. Mitzig
 * @author Lic. Mónica S. Mitzig
 */
public class LuceneDigesterRecords {
    /**
     * variable usada para crear y mantener el índice
     */
    private static IndexWriter writer;
     /**
     * variable utilizada para realizar la búsqueda en el índice
     */
    private static IndexSearcher isearch;
     /**
     * variable usada para tomar un documento y convertirlo en tokens que pueden ser indexados
     */
    private static Analyzer analyzer;
    /**
     * variable que se utiliza se utiliza para construir un analizador sintáctico que
     * puede buscar a través de un índice
     */
    private static QueryParser parser;
    /**
     * variable para llevar la cantidad de recursos indexados
     */   
    public static int cantRecordsIndexados = 0;
    /**
     * variable para llevar la cantidad de recursos considerados
     */    
    public static int cantRecords = 0;
    /**
     * variable que almacena por cada sitio URL, la cantidad de recursos indexados para el mismo
     */   
    public static HashMap<String, Integer> recursosIndexados = new HashMap<String, Integer>();
    /** 
     * Procedimiento que agrega un Record al índice
     * @param record <code>record</code> para agregarlo al índice
     * @exception IOException excepción que ocurre al agregar un record al índice
     */
    public void addRecord(Record record) throws IOException
    {
        try{
            Document recordDocument  = new Document();
            
            ArrayList titles = record.getTitles();
            for (int i=0; i < titles.size(); i++){
                String title = (String)titles.get(i);
                recordDocument.add(new Field("dc:title", title, Store.YES, Index.ANALYZED));
            }
                        
            ArrayList subjects = record.getSubjects();
            for(int i=0; i< subjects.size(); i++){
                String subject = (String) subjects.get(i);
                recordDocument.add(new Field("dc:subject", subject, Store.YES, Index.ANALYZED));
            }
            
            ArrayList descriptions = record.getDescriptions();
            for(int i=0; i< descriptions.size(); i++){
                String description = (String) descriptions.get(i);
                recordDocument.add(new Field("dc:description", description, Store.YES, Index.ANALYZED));
            }          
            
            recordDocument.add(new Field("dc:language", record.getLanguage(), Store.YES, Index.ANALYZED));
            
            recordDocument.add(new Field("dc:identifier", record.getIdentifier(), Store.YES, Index.ANALYZED));
            
            System.out.print("Agregando al índice el record: ");
            for (int i=0; i< titles.size(); i++){                
                System.out.println((String)titles.get(i) + " ");
            };
            
            writer.addDocument(recordDocument);
            cantRecordsIndexados = cantRecordsIndexados + 1;
            
        }catch(Exception e){
            System.out.println("Error al agregar el record, " + e.getMessage());
        }
    }
    
    /*
     * Procedimiento que crea un índice para agregar artículos, configura las reglas
     * Digester y acciones, parsea los archivos XML especificados en la carpeta entrenamientoXML.
     */
    public static void Indexar()
    {
        try {
            File stopWords = new File("stopwords.txt");
            StandardAnalyzer analyzer = new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_30, stopWords);
            boolean createFlag = true;
            File indexPath = new File ("indiceRecords");

            //IndexWriter to use for adding records to the index
            writer = new IndexWriter(FSDirectory.open(indexPath), analyzer, createFlag, IndexWriter.MaxFieldLength.LIMITED);    
            
            File dirOrigen = new File("entrenamiento");
            String[] records = dirOrigen.list();
            cantRecords = records.length;
            System.out.println("Vamos a indexar todos los records considerados: " + records.length);
            
            GeneradorXML gen = new GeneradorXML();
            HashMap recursos = gen.getRecursosConsiderados();
            for (String record : records){
                String recordXML = "entrenamiento/" + record;
                File dir = new File(recordXML);
                if (dir.exists())
                {
                    parsingXML(recordXML);
                    if (recursos.containsKey(record)){
                        String url = (String)recursos.get(record);
                        if(!recursosIndexados.containsKey(url)){   
                            recursosIndexados.put(url, 1);
                        }else{
                            Integer cant = recursosIndexados.get(url);
                            cant = cant + 1;
                            recursosIndexados.put(url, cant);
                        }
                    }
                }
            }
            //optimize and close the index
            writer.optimize();
            writer.close(); 
            analyzer.close();
            System.out.println("Fin de la indexación de records.");
            System.out.println("Total de recursos indexados: " + cantRecordsIndexados);
            mostrarCantidadRegistrosPorSitios();
        }catch (IOException ex){
            System.out.println("Error al crear el indice de records " + ex.getMessage());
        }
    }
    /**
     * Procedimiento que se encarga de mostrar la cantidad de recursos que se indexaron 
     * por sitios y guarda dicha información en un archivo de logs
     */
    public static void mostrarCantidadRegistrosPorSitios(){
        Set set = recursosIndexados.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            System.out.println("Para el sitio " + me.getKey() + " se indexaron " + me.getValue() + " recursos.");
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
            guardarEnArchivo("LogEstadisticas.txt", "Para el sitio " + me.getKey() + " se indexaron " + me.getValue() + " recursos.\n");
        }     
    }
     /**
     * Procedimiento que dado un nombre de archivo y un mensaje, almacena el mensaje en el archivo 
     * @param archivo contiene el nombre del archivo en el que se guardará la información deseada
     * @param mensaje contiene la información a guardar en el archivo
     */
    private static void guardarEnArchivo(String Archivo,String mensaje){
        try{
            String sFichero = Archivo;
            File fichero = new File(sFichero);
            BufferedWriter bw = new BufferedWriter(new FileWriter(sFichero, true));
            if(Archivo.compareTo("LogEstadisticas.txt")==0){
                bw.write(mensaje);
            }
            bw.close();
        }catch(IOException ioe){
              ioe.printStackTrace();
        }
    }
    /*
     * Procedimiento que dado un nombre de archivo XML, parsea el mismo usando Digester
     * mapeo de archivos xml a objetos
     */
    public static void parsingXML(String recordXML){
        try {
            //instantiate Digester and disable XML validation
            Digester digester = new Digester();
            digester.setValidating(false);

            //instantiate LuceneDigesterRecords class
            digester.addObjectCreate("oai-dc:dc", LuceneDigesterRecords.class);
            //instantiate Record class
            digester.addObjectCreate("oai-dc:dc/dc:record", LuceneDigesterRecords.Record.class);

            //set different properties of Record instance using specified methods
            digester.addCallMethod("oai-dc:dc/dc:record/dc:title", "addTitle", 1);
            digester.addCallParam("oai-dc:dc/dc:record/dc:title", 0);
            
            digester.addCallMethod("oai-dc:dc/dc:record/dc:subject", "addSubject", 1);
            digester.addCallParam("oai-dc:dc/dc:record/dc:subject", 0);

            digester.addCallMethod("oai-dc:dc/dc:record/dc:description", "addDescription", 1);
            digester.addCallParam("oai-dc:dc/dc:record/dc:description", 0);
           
            digester.addCallMethod("oai-dc:dc/dc:record/dc:language", "setLanguage", 0);
            
            digester.addCallMethod("oai-dc:dc/dc:record/dc:identifier", "setIdentifier", 0);

            //call 'addArticulo' method when the next '/oai-dc:dc' pattern is seen
            digester.addSetNext("oai-dc:dc/dc:record", "addRecord");

            //now that rules and actions are configured, start the parsing process
            System.out.println("Parsing XML record: " + recordXML);
            LuceneDigesterRecords ld = (LuceneDigesterRecords) digester.parse(new File(recordXML));
            
         }catch (IOException ex){
            System.out.println("Error al crear digester " + ex.getMessage());
        }catch (SAXException ex){
            System.out.println("Error al parsear record xml " + ex.getMessage());
        }
    }
    
    public static String buscarSimilares(String textoSeleccionado){
    
//        try{
//            ArrayList lista = new ArrayList();
//            
//            File indexPath = new File ("indiceRecords");
//            IndexSearcher searcher = new IndexSearcher(FSDirectory.open(indexPath));
//            IndexReader ir =  IndexReader.open(FSDirectory.open(indexPath)); 
//            MoreLikeThis mlt = new MoreLikeThis(ir);
//            File stopW = new File("stopwords.txt");
//            StandardAnalyzer analyzer = new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_30, stopW);
//            
//            mlt.setAnalyzer(analyzer);
//            mlt.setMinTermFreq(2);
//            mlt.setMinDocFreq(1); 
//            
//            StopWords stopWords = new StopWords("stopwords.txt");
//            mlt.setStopWords(stopWords.getStopWords());
//            mlt.setMinWordLen(3);
//            mlt.setFieldNames(new String[] {"dc:title", "dc:subject" , "dc:description" , "dc:language" , "dc:identifier"});           
//            Reader reader = new StringReader(textoSeleccionado);            
//            Query query = mlt.like(reader);
//
//            ScoreDoc[] hits = searcher.search(query, null, 3).scoreDocs;
//            
//            Set subjects = new HashSet();
//
//            System.out.println("NUMBER OF MATCHING: " + hits.length);
//            System.out.println("Texto seleccionado: " + textoSeleccionado);
//            System.out.println("QUERY: " + query.toString());
//            for(ScoreDoc d: hits){
//                System.out.println("NAME: " +  d.toString());
//                Document doc = searcher.doc(d.doc);
//                String[] listTitles = doc.getValues("dc:title");
//                System.out.print("Títulos: " );
//                for (int i=0; i<listTitles.length; i++){
//                    System.out.println(listTitles[i]);
//                }
//                
//                System.out.println("Idioma: " + doc.get("dc:language"));
//                
//                String[] listsubjects = doc.getValues("dc:subject");
//                int i=0;                
//                while (i < listsubjects.length ){                   
//                    if (!subjects.contains(listsubjects[i])){
//                        subjects.add(listsubjects[i]);
//                        lista.add(listsubjects[i]);
//                        System.out.println("Subject: " + listsubjects[i]);
//                    }
//                    i++;
//                }
//            }
//            ir.close();
//            
//            return lista;
//            
//        }catch(Exception e){
//            return null;
//        }
        
         //TODO write your implementation code here:
        
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
            
            ScoreDoc[] hits = searcher.search(query, null, 1).scoreDocs;
            
            //Set subjects = new HashSet();

//            System.out.println("NUMBER OF MATCHING: " + hits.length);
//            System.out.println("Texto seleccionado: " + textoSeleccionado);
//            System.out.println("QUERY: " + query.toString());
            int j= 1;
            for(ScoreDoc d: hits){
                //System.out.println("NAME: " +  d.toString());
                Document doc = searcher.doc(d.doc);
//                String[] listTitles = doc.getValues("dc:title");
//                System.out.print("Títulos: " );
//                for (int i=0; i<listTitles.length; i++){
//                    System.out.println(listTitles[i]);
//                }
                if (j!=1){
                    sugerencias = sugerencias + " ";
                }
                //System.out.println("Idioma: " + doc.get("dc:language"));
                String[] listsubjects = doc.getValues("dc:subject");
                int i=0;                
                while (i < listsubjects.length ){                   
//                    if (!subjects.contains(listsubjects[i])){
//                        subjects.add(listsubjects[i]);
                        sugerencias = sugerencias + listsubjects[i]+ " ";
                        //System.out.println("Subject: " + listsubjects[i]);
//                    }
                    i++;
                }
                sugerencias = sugerencias + " ";
                sugerencias =  sugerencias + doc.get("dc:identifier");
                j++;
            }
            ir.close();
            //System.out.println(sugerencias);
            //guardarEnAchivo(sugerencias);
            return sugerencias;
            
        }catch(Exception e){
            String msg = e.getMessage();
            if (msg==null) msg ="";
            return "No se ha seleccionado texto o se ha seleccionado texto con símbolos extraños: " + msg;
        }
    
    }
    
    /**
     * Sistema Sugerencias de palabras claves
     * @author Lic. Natalia L. Mitzig
     * @author Lic. Mónica S. Mitzig
     * 
     * Clase JavaBean que mantiene las propiedades de cada recurso
     * Es importante que la clase sea pública y estática, 
     * para permitir que Digester pueda crear instancias de la misma
     */
    public static class Record {
        /**
         * variable privada para almacenar la lista de títulos del recurso
         */
        private ArrayList title = new ArrayList();
        /**
         * variable privada para almacenar la lista de palabras clave del recurso
         */
        private ArrayList subjects = new ArrayList();
        /**
         * variable privada para almacenar la lista de resumenes del recurso
         */        
        private ArrayList descriptions = new ArrayList();
        /**
         * variable privada para almacenar el idioma del recurso
         */
        private String language = "";
        /**
         * variable privada para almacenar el url del recurso
         */
        private String identifier = ""; 

        /**
         * Procedimiento que retorna la lista de títulos del recurso
         * @return lista de títulos
         */
        public ArrayList getTitles() {
            return title;
        }
        /**
         * Procedimiento que asigna una lista de títulos dada al recurso
         * @param title lista de títulos
         */
        public void setTitles(ArrayList title) {
            this.title = title;
        }
        /**
         * Procedimiento que agrega un título a la lista de títulos del recurso
         * @param title título de un recurso
         */
        public void addTitle(String title) {
            this.title.add(title);
        }
        /**
         * Procedimiento que retorna la lista de palabras clave del recurso
         * @return lista de palabras clave
         */
        public ArrayList getSubjects() {
            return subjects;
        }
        /**
         * Procedimiento que asigna una lista de palabras clave dada al recurso
         * @param subjects lista de palabras clave
         */
        public void setSubjects(ArrayList subjects){
            this.subjects = subjects;
        }
        /**
         * Procedimiento que agrega una palabra clave dada, a la lista de palabras clave del recurso
         * @param subject palabra clave
         */
        public void addSubject(String subject) {
            this.subjects.add(subject);
        }
        /**
         * Procedimiento que retorna la lista de resumenes del recurso
         * @return lista de resumenes
         */         
        public ArrayList getDescriptions() {
            return descriptions;
        }
        /**
         * Procedimiento que asigna la lista de resumenes dada al recurso
         * @param descriptions lista de resumenes
         */
        public void setDescriptions(ArrayList descriptions) {
            this.descriptions = descriptions;
        }
        /**
         * Procedimiento que agrega un resumen dado al recurso
         * @param description resumen 
         */
        public void addDescription(String description){
            this.descriptions.add(description);
        }
        /**
         * Procedimiento que retorna el idioma del recurso
         * @return idioma
         */
        public String getLanguage() {
            return language;
        }
        /**
         * Procedimiento que asigna un idioma dado al recurso
         * @param language idioma
         */
        public void setLanguage(String language) {
            this.language = language;
        }
        /**
         * Procedimiento que retorna el url del recurso
         * @return url
         */
        public String getIdentifier() {
            return identifier;
        }
        /**
         * Procedimiento que asinga el url dado al recurso
         * @param identifier url
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
       
    }
    
}
