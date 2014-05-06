/* <mindful>
 	1.) warning
             do not use objName.hasOwnProperty
    2.) get all the known core modules using JagNative
 */

//reference to Native func in scr/jaggery.js
var JagNative = require('natives');
var path = JagNative.require('path');
//var util = require('util');

var platform = jaggery.platform;
var pathSep = (platform == 'Linux') ? '/' : '';

//wrapper use when compiling.
Module.wrapper = JagNative.wrapper;

function hasOwnProperty(obj, prop) {
	return Object.prototype.hasOwnProperty.call(obj, prop);
}

//path status
function statsPath(path) {
	var fs = JagNative.require('fs');
	//file relate task may throw error;
	try {
		return fs.statPath(path); //{return object}
	} catch(err) {
		throw err;
	}
}

function Module(id, parent) {
	this.id = id;
	this.fileName = null;
	this.exports = {};
	this.parent = parent;
	this.loaded = false;
}

//Module can be call as a function wherever is required;
//use to call runMain through src/jaggery.js
module.exports = Module;

//fullyPathCache is stored in a object user requirePath as a key against absolutePath of that user requirePath
Module.fullyPathCache = {};
Module.module_cache = {};
Module.extensions = {};

var packageCache = {};

Module.extensions = ['.js','json']; //insert .jag
//handle json.

/*
 * read package.json file in given path
 * param -> package - package.json content;
 * */
function readPackage(absPath) {

	var mainPath = '';
	var package = tryPackage(absPath);

	if (package) {
		try {
			var pack = JSON.parse(package);

			//cache parsed package.
			packageCache[absPath] = package;
			var main = pack.main /*'./index'*/;

			if (main) {
				var mainModulePath = path.resolve(absPath, main);

				//baseName of the mainModlePath
				var mainModule_base = path.baseName(mainModulePath);

				//check if (package.json).main has valid extension
				if(path.extName(mainModule_base)) {
					mainPath = tryFile(mainModulePath, null);     // if package.json mention main not available returns false
				} else {
					mainPath = tryExtensions(mainModulePath);
				}

				if(!mainPath) {
					throw new Error('file mention in (pacakge.json).main could not found');
				}
				return mainPath;

			} else {
				throw new Error('no valid main module mention in (package.json).main')
			}
		} catch (error) {
			throw error;
		}
	}

	//search for index.jag or index.js
	var index = tryExtensions([absPath, 'index'].join(pathSep));
	return index;   //may be false
};

/*
 * find if package.json exists
 * */
function tryPackage(absPath) {

	if (hasOwnProperty(packageCache, absPath)) {
		return packageCache[absPath];
	}

	var packagePath = [absPath, 'package.json'].join(pathSep);
	//inside dir
	//check if package.json exists before read
	var has_pkg = tryFile(packagePath, null);;

	if(has_pkg) {
		var fs = require('fs');
		var pkg = fs.readFile(packagePath);
		if (pkg) {
			return pkg;
		}
	}
	return false;   //if this func return false try to read index.jag or index.js   */
};

//read the file from given path
/*
* if path is a js file statpath will return path with extension name
* if path is a dir statpath will return path without extension
* */
function tryFile(filePath, realRequest) {

	var absPath;
    if(realRequest  != null) {
		var pathToJoin = [filePath, realRequest];
		absPath = pathToJoin.join(pathSep);
	} else {
		absPath = filePath;
	}

	var statpath = statsPath(absPath);
	return statpath.isPathExists;
};

//try given filepath with all extensions
//wrong here. check which try extension is using there are two try extensions
function tryExtensions(absPath) {

	var ext = ['.js'];//Object.keys(Module.extensions);
	var extLength = ext.length;

	for (var i = 0; i < extLength; i++) {
		var file = tryFile(absPath + ext[i], null);
		if (file) {
			return file;
		}
	}
	return false;
};

