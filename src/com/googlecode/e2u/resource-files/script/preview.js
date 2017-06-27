 view1 = null;
		view2 = null;
      onload=function() {
			ping();
			if (document.getElementsByClassName == undefined) {
				document.getElementsByClassName = function(className)
				{
					var hasClassName = new RegExp("(?:^|\\s)" + className + "(?:$|\\s)");
					var allElements = document.getElementsByTagName("*");
					var results = [];
			
					var element;
					for (var i = 0; (element = allElements[i]) != null; i++) {
						var elementClass = element.className;
						if (elementClass && elementClass.indexOf(className) != -1 && hasClassName.test(elementClass))
							results.push(element);
					}
					return results;
				}
			}
			shortcut.add("Page_down", function() {
				i = yPos();
				movePage(1);
				// if nothing happened, try again
				if (i == yPos()) {
					movePage(1);
					// if still nothing happened, revert
					if (i == yPos()) {
						movePage(-2);
					}
				}
			});
			shortcut.add("Page_up", function() {
				i = yPos();
				movePage(-1);
				// if nothing happened, try again
				if (i == yPos()) {
					movePage(-1);
					// if still nothing happened, revert
					if (i == yPos()) {
						if (1 * document.getElementById('gotoPage').value > 1) {
							movePage(2);
						}
					}
				}
			});
			shortcut.add("alt+v", function() {
				toggleViews();
			});
			shortcut.add("ctrl+i", function() {
				toggleById('about');
			});
		}
		  visible = true;
		  function toggleVisibility() {
			  visible = !visible;
			  alert(visible);
			  
		  }
	  
		function toggleById(id) {
			toggle(document.getElementById(id));
		}
		
		function toggleAllByName(className) {
			elements = document.getElementsByClassName(className);
			toggleAll(elements);
		}
		
		function toggleAll(elements) {
			for (i=0;i<elements.length;i++) {
				toggle(elements[i]);
			}
		}
		
		function yPos() {
			return getScrollXY()[1];
		/*
			if (navigator.appName == "Microsoft Internet Explorer") {
				return document.documentElement.scrollTop;
			} else {
				return window.pageYOffset;
			}*/
		}
		
		function getScrollXY() {
  var scrOfX = 0, scrOfY = 0;
  if( typeof( window.pageYOffset ) == 'number' ) {
    //Netscape compliant
    scrOfY = window.pageYOffset;
    scrOfX = window.pageXOffset;
  } else if( document.body && ( document.body.scrollLeft || document.body.scrollTop ) ) {
    //DOM compliant
    scrOfY = document.body.scrollTop;
    scrOfX = document.body.scrollLeft;
  } else if( document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop ) ) {
    //IE6 standards compliant mode
    scrOfY = document.documentElement.scrollTop;
    scrOfX = document.documentElement.scrollLeft;
  }
  return [ scrOfX, scrOfY ];
}
		
		function setAll(elements, val) {
			for (i=0;i<elements.length;i++) {
				elements[i].style.visibility = val;
			}
		}
		
		function toggleViews() {
			
			if (view1 == null | view2 == null) {
				view1 = document.getElementsByClassName('page');
				setAll(view1, 'visible');
				view2 = document.getElementsByClassName('text');
				setAll(view2, 'hidden');

			} else {
				toggleAll(view1);
				toggleAll(view2);
			}
			return false;
		}		

		function toggle(obj)
		{
			if (obj.style.visibility == 'visible') { obj.style.visibility = 'hidden'; }
			else { obj.style.visibility = 'visible'; }
		}
		function movePage(val) {
			setTo = 1 * document.getElementById('gotoPage').value + val;
			if (setTo >= 1) {
				setPage(setTo);
				gotoPage();
			}
		}
		function setPage(val) {
			document.getElementById('gotoPage').value = val;
		}
		function gotoPage() {
			document.location.href='#pagenum' + document.getElementById('gotoPage').value;
		}
		


function get(url) {
	xmlHttp=GetXmlHttpObject();
	if (xmlHttp==null) {
  		return;
  	} 
  	url=url+"?sid="+Math.random();
  	try {
		xmlHttp.open("GET",url,true);
		xmlHttp.onreadystatechange=function() {
	  		if (xmlHttp.readyState==4) {
				if (xmlHttp.status!=200) {
					document.getElementById("connected").style.visibility="hidden";
					document.getElementById("notConnected").style.visibility="visible";
					ok = false;
				} else {
					var t=setTimeout("ping()",5000);
				}
			}
		}
		xmlHttp.send(null);
	} catch (e) {}
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
function ping() {
	get("ping.xml");
}