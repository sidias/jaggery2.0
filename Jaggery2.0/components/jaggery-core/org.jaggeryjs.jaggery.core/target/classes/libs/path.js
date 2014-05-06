//initialize Array.filter method;
/*if (!Array.prototype.filter) {
 Array.prototype.filter = function(fun ) {
 'use strict';

 if (!this) {
 throw new TypeError();
 }

 var objects = Object(this);
 var len = objects.length >>> 0;
 if (typeof fun !== 'function') {
 throw new TypeError();
 }

 var res = [];
 var thisp = arguments[1];
 for (var i in objects) {
 if (objects.hasOwnProperty(i)) {
 if (fun.call(thisp, objects[i], i, objects)) {
 res.push(objects[i]);
 }
 }
 }
 return res;
 };
 } */

var pathSep = (jaggery.platform === 'Linux') ? '/' : '';

//all methods only for posix systems(otherwise abs path will fail)
var splitReg = /^(\/?|)([\s\S]*?)((?:\.{1,2}|[^\/]+?|)(\.[^.\/]*|))(?:[\/]*)$/;

function splits(path) {
	var paths = splitReg.exec(path).slice(1);
	return paths;
};

//normalize given path
function normalizedPath(parts, abs) {

	//this func take care of ./ and ../
	var root = 0;
	var end;
	for (var i = parts.length - 1; i >= 0; i--) {

		end = parts[i];

		if (end === '.') {
			//if found splice rear end
			parts.splice(i, 1);
		} else if (end === '..') {
			parts.splice(i, 1);
			root++;
			continue;
		}
		if (root) {
			parts.splice(i, 1);
			root--;
		}
	}

	if (!abs) {
		//no effect in ./.. it is same to ../
		//so no need to worry about front ./ of given path.just splice it
		for (var j = 0; j < root; j++) {
			parts = ['..'].concat(parts);
		}
	}
	delete end;
	return parts;
};

exports.normalize = function (path) {

	var abs = isAbsolute(path);
	var trailingSlash = path.substr(-1) === '/';

	if (!path && !trailingSlash) {
		return '.';
	}

	var normalized = /*normalizedPath*/path.split('/');
	var fs = normalized.filter(function (val) {
		if (val.length > 0) {
			return val;
		}
	}).join('/');

	var normal = normalizedPath(fs.split('/'), abs).join('/');
	if (abs) {
		normal = ('/').concat(normal);
	}

	if (trailingSlash) {
		normal = normal.concat('/');          //change if u can
	}

	if (!normal) {
		return false
	}
	return normal;
};

exports.join = function () {

	var args = Array.prototype.slice.call(arguments);
	return normalize(args.filter(function (val) {

		if (typeof val !== 'string') {
			throw new Error('arguments to path join must be string')
		} else if (val.substr(-1) === '/') {
			val.slice(-1);
		}
		return val;
	}).join('/'));
};

exports.resolve = function () {

	var aIndex = '#';
	var args = Array.prototype.slice.call(arguments);
	args = [jaggery.cwd].concat(args); //set the  home dir here

	var args = (args.filter(function (val, index) {
		if (typeof val !== 'string') {
			throw new Error('arguments to path resolve must be a string');
		}

		if (val.charAt(0) === '/') {
			aIndex = index;
		}
		return val;
	}));

	if (!isNaN(aIndex)) {
		var resolved = args.slice(aIndex);
		resolved = resolved.join('/');

		var k = normalizedPath(resolved.split('/').filter(function (val) {
			if (val.length > 0) {
				return val
			}
		}));
		k = ('/').concat(k.join('/'));
	}
	return (k.substr(-1) === '/') ? k.substr(0, k.length - 1) : k;
};

exports.relative = function (from, to) {

	var from = resolve(from);
	var to = resolve(to);

	var fromParts = from.split('/');
	fromParts.shift();

	var toParts = to.split('/');
	toParts.shift();

	var f_length = fromParts.length;
	var t_length = toParts.length;

	var min = Math.min(f_length, t_length);

	var matched = 0
	for (; matched < min; matched++) {
		if (fromParts[matched] !== toParts[matched]) {
			break;
		}
	}
	var relative = toParts.slice(matched);

	//f__length - match will not be -values
	for (var k = 0; k < f_length - matched; k++) {
		relative = ['..'].concat(relative);
	}
	return relative.join('/');
};

//recreate it.just for testing
exports.baseName = function (request, ext) {
	var f = splits(request)[2];
	if (ext && f.substr(-1 * ext.length) === ext) {
		f = f.substr(0, f.length - ext.length);
	}
	return f;
};

exports.dirName = function (request) {
	var result = splits(request),
		root = result[0],
		dir = result[1];

	if (!root && !dir) {
		return '.';
	}

	if (dir) {
		// It has a dirname, strip trailing slash
		dir = dir.substr(0, dir.length - 1);
	}

	return root + dir;
};

exports.extName = function (filename) {
	return splits(filename)[3]
};

//exports.path.sep = ['/', '\\']; //psoix / and windows \\

//exports.path.delimiter = [':',';']; //windows it is ; posix :

function isAbsolute(path) {
	if (path.charAt(0) === '/') {
		return true;
	}
	return false;
};

exports.isAbsolutePath = function(path) {
	return isAbsolute(path);
};

//get the root element
exports.getRoot = function (request) {
	var result = splitReg(request);
	return result[0];
};