Module.findPath = function (realRequest, paths) {

	/*check user gives fileName extension or not
	 if extension is not given we have to check for .js and .jag
	 */
	var fileName = path.baseName(realRequest);
    var extension = path.extName(realRequest);

	if(!Array.isArray(paths)) {

		//require('foo') foo don't have extension. so this if will be skipped if passed as 'foo'.because foo has no extension.
		if (extension) {
			return tryFile(paths, null);
		} else {
			//if no extension first search for single.js file. if not found looks for folderModule
			var ext = tryExtensions(paths);

			if(ext) {
				return ext;
			}

			var folderModule = tryFile(paths, null);
			if(folderModule) {
				var mainSource = readPackage(folderModule);
				return mainSource;
			}
			return false;
		}
	} else {  //fill this if not normal files will do the same stuff.
		//search in jaggery_module folders
		var jaggeryModulePaths = paths;
		var file = '', fileParts = '';

		//change this method firs .js file then folder
		for (var i = 0; i < jaggeryModulePaths.length; i++) { 					//real request --> eg : foo

			//first we try to get fileName.js file.if not then looks for folder as module
			fileParts = [jaggeryModulePaths[i], fileName]
			file = tryExtensions(fileParts.join(pathSep));

			if(file) {
				return file;
				break;
			}

			//file is a dir(without extension).read package.json and get mainModule/index path
			file = tryFile(jaggeryModulePaths[i], fileName);
			if(file) {
			   	var mainSource = readPackage(file);
				return mainSource;
			}
		}
		return false;
	}
};

/*
 * param   request - module;
 *         parent - parent module request this module.
 *         (param parent use to resolve the relative path)
 *
 *         if module is core module it will not be cached into Module.module_cache obj
 *         it will be cached into Native._cache obj in jaggery.js
 * */
Module._load = function (request, parent) {

	//in this point module path is resolved to it's absolute path;
	//check file exist in given dir
	var filePath = Module.resolveFileName(request, parent);

	//if request module is a jaggery core module resolveFileName func return coreModuleName;
	var cacheModule = hasOwnProperty(Module.module_cache, filePath);
	if (cacheModule) {
		var module = Module.module_cache[filePath];
		return module.exports;
	}

	//if module exists cache it
	if (filePath) {
		//cache fullPath if not cached
		//guarantee to cache full path at the first time if module was not cached.use if module path is found
		//but unable to load module;
		if(!hasOwnProperty(Module.fullyPathCache, request)) {
			Module.fullyPathCache[request] = filePath;
		}

		var f_letter = filePath.charAt(0);
		//check if a core modules(resolveFileName return just module name without beginning slash, if module is a core,otherwise return
		// string path beginning with / regarding to module);
		if( f_letter !== '/') {
			//Module.resolveFileName returns single string if required module is a core module
			//guarantee to call if module is a core module.
			return JagNative.require(filePath);
		}

		//found the targeted file, main is a absolute path to the main js file
		//create a module and compile it and send module.exports
		var module = new Module(filePath, parent);
		module.load(filePath);

		//read the file and compile it and return exports
		return module.exports;		//create a module and compile it and send module.exports

	} else {
		var error = new Error('requested module ' + request + ' not found ');
		throw error;
	}
};

//send resolve filename
//module can be as following
/*can be  	1.require(foo) - foo can be core module or module inside jaggery_module folder
 			3../foo(with,without extension)
 			4.../../foo(with,without extension)
 			5.abs path(with,without extension)
 */
Module.resolveFileName = function (request, parent) {

	//check path cache
	if (hasOwnProperty(Module.fullyPathCache, request)) {
		return Module.fullyPathCache[request];
	}

	//check for core modules in src/lib
	if (JagNative.source_exists(request)) {
		return request;
	}

	//set ./ of ../ or ../jag_module/moduleName paths to check
	var resolvedPaths = Module.resolvePaths(request, parent);
	var requests = resolvedPaths[0];
	var pathsToSearch = resolvedPaths[1];

	//use findPath method to see if this file exists
	var filePath = Module.findPath(requests, pathsToSearch);

	if (filePath) {
		return filePath;
	}
	return false;
};

//set all possible paths to check.
Module.resolvePaths = function (request, parent) {

	//if request is abs
	if(path.isAbsolutePath(request)) {
		return [request, request];
	}

	var parentPaths = '';
	if (parent != null) {
		parentPaths = path.dirName(parent.fileName);
	}

	var childPath = path.resolve(parentPaths, request);

	//guarantee requests starting with ./ or ../ only reach here(require('foo') will be skipped)
	var start = request.slice(0, 2);
	if (start === '..' || start === './') {
	//assume parent path is a abs path because parent module is a already created module.
		return [request, childPath];
	}

	//if require('foo') is not a core module and assume lay inside jaggery_module folder
	//assume parent is a fully resolved path; cwd may null with first startup;(runMain pass null);
	var jagmod_paths = Module.jagModulePaths(childPath);

	//jagmod_paths is an array
	return [request, jagmod_paths];
};

