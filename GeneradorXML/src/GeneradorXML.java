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
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Sistema Sugerencias de palabras clave
 * @author Lic. Natalia L. Mitzig
 * @author Lic. Mónica S. Mitzig
 */
public class GeneradorXML {
    /**
     * Variable de conexion a la base de datos donde se encuentran los recursos dublin core.
    */
    Connection conexionBD = null;
    /**
     * Variable que contiene el nombre de la carpeta donde se va a almacenar el 
     * entrenamiento.
     */
    String pathDirEntrenamientoXML;
    /**
     * Estructura de datos para almacenar por recursos el url del sitio de donde
     * se ha obtenido el mismo.
     */
    public static HashMap<String, String> recursosConsideradosPorSitos = new HashMap<String, String>();
    /**
     * Estructura de datos para almacenar por sitios la cantidad de recursos 
     * descartados que no cumplen las condiciones basicas requeridas.
     */
    public static HashMap<String, Integer> recursosDescartadosPorSitios = new HashMap<String, Integer>();
    /**
     * Estructura de datos para almacenar por sitios la cantidad recursos 
     * considerados que cumplen las condiciones basicas requeridas.
     */
    public static HashMap<String, Integer> cantidadRecursosConsideradosPorSitios = new HashMap<String, Integer>();
    
