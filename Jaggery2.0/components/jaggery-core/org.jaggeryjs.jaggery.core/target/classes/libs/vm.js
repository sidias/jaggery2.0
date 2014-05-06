
//look what are ths options node providing
var bind = jaggery.bind('contextify');
var Script = bind.contextifyScript;

//vm methods
/*var vm = require('vm');
vm.createScript;
vm.createScript; */

var vm = exports;

//expose jaggery.bind('contextify') to script
vm.script = Script;

vm.createScript = function(content, fileName) {
	return new Script(content, fileName);
}

/*execute in the current context global context
 @param	  fileName  name of the file to execute
 @param	  content   content of a file as a string
*/
vm.runInThisContext = function(content, fileName) {
	var script = new Script(content, fileName);
	return script.runInThisContext();
}

/* run Script in a new global Context
*  @param	fileName  name of the file to execute
*  @param	content   content of a file as a string
* */
vm.runInNewContext = function(content, fileName) {
	var script = new Script(content, fileName);
	return script.runInNewContext();
}

vm.isContext = bind.isContext;