/*jaggery_module folder paths
 * param -> jaggery module name
*/
Module.jagModulePaths = function (request) {

	request = path.dirName(path.resolve(request));

	var modulePaths = [];
	var splitPath = request.split(pathSep);
	splitPath.shift();

	var p = '';
	for (var i = 0; i < splitPath.length; i++) {

		//do not search in jaggery_module/jaggery_module
		if(splitPath[i] == 'jaggery_module') {
			continue;
		} else {
			p = p.concat(pathSep).concat(splitPath[i]);
			modulePaths.unshift(p.concat(pathSep).concat('jaggery_module'));
		}
	}
	return modulePaths;
};

//add more stuff.
Module.prototype.load = function (absPath) {

	//fill newly created module object with data
	this.fileName = absPath;
	//read the content of file;
	var content = require('fs').readFile(this.fileName);

	//compiled the source and fill module.exports object;
	this.compile(content);
};

//instance property of Module object.use to load module to this module
Module.prototype.require = function (request) {
	var module = Module._load(request, this);
	return module;
};

Module.prototype.compile = function(content) {
	var self = this;

	//param - required path
	function require(path) {
		return self.require(path);
	}

	/*if developer want to expose more functionalists to the scriptEnvironment. bind them as static methods to
	'require' function*/

	//wrapped content ;
	var wrapped_content = Module.wrapper[0] + content + Module.wrapper[1];

	//function(exports, require, module, fileName)
	var Script = jaggery.bind('contextify').contextifyScript;
 	var fn = new Script(wrapped_content, this.fileName).runInThisContext();
 	fn(this.exports, require, this, this.fileName);

 	//cache module after compiled
 	Module.module_cache[this.fileName] = this;
 };

 /* starting point
 * */
Module.Main = function () {
print('main module');
	var querystring = Module._load(/*jaggery.files[0]*/'querystring', null);   //return {};



    //var stringified = querystring.stringify({ foo: 'bar', baz:'quux',baz:'buddhi', corge: '' });
    //print(stringified);
    //var stringified = "foo=bar&foo=yyyy&foo=rty&baz=qux&corge=jj";
	//var parsed = querystring.parse(stringified);
    //print(parsed.foo + '*'+ parsed.baz + '*' + parsed.corge);


	//print(Object.keys(vm));
    //var script = vm.compileScript('myfile.js','globalVar += 1');

    /*for (var i = 0; i < 1000 ; i += 1) {
         vm.runScripts(script);
    }
      print(globalVar)   */

	   //========================================================
//very useful tests in vm testing
	//var k = vm.runInNewContext('buddhi.js', 'var foo = {age:23}; print(foo.age)')
	//print(Object.prototype.toString.call(k))    // --- print [Object Undefined]
	//var foo = {age:23}
	//var k = vm.runInNewContext('buddhi.js', foo);


	/*node's following approch achieve by this way in jaggery
	 var util = require('util'),
	 vm = require('vm'),
	 sandbox = {
	 animal: 'cat',
	 count: 2
	 };

	 vm.runInNewContext('count += 1; name = "kitty"', sandbox, 'myfile.vm');

	 instead of passing sandbox argument you can get a function and then execute that function by given sandbox param as follow
	*
	* */
	/*var k = vm.runInNewContext('buddhi.js', 'function(sandbox){ sandbox.count += 1; sandbox.name = "kitty"};');
	var sandbox = { animal: "cat",count: 2}
	k(sandbox);
	print(sandbox.count); */
	//print(Object.prototype.toString.call(k))    //-- print [Object Function]
 ///====================================================================================

	//test for vm.runInThisContext example from node.js
	/*var localVar = 123,
		usingscript, evaled,

		usingscript = vm.runInThisContext('localVar = 1;','myfile.vm'
			);
	print('localVar: ' + localVar + ', usingscript: ' +
		usingscript);
	evaled = eval('localVar = 1;');
	print('localVar: ' + localVar + ', evaled: ' +
		evaled);
	 print('---------------------------------')
	//var path = Module._load(/*jaggery.files[0]'path', null);*/
	//var k = path.resolve('/foo/bar', '/tmp/file/');
	//print(k);

};







