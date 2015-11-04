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
			shortcut.add("v", function() {
				toggleViews();
			});
			shortcut.add("o", function() {
				toggleById('about');
			});
			shortcut.add("ctrl+p", function() {
				location.href="/";
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
		
/**
 * http://www.openjs.com/scripts/events/keyboard_shortcuts/
 * Version : 2.01.B
 * By Binny V A
 * License : BSD
 */
shortcut = {
	'all_shortcuts':{},//All the shortcuts are stored in this array
	'add': function(shortcut_combination,callback,opt) {
		//Provide a set of default options
		var default_options = {
			'type':'keydown',
			'propagate':false,
			'disable_in_input':false,
			'target':document,
			'keycode':false
		}
		if(!opt) opt = default_options;
		else {
			for(var dfo in default_options) {
				if(typeof opt[dfo] == 'undefined') opt[dfo] = default_options[dfo];
			}
		}

		var ele = opt.target;
		if(typeof opt.target == 'string') ele = document.getElementById(opt.target);
		var ths = this;
		shortcut_combination = shortcut_combination.toLowerCase();

		//The function to be called at keypress
		var func = function(e) {
			e = e || window.event;
			
			if(opt['disable_in_input']) { //Don't enable shortcut keys in Input, Textarea fields
				var element;
				if(e.target) element=e.target;
				else if(e.srcElement) element=e.srcElement;
				if(element.nodeType==3) element=element.parentNode;

				if(element.tagName == 'INPUT' || element.tagName == 'TEXTAREA') return;
			}
	
			//Find Which key is pressed
			if (e.keyCode) code = e.keyCode;
			else if (e.which) code = e.which;
			var character = String.fromCharCode(code).toLowerCase();
			
			if(code == 188) character=","; //If the user presses , when the type is onkeydown
			if(code == 190) character="."; //If the user presses , when the type is onkeydown

			var keys = shortcut_combination.split("+");
			//Key Pressed - counts the number of valid keypresses - if it is same as the number of keys, the shortcut function is invoked
			var kp = 0;
			
			//Work around for stupid Shift key bug created by using lowercase - as a result the shift+num combination was broken
			var shift_nums = {
				"`":"~",
				"1":"!",
				"2":"@",
				"3":"#",
				"4":"$",
				"5":"%",
				"6":"^",
				"7":"&",
				"8":"*",
				"9":"(",
				"0":")",
				"-":"_",
				"=":"+",
				";":":",
				"'":"\"",
				",":"<",
				".":">",
				"/":"?",
				"\\":"|"
			}
			//Special Keys - and their codes
			var special_keys = {
				'esc':27,
				'escape':27,
				'tab':9,
				'space':32,
				'return':13,
				'enter':13,
				'backspace':8,
	
				'scrolllock':145,
				'scroll_lock':145,
				'scroll':145,
				'capslock':20,
				'caps_lock':20,
				'caps':20,
				'numlock':144,
				'num_lock':144,
				'num':144,
				
				'pause':19,
				'break':19,
				
				'insert':45,
				'home':36,
				'delete':46,
				'end':35,
				
				'pageup':33,
				'page_up':33,
				'pu':33,
	
				'pagedown':34,
				'page_down':34,
				'pd':34,
	
				'left':37,
				'up':38,
				'right':39,
				'down':40,
	
				'f1':112,
				'f2':113,
				'f3':114,
				'f4':115,
				'f5':116,
				'f6':117,
				'f7':118,
				'f8':119,
				'f9':120,
				'f10':121,
				'f11':122,
				'f12':123
			}
	
			var modifiers = { 
				shift: { wanted:false, pressed:false},
				ctrl : { wanted:false, pressed:false},
				alt  : { wanted:false, pressed:false},
				meta : { wanted:false, pressed:false}	//Meta is Mac specific
			};
                        
			if(e.ctrlKey)	modifiers.ctrl.pressed = true;
			if(e.shiftKey)	modifiers.shift.pressed = true;
			if(e.altKey)	modifiers.alt.pressed = true;
			if(e.metaKey)   modifiers.meta.pressed = true;
                        
			for(var i=0; k=keys[i],i<keys.length; i++) {
				//Modifiers
				if(k == 'ctrl' || k == 'control') {
					kp++;
					modifiers.ctrl.wanted = true;

				} else if(k == 'shift') {
					kp++;
					modifiers.shift.wanted = true;

				} else if(k == 'alt') {
					kp++;
					modifiers.alt.wanted = true;
				} else if(k == 'meta') {
					kp++;
					modifiers.meta.wanted = true;
				} else if(k.length > 1) { //If it is a special key
					if(special_keys[k] == code) kp++;
					
				} else if(opt['keycode']) {
					if(opt['keycode'] == code) kp++;

				} else { //The special keys did not match
					if(character == k) kp++;
					else {
						if(shift_nums[character] && e.shiftKey) { //Stupid Shift key bug created by using lowercase
							character = shift_nums[character]; 
							if(character == k) kp++;
						}
					}
				}
			}
			
			if(kp == keys.length && 
						modifiers.ctrl.pressed == modifiers.ctrl.wanted &&
						modifiers.shift.pressed == modifiers.shift.wanted &&
						modifiers.alt.pressed == modifiers.alt.wanted &&
						modifiers.meta.pressed == modifiers.meta.wanted) {
				callback(e);
	
				if(!opt['propagate']) { //Stop the event
					//e.cancelBubble is supported by IE - this will kill the bubbling process.
					e.cancelBubble = true;
					e.returnValue = false;
	
					//e.stopPropagation works in Firefox.
					if (e.stopPropagation) {
						e.stopPropagation();
						e.preventDefault();
					}
					return false;
				}
			}
		}
		this.all_shortcuts[shortcut_combination] = {
			'callback':func, 
			'target':ele, 
			'event': opt['type']
		};
		//Attach the function with the event
		if(ele.addEventListener) ele.addEventListener(opt['type'], func, false);
		else if(ele.attachEvent) ele.attachEvent('on'+opt['type'], func);
		else ele['on'+opt['type']] = func;
	},

	//Remove the shortcut - just specify the shortcut and I will remove the binding
	'remove':function(shortcut_combination) {
		shortcut_combination = shortcut_combination.toLowerCase();
		var binding = this.all_shortcuts[shortcut_combination];
		delete(this.all_shortcuts[shortcut_combination])
		if(!binding) return;
		var type = binding['event'];
		var ele = binding['target'];
		var callback = binding['callback'];

		if(ele.detachEvent) ele.detachEvent('on'+type, callback);
		else if(ele.removeEventListener) ele.removeEventListener(type, callback, false);
		else ele['on'+type] = false;
	}
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