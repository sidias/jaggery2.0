var Context 		= Java.type("jdk.nashorn.internal.runtime.Context");
var ScriptRuntime 	= Java.type("jdk.nashorn.internal.runtime.ScriptRuntime");
var Source 			= Java.type("jdk.nashorn.internal.runtime.Source");
var File            = Java.type("java.io.File");
var IOException     = Java.type("java.io.IOException");
var Files           = Java.type("java.nio.file.Files");
var Path            = Java.type("java.nio.file.Path");
var Paths           = Java.type("java.nio.file.Paths");
var ManagementFactory = Java.type("java.lang.management.ManagementFactory");
//delete later
var HashMap			= Java.type("java.util.HashMap");
var StringBuilder	= Java.type("java.lang.StringBuilder");
var ByteOrder		= Java.type('java.nio.ByteOrder');
var InetAddress		= Java.type('java.net.InetAddress');
var Runtime			= Java.type('java.lang.Runtime');
var Scanner			= Java.type('java.util.Scanner');
var FileInputStream = Java.type('java.io.FileInputStream');

//use javascript object instead of hashmap.
var property = new HashMap();
//------------- delete later

function hasOwnProperty(obj, prop) {
	return Object.prototype.hasOwnProperty.call(obj, prop);
}

function readCore() {
    //do not hard code core lib path get it from jaggery context.
    //libpath will be put into jaggery context;
	var libPath = "/home/buddhi/IdeaProjects/Jaggery2/lib";

	var libDir = new File(libPath.toString());
	if( !libDir.exists() || !libDir.isDirectory()){
		return;
	}

	var jsFile = Java.from(libDir.listFiles());

	for(var i=0; i< jsFile.length;i++){
		if( !jsFile[i].isFile() &&  !jsFile[i].getName().endsWith(".js")){
			continue;
		}
		var jsSource = new Source(jsFile[i].getName(), jsFile[i]);
		var fileName = jsFile[i].getName().replace( ".js","");
		property.put(fileName ,jsSource.getString());
	}
}

//move this down
readCore();

function systemProperty(property) {
	return java.lang.System.getProperty(property)
};

//delete -----------
function getModule(moduleName) {
	return property.containsKey(moduleName) ? property.get(moduleName):"";
}
//------------------

var contextify = {
	isContext:function(sandbox) {

	},

	contextifyScript:function(content, fileName) {
		if(!content || typeof content !== 'string') {
			throw new Error("empty arguments");
		}
		var functions, func;
		var context = Context.getContext();
		var errorManager = context.getErrorManager();

		this.runInThisContext = function() {
			var global = Context.getGlobal();

			func = context.compileScript(new Source("",content.toString()), global);
			if(func == null || errorManager.getNumberOfErrors() != 0 ){
				//sent javascript error to the output
			}

			try{
				functions = ScriptRuntime.apply(func, global);
			}catch(e) {
				errorManager.error(e.toString());
				if(context.getEnv()._dump_on_error){
					e.printStackTrace(context.getErr());
				}
			}finally {
				context.getOut().flush();
				context.getErr().flush();
			}
			return functions;
		}

		this.runInNewContext = function() {
			var newGlobal = context.createGlobal();

			func = context.compileScript(new Source("",content.toString()), newGlobal);

			if(func == null || errorManager.getNumberOfErrors() != 0 ){
				throw new Error("execution failed ");
			}

			try{
				functions = ScriptRuntime.apply(func, newGlobal);
			} catch (e){
				errorManager.error(e.toString());
				if(context.getEnv()._dump_on_error){
					e.printStackTrace(context.getErr());
				}
			} finally {
				context.getOut().flush();
				context.getErr().flush();
			}
			return functions;
		}
	}
};

var fs = {
	statPath: function(path) {
		if(!path || typeof path !== 'string') {
			throw new Error("empty arguments");
		}

		var fileName = Paths.get(path.toString());
		var isRegularReadableFile;

		try {
			isRegularReadableFile = Files.isRegularFile(fileName) & Files.isReadable(fileName);

			if(isRegularReadableFile) {
				var pathType = Files.isRegularFile(fileName) && (path.toString().contains(".js") ||
					path.toString().contains(".jag"));

				if(pathType) {
					return path;
				}
			} else {
				var pathType = Files.isDirectory(fileName);
				if(pathType) {
					return path;
				}
			}
		}catch(e) {
			//throw error to script env
		}
		return false;
	},

	readFile: function(path) {
		if(!path || typeof path !== 'string') {
			throw new Error('empty arguments');
		}

		var fileName = Paths.get(path.toString());
		var filePath = fileName.toFile();
		var source = null;
		try {
			source = new Source(fileName.toString(), filePath);
		} catch (e) {
			e.printStackTrace();
		}
		return source.getString();
	}
};

