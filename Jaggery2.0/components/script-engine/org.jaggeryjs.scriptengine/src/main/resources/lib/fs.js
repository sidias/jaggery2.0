var fs = jaggery.bind('fs');
var path = require('path');
//return if path(@param) exists(may be a file or directory) otherwise false;
//returns path name if exists
exports.statPath = function(path) {
	return new Stats(path);
}

function Stats(path) {
	//if no such path exists in the file system java returns false;
	this.isPathExists = fs.statPath(path);
}

/*
* recommends to check if statPath is not null before use
* */
Stats.prototype.isDirectory = function() {
	if(this.isPathExists) {
		var isDir = path.extName(path.baseName(this.isPathExists));
		if(!isDir) {
			return true;
		}
	}
	//if no file/folder exists return null
	return false;
}

Stats.prototype.isRegularFile = function() {

   	if(this.isPathExists != null) {
		var isRegularFile = path.extName(path.baseName(this.isPathExists));
		if(isRegularFile) {
			return true;
		}
	}
	return false;
}

//guarantee to return source content. param(@path) must be abs path.
exports.readFile = function(path){
	var fileContent = fs.readFile(path);
	return fileContent;

}