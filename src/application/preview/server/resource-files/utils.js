view1 = null;
		view2 = null;
      onload=function() {
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
				}
			});
			shortcut.add("Page_up", function() {
				i = yPos();
				movePage(-1);
				// if nothing happened, try again
				if (i == yPos()) {
					movePage(-1);
				}
			});
			shortcut.add("Cltr+1", function() {
				toggleViews();
			});

		}
		  visible = true;
		  function toggleVisibility() {
			  visible = !visible;
			  alert(visible);
			  
		  }
	  
		function toggleById(id) {
			toggle(document.getElementById(obj));
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
			if (navigator.appName == "Microsoft Internet Explorer") {
				return document.body.scrollTop;
			} else {
				return window.pageYOffset;
			}
		}
		
		function setAll(elements, val) {
			for (i=0;i<elements.length;i++) {
				elements[i].style.visibility = val;
			}
		}
		
		function toggleViews() {
			
			if (view1 == null | view2 == null) {
				view1 = document.getElementsByClassName('page');
				setAll(view1, 'hidden');
				view2 = document.getElementsByClassName('text');
				setAll(view2, 'visible');

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
			setPage(1 * document.getElementById('gotoPage').value + val);
			gotoPage();
		}
		function setPage(val) {
			document.getElementById('gotoPage').value = val;
		}
		function gotoPage() {
			document.location.href='#pagenum' + document.getElementById('gotoPage').value;
		}