var natives = {
	cache		: getModule('cache'),
	debugger	: getModule('debugger'),
	fs			: getModule('fs'),
	module		: getModule('module'),
	path		: getModule('path'),
	querystring	: getModule('querystring'),
	uri			: getModule('URI'),
	util		: getModule('util'),
	vm			: getModule('vm'),
	os          : getModule('os')
}

var os = {
	endian  	:(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) ? 'BE':'LE',
	hostname 	:InetAddress.getLocalHost().getHostName(),
	uptime 		:new Scanner(new FileInputStream("/proc/uptime")).next(),   //change latency issue
	OSType		:systemProperty('os.name'),
	OSRlease 	:systemProperty('os.version'),
	tmpdir		:systemProperty('java.io.tmpdir')
};

var load = {
    /*require will be registered as a global function.
    * purpose is to give control over module loading system to user via cmd.
    *
    * */
    require : req,
	jaggery:{
		bind:function(property) {
			switch (property) {
				case 'contextify':
					return contextify;
					break;
				case 'natives':
					return natives;
					break;
				case 'fs':
					return fs;
					break;
				case 'os':
					return os;
					break;
			}
		},
		version	:	"Jaggery 2",
		arch	:	systemProperty("os.arch"),
		argv	: 	systemProperty(), 			//command line arguments.
		platform: 	systemProperty("os.name"),
		cwd		:  	new File("").getAbsolutePath(),
		env		:   java.lang.System.getenv(),
		files	:   function() {
						var scriptEnviron = Context.getContext().getEnv();
						var files = scriptEnviron.getFiles();
						if(!files.isEmpty()){
							return files;
						}
					},
		pid		:	ManagementFactory.getRuntimeMXBean().getName().split("@",1),   //pid for jvm
		kill	: "this functionality will implement later",
		uptime	: "server up time",
		execPath: "executable path of node",
		chdir	: function(){}
	},
	exit    : function() { exit();}
};

//bind jaggery object to current global scope
Object.bindProperties(this,load);


//module loading system
/*
* only .js files are accepted;		 * */

//get source as a string wrap and return as a javascript function
function runInThisContext(content, fileName) {
	var Script = contextify.contextifyScript;   // == jaggery.bind('contextify').contextifyScript;
	var fn = new Script(content,fileName).runInThisContext();

	return fn;
}

function Native(id) {
	this.id = id;
	this.fileName = id + '.js';
	this.exports = {};
};

//source codes compiled as strings
Native._source = natives; // == jaggery.bind('natives');
Native._cache = {};

Native.require = function (id) {
	if (id == 'natives') {
		return Native;
	}

	var hasCache = Native.cache_exists(id);
	if (hasCache) {
		var cached = Native.getCache(id);
		return cached.exports;
	}

	var jagModule = new Native(id);

	jagModule.compile();
	//cache the compiled object
	jagModule.cache();
	return jagModule.exports;
}

//compile wrapped content of core .js files
Native.prototype.compile = function () {
	var fn = runInThisContext(Native.wrap(this.id), this.fileName);
	fn(this.exports, Native.require, this, this.fileName);
};

Native.wrapper = ['function(exports, require, module, fileName) {\n',
			'\n}'];

Native.wrap = function (id) {
	return Native.wrapper[0] + Native.getSource(id) + Native.wrapper[1];
};

Native.getSource = function (id) {
	if (Native.source_exists(id)) {
		return Native._source[id];
	}
};

Native.getCache = function (id) {
	return Native._cache[id];
}

Native.source_exists = function (id) {
	return hasOwnProperty(Native._source, id);
};

//cache existences check
Native.cache_exists = function (id) {
	return hasOwnProperty(Native._cache, id);
};

//cache the module
Native.prototype.cache = function () {
	Native._cache[this.id] = this;
};

/*REPL implementation of jaggery require
* parameter parent = null(loading from cmd)
* */
function req(filePath) {
    var module = Native.require('module');
    return module._load(filePath, null);
}

//test

var module = Native.require('module');
//calling static method Module.Main inside module.js
module.Main();


