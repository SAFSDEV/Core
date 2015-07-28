<!--
d=document;
var sa=d.createElement('OBJECT');
sa.setAttribute('classid','clsid:8AD9C840-044E-11D1-B3E9-00805F499D93');
sa.setAttribute('codebase','http://java.sun.com/products/plugin/1.5/plugin-install.html');
sa.setAttribute('width','0');
sa.setAttribute('height','0');

var app=sa.appendChild(d.createElement("<PARAM NAME=CODE VALUE='java.applet.Applet.class'>"));
var apptype=sa.appendChild(d.createElement("<PARAM NAME='type' VALUE='application/x-java-applet;version=1.5'>"));
var scriptable=sa.appendChild(d.createElement("<PARAM NAME='scriptable' VALUE='true'>"));
var mayscript=sa.appendChild(d.createElement("<PARAM NAME='MAYSCRIPT' VALUE='true'>"));

d.getElementsByTagName('html')[0].appendChild(sa);
// -->

<!--
d=document;
sa=d.createElement('OBJECT');
sa.setAttribute('classid','clsid:8AD9C840-044E-11D1-B3E9-00805F499D93');
sa.setAttribute('codebase','http://java.sun.com/products/plugin/1.5/plugin-install.html');
sa.setAttribute('width','0');
sa.setAttribute('height','0');

app=sa.appendChild(d.createElement("PARAM"));
app.setAttribute('id','safsp1');
app.setAttribute('NAME','CODE');
app.setAttribute('VALUE','java.applet.Applet.class');

apptype=sa.appendChild(d.createElement("PARAM"));
apptype.setAttribute('id','safsp2');
apptype.setAttribute('NAME','type'); 
apptype.setAttribute('VALUE','application/x-java-applet');

scriptable=sa.appendChild(d.createElement("PARAM"));
scriptable.setAttribute('id','safsp3');
scriptable.setAttribute('NAME','scriptable');
scriptable.setAttribtue('VALUE','true');

d.getElementsByTagName('html')[0].appendChild(sa);
// -->

<!--

safsapplet = document.createElement('OBJECT');
safsapplet.setAttribute('classid','clsid:8AD9C840-044E-11D1-B3E9-00805F499D93');
safsapplet.setAttribute('codebase','http://java.sun.com/products/plugin/1.5/plugin-install.html');
safsapplet.setAttribute('width','0');
safsapplet.setAttribute('height','0');

param1=document.createElement("<PARAM NAME='CODE' VALUE='java.applet.Applet.class' >");    
param2=document.createElement("<PARAM NAME='type' VALUE='application/x-java-applet' >");    
param3=document.createElement("<PARAM NAME='scriptable' VALUE='true' >");    
safsapplet.appendChild(param1);
safsapplet.appendChild(param2);
safsapplet.appendChild(param3);

void(document.getElementsByTagName('body')[0].appendChild(safsapplet));

// -->