<%-- 
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
    Document   : index
    Created on : 25-nov-2013, 17:57:59
    Sistema Sugerencias de palabras claves
    @author Lic. Natalia L. Mitzig
    @author Lic. Mónica S. Mitzig
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
        <%   // TODO initialize WS operation arguments here
        try {
            org.melliz.sugerenciasSubjects.WSSugerencias_Service service = new org.melliz.sugerenciasSubjects.WSSugerencias_Service();
            org.melliz.sugerenciasSubjects.WSSugerencias port = service.getWSSugerenciasPort();
            request.setCharacterEncoding("UTF-8");
            //System.out.println("MOSTRANDO REQUEST:" + request.getRequestURI());
            String textoSeleccionado = request.getParameter("text");
            //out.print(textoSeleccionado);
            String sugerencias = port.obtenerSugerencias(textoSeleccionado);
            out.print(sugerencias);
         } catch (Exception ex) {
            out.print("Error en index");
        }
    %>
    <%-- end web service invocation --%>