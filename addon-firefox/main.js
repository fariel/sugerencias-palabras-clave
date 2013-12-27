//--------------------------------------------------------------------------------------------------------------
//Addons: Sugerencias de Palabras clave
//Authors: Licenciada Mónica S. Mitzig
//         Licenciada Natalia L. Mitzig       
//--------------------------------------------------------------------------------------------------------------
  
//Import the APIs we need.
var contextMenu = require("context-menu");
var Request = require("request").Request;
var panel = require("panel");
var data = require("sdk/self").data;
var tabs = require("sdk/tabs");
  
exports.main = function(options, callbacks) {
  console.log(options.loadReason);
      
  // Create a new context menu item.
  var menuItem = contextMenu.Item({
    label: "Sugerir Palabras Clave",
    // Show this item when a selection exists.
    context: contextMenu.SelectionContext(),
    // When this item is clicked, post a message back with the selection
    contentScript: 'self.on("click", function () {' +
                   'var userSelection, ta; ' +
                   'if (window.getSelection && document.activeElement){' +
                       'if (document.activeElement.nodeName == "TEXTAREA"){' +
                           'ta = document.activeElement;' +
                           'userSelection = ta.value.substring(ta.selectionStart, ta.selectionEnd);' +
                        '} else {'+
                            'userSelection = window.getSelection();' +
                        '}'+
                   '}'+
                   'var text = userSelection.toString();' + 
                   'self.postMessage(text);' +
                   '});',
    // When we receive a message, look up the item
    onMessage: function (item) {
      //console.log('looking up "' + item + '"');
      lookup(item);
    }
  });
};
  
//call web service with method POST
function lookup(item) {
  
  Request({
      url: "http://localhost:8080/WebServiceSugerencias/",
      content: {text: item},
      onComplete: function (response) {
        //console.log(response.text);
        showSuggestions(response.text);
      }
  }).post();
}
  
//show suggestions on panel
function showSuggestions(texto){
  
  var sugerencias = texto.trim().split('@');
  var subjects1='', subjects2='', subjects3='';
  for (var i=0; i<sugerencias.length; i++){
       var datos = sugerencias[i].split('::');
       switch(i)
       {  case 0: subjects1 = datos[0]; 
                  break;
          case 1: subjects2 = datos[0]; 
                  break;
          case 2: subjects3 = datos[0]; 
                  break;
       }
  }
   
  SugerenciaPanel = panel.Panel({
   width: 315,
   height: 430,
   contentURL: data.url("index.html"),
   contentScriptFile: [data.url("jquery.tools.min.js"), data.url("tabs-accordion.css"), data.url("tooltip.css")],
   contentScript : 
        '$("#sug1").html("<p>' + subjects1  + '</p>");' +
        '$("#sug2").html("<p>' + subjects2  + '</p>");' +
        '$("#sug3").html("<p>' + subjects3  + '</p>");'
  });
  SugerenciaPanel.show();  
}