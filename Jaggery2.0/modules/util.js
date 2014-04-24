//all utilities in jaggery js

//behave as java.String.format() - right num of arguments must be supplied to work
//find all existences of %s,%j and %d
var regExp = /%[sdj%]/g;
exports.format = function() {
    var args =  Array.prototype.slice.call(arguments);
    var str = String(args[0]);

      if (!isString(str)) {
        var objects = [];
        for (var i = 0; i < arguments.length; i++) {
          objects.push(inspect(arguments[i]));
        }
        return objects.join(' ');
      }

    var len = args.length;
    var k = 0;

    str = str.replace(regExp,function(val) {
        if(val === '%%'){return '%'}

        switch(val){
            case '%s':
                k++;
                if(args[k]){
                    return String(args[k]);
                    break;
                }
                throw Error('Invalid number of arguments');

            case '%d':
                k++;
                if(args[k]){
                    return Number(args[k]);
                    break;
                }
                throw Error('Invalid number of arguments');

            case '%j':
                k++;
                if(args[k]){
                    try{
                        return JSON.stringify(args[k]);
                    }catch(e){
                        return '[faild]';          //correct this
                    }
                    break;
                }
                throw Error('Invalid number of arguments');
        }
    });
    return str;
};

//return true if value was been declared
exports.isDefine = function(e) {  //------------ //check this is correct or not
    return typeof e !== 'undefined';
};

exports.isObject = function(e) {
    return typeof e === 'object';
};

exports.isError = function(err) {
    return (isObject(err) && classType('Error',err));
};

//check given object is in a given class
//in typeoof operator return null type as object(avoided);

//this approach is the best it will give
//1.2 === Number and var k = new Number(1.2) both class as Number
classType = function(type, obj) {
    var cs = Object.prototype.toString.call(obj);
    return obj !== undefined && obj !== null && cs === type;
};

exports.isArray = function(e) {
    return Array.isArray(e);
};

exports.isBoolean = function(bool) {
    return classType('[object Boolean]', bool);
};

exports.isDate = function(date) {
    return isObject(date) && classType('[object Date]', date);
};

exports.isFunction = function(func) {
    return typeof func === 'function';
};

exports.isNumber = function(num) {
    return classType('[object Number]', num);
};

exports.isRegExp = function(re) {
    return isObject(re) && classType('[object RegExp]', re);
};

exports.isString = function(str) {
    return classType('[object String]', str);
};

exports.isNull = function(obj){
    return obj === null;
};

exports.isPrimitive = function(arg) {
    return arg === null ||
            isBoolean(arg) ||
            isNumber(arg) ||
            isString(arg) ||
            isDefined(arg) ;
};

function tik(time){
    return (time < 10)? '0'+time.toString() : time.toString();
}

var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sep', 'Oct', 'Nov','Dec'];
var Day = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'];

//format date month hour:min:second
exports.timestamp = function() {
    var d = new Date();
    return [tik(d.getDate()),
         months[d.getMonth()],
         [tik(d.getHours()),
         tik(d.getMinutes()),
         tik(d.getSeconds())].join(':')].join(' ');
};

exports.timeLog = function(msg) {
    print(timestamp() + ' - ' + msg);
};

//avoid writing code for inheritance
exports.inherit = function (obj){

//must provide as obj,{param:___,body____}
    	if(arguments.length>1){
            var functionDetail = arguments[1]
    		var func = new Function(functionDetail.param,functionDetail.body);
    		func.prototype = obj;
    		func.prototype.constructor = func;
    		return new func();
    	}

    	function f(){};
    	f.prototype = obj;
    	return new f();
};

//functions cannot stringify directly.need to convert into string first
//seal the object before serialize the object using Object.preventExtensions(someObject); or with Object.seal(someObjet)
/*
function serialize(values){

    //if(Object.isSealed(values)){
        return JSON.stringify(values, function (key, value) {
            return (typeof value === 'function') ? value.toString() : value;
        });
    //}
};

//deserializing an object.functions are passed as a string then process and send out
function deserializeJSON(filterString){

    return JSON.parse(filterString, function (key, value) {

        if (value && typeof value === "string" && value.substr(0,8) === "function" ) {

            var startBody = value.indexOf('{') + 1;
            var endBody = value.lastIndexOf('}');
            var startArgs = value.indexOf('(') + 1;
            var endArgs = value.indexOf(')');

            //return string as function and new Function(,);
            if(!startBody || endBody == -1 || !startArgs || endArgs == -1){
                return value;
            }
            return (new Function(value.substring(startArgs, endArgs), value.substring(startBody, endBody)));
        }
        return value;
    }) ;
}; */