    /**
     * Constructor de clase
     */
    public GeneradorXML()
    {
    }
    /**
     * Procedimiento que retorna la estructura de datos recursosDescartadosPorSitios
     * @return HashMap<String, Integer> recursosDescartadosPorSitios
    */
    public HashMap<String, Integer> getRecursosDescartados(){
        return recursosDescartadosPorSitios;
    }
     /**
     * Procedimiento que retorna la estructura de datos recursosConsideradosPorSitos
     * @return HashMap<String, String> recursosConsideradosPorSitos
    */
    public HashMap<String, String> getRecursosConsiderados(){
        return recursosConsideradosPorSitos;
    }
     /**
     * Procedimiento que retorna la estructura de datos recursosConsideradosPorSitos
     * @return HashMap<String, Integer> cantidadRecursosConsideradosPorSitios
    */
    public HashMap<String, Integer> getCantidadRecursosConsideradosPorSitios(){
        return cantidadRecursosConsideradosPorSitios;
    }
    /**
     * Procedimiento que realiza retorna la conexion a la base de datos ohsdb
     * @return Connection conexionBD
     */
    private Connection conexionBD()
    {   
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexionBD = DriverManager.getConnection("jdbc:mysql://localhost:3306/ohsdb?" + "user=root&password=");
            System.out.println("Conexion a base de datos abierta.");
            return conexionBD;
        } catch (Exception ex) {
            System.out.println("Error al conectarse a la base de datos: " + ex.getMessage());
            return conexionBD;
        }
    }
    /**
     * Procedimiento que se encarga de cerrar la conexion a la base de datos ohsdb
     */
    private void cerrarConexionBD()
    {
        try { 
            if (conexionBD != null) 
                conexionBD.close(); 
            System.out.println("Conexion a base de datos cerrada.");
        } catch (Exception ex) {}
    }
    /**
     * Procedimiento que retorna los registros en formato dublin core
     */
    public ResultSet getRegistrosDublinCore()
    {
        Statement stmt = null;
        ResultSet rs = null;
        try 
        {
            stmt = conexionBD.createStatement();
            //rs = stmt.executeQuery("SELECT record_id, contents FROM records;");
            rs = stmt.executeQuery("SELECT r.record_id, r.contents, a.url FROM records r INNER JOIN archives a ON r.archive_id=a.archive_id;");
        }catch(Exception ex){
            System.out.println("Error al obtener los registros dublin core: " + ex.getMessage());
        }
        return rs;
    }
    /**
     * Procedimiento que procesa los registros dublin core, generando para aquellos
     * registros que cumplen las condiciones basicas el archivo xml correspondiente
     */
    public void ProcesarRegistrosDublinCore()
    {
        try{
            conexionBD = conexionBD();
            ResultSet rs = getRegistrosDublinCore();
            while (rs.next())
            {
                String record_id = rs.getString("record_id");
                String content = rs.getString("contents");
                String url = rs.getString("url");
                String xml = decodificarCadena(content);   
                //controlamos si tienen las etiquetas basicas que necesitamos: 
                //<dc: title>, <dc:subject>, <dc:description>, <dc:identifier>, <dc:language>
                if (contieneTagBasicos(content) && (esIdiomaEspaniol(content))){
                    generarArchivoXML(record_id, xml);
                    if(!recursosConsideradosPorSitos.containsKey("record_" + record_id + ".xml")){
                        recursosConsideradosPorSitos.put("record_" + record_id + ".xml", url);
                        if(!cantidadRecursosConsideradosPorSitios.containsKey(url)){
                            cantidadRecursosConsideradosPorSitios.put(url, 1);
                        }else{
                            Integer cant = cantidadRecursosConsideradosPorSitios.get(url);
                            cant = cant + 1;
                            cantidadRecursosConsideradosPorSitios.put(url, cant);
                        }
                    }
                }else{
                    System.out.println("El record " + record_id + " se descarta dado que el xml " + xml + " no contiene los tag basicos necesarios");
                    if(!recursosDescartadosPorSitios.containsKey(url)){
                        recursosDescartadosPorSitios.put(url, 1);
                    }else{
                        Integer cant = recursosDescartadosPorSitios.get(url);
                        cant = cant + 1;
                        recursosDescartadosPorSitios.put(url, cant);
                    }
                }
            }
            cerrarConexionBD();
            mostrarRecursosDescartados();
            mostrarRecursosConsiderados();
        }catch(Exception ex){
            System.out.println("Error al procesar registros en dublin core: " + ex.getMessage());
        }
    }
    /**
     * Procedimiento que se encarga de mostrar la información referida a los recursos que fueron 
     * descartados por no verificar las condiciones basicas requeridas, guardando la información en un archivo de logs
     */
    private void mostrarRecursosDescartados(){
        Set set = recursosDescartadosPorSitios.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            System.out.println("Para el sitio " + me.getKey() + " se descartaron " + me.getValue() + " recursos.");
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
            guardarEnArchivo("LogEstadisticas.txt", "Para el sitio " + me.getKey() + " se descartaron " + me.getValue() + " recursos.\n");
        }  
    }
    /**
     * Procedimiento que se encarga de mostrar la información referida a los recursos considerados
     * que verifican las condiciones basicas requeridas, guardando la información en un archivo de logs
     */
    private void mostrarRecursosConsiderados(){
        Set set = cantidadRecursosConsideradosPorSitios.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            System.out.println("Para el sitio " + me.getKey() + " se consideran " + me.getValue() + " recursos posibles para indexar.");
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
            guardarEnArchivo("LogEstadisticas.txt", "Para el sitio " + me.getKey() + " se consideran " + me.getValue() + " recursos posibles para indexar.\n");
        }  
    }
    /**
     * Procedimiento que dado un nombre de archivo y un mensaje, almacena el mensaje en el archivo 
     * @param archivo contiene el nombre del archivo en el que se guardará la información deseada
     * @param mensaje contiene la información a guardar en el archivo
     */
    private void guardarEnArchivo(String archivo, String mensaje){
        try{
            String sFichero = archivo;
            File fichero = new File(sFichero);
            BufferedWriter bw = new BufferedWriter(new FileWriter(sFichero, true));
            if(archivo.compareTo("LogEstadisticas.txt")==0){
                bw.write(mensaje);
            }
            bw.close();
        }catch(IOException ioe){
              ioe.printStackTrace();
        }
    }
    /**
     * Procedimiento que dada una cadena que contiene el xml del registro dublin core, verifica si
     * contiene los tags básicos necesarios que son: subject, description, language e identifier
     * @param xml variable string que contiene los datos del xml del registro dublin core en cuestión
     * @return true si el xml contiene los tags básicos necesarios y false en caso contrario
     */
    private boolean contieneTagBasicos(String xml)
    {
        return xml.contains("<dc:subject>") && xml.contains("<dc:description>") && xml.contains("<dc:language>") && xml.contains("<dc:identifier>");
    }
    /**
     * Procedimiento que dada una cadena que contiene el xml del registro dublin core, verifica si
     * contiene el tag basico idioma donde el mismo sea solamente espaniol
     * @param xml variable que contiene el xml del registro dublin core
     * @return true si la cadena dada, contiene el tag básico que indica que el idioma es espaniol
     * y false en caso contrario
     */
    private boolean esIdiomaEspaniol(String xml)
    {
        return (xml.contains("<dc:language>es</dc:language>") || (xml.contains("<dc:language>spa</dc:language>")));
    }
    /**
     * Procedimiento que dada una cadena en codificación en UTF-8, retorna su equivalente 
     * en codificación ISO-8859-1
     * @param xml cadena en codificación UTF-8
     * @return cadena en codificación ISO-8859-1
     */
    private String decodificarCadena(String xml)
    {
        // Vemos si el formato entrante es ASCII o UTF8
        CharsetEncoder isoEncoder = Charset.forName("ISO-8859-1").newEncoder();
        CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
        //Boolean isISO = isoEncoder.canEncode(xml);
        //Boolean isUTF8 =  utf8Encoder.canEncode(xml);
        // Convertir de UTF-8 a ISO-8859-1
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");
        // Decode UTF-8
        ByteBuffer bb = ByteBuffer.wrap(xml.getBytes());
        CharBuffer data = utf8charset.decode(bb);
        // Encode ISO-8559-1
        ByteBuffer outputBuffer = iso88591charset.encode(data);
        byte[] outputData = outputBuffer.array();

        return new String(outputData);
    }
    /**
     * Procedimiento que dada una cadena que contiene un XML, retorna un documento 
     * con el contenido XML correspondiente
     * @param xml cadena que contiene un XML
     * @return documento XML
     */
    public Document StringToDocumentXML(String xml)
    {
        try{    
            String[] cad = xml.split(">");
            String p = "><dc:record>";
            String np = "</dc:record>";
            String k = cad[0].concat(p);
            for(int i=1; i<cad.length - 1; i++)
            {
                k= k + cad[i] + ">";
            }
            k = k + np + cad[cad.length - 1]+ ">";
            org.jdom.input.SAXBuilder saxBuilder = new SAXBuilder();  
            org.jdom.Document doc = saxBuilder.build(new StringReader(k));
            return doc;
        }catch(Exception ex){
            System.out.println("Error al generar documento desde el string dado: " + ex.getMessage());
            return null;
        }
    }
    /**
     * Procedimiento que crea la carpeta "entrenamiento", en caso de no existir, 
     * para almacenar en la misma todos los documentos XML generados
     */    
    public void crearCarpetaEntrenamiento()
    {
        File dir = new File("entrenamiento");
        if (dir.exists())
        {
            pathDirEntrenamientoXML = dir.getPath();
        }else{
            if(dir.mkdir()){
                pathDirEntrenamientoXML = dir.getPath();
            }
        }
    }
    /**
     * Procedimiento que dado el nombre del recurso, el contenido y el url de donde fue obtenido el mismo,
     * genera un archivo XML cuyo nombre es record_id, cuyo contenido es el content dado
     * @param record_id nombre del recurso considerado
     * @param content contenido del recurso considerado   
     */
    public void generarArchivoXML(String record_id, String content)
    {
        try{
            crearCarpetaEntrenamiento();
            Document doc = StringToDocumentXML(content);
            FileWriter archivoSalida = new FileWriter(pathDirEntrenamientoXML + "/record_" + record_id + ".xml");
            XMLOutputter outPutter = new XMLOutputter();
            outPutter.setFormat(Format.getPrettyFormat());
            outPutter.output(doc, archivoSalida);
            archivoSalida.close();
            System.out.println("Generamos el archivo: " + "record_" + record_id + ".xml"); 
        }catch(Exception e){
           e.printStackTrace();
           System.out.println("Error en generar: " + e.getMessage());
        }
    }

}
