function get() {
	xmlHttp=GetXmlHttpObject();
	if (xmlHttp==null) {
  		return;
  	} 
	var url="fakeurltogetietoclose.html";
	url=url+"?sid="+Math.random();
	xmlHttp.open("GET",url,false);
	xmlHttp.send(null);
}

function GetXmlHttpObject() {
	var xmlHttp=null;
	try {
  		// Firefox, Opera 8.0+, Safari
  		xmlHttp=new XMLHttpRequest();
  	} catch (e) {
  	// Internet Explorer
  		try {
    		xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
    	} catch (e)	{
    		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
		}
	}
	return xmlHttp;
}