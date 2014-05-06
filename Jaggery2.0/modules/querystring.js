var util = require('util');
var queryString = exports;

function hasOwnProperty(obj, prop) {
	return Object.prototype.hasOwnProperty.call(obj, prop);
}

function encodes(key) {
	return encodeURIComponent(key);
}

function decodes(encodedURI) {
	return decodeURIComponent(encodedURI);
};

/*Serializes an object containing name:value pairs into a query string:
@param  fields    object contain URI component as key value pairs.
@param  separator separator of URIComponent                  [optional, default - &]
@param  assigner  assigner of key value pairURI Component    [optional, default - =]
*
* */
queryString.stringify = function(fields, sep, assign) {
	if(!util.isObject(fields)) {
		throw new Error('fields must be a Object');
	}

	sep = sep || '&';
	assign = assign || '=';
	var keyComp;

	//if not characters are encoded as %HH hex representation and return
	return Object.keys(fields).map(function(key){
		//encode key-value components
		keyComp = encodes(primitiveStringify(key)) + assign;

		if(util.isArray(fields[key])) {
			return fields[key].map(function(key) {
				return keyComp + encodes(primitiveStringify(key));
			}).join(sep);
		}
		return keyComp + encodes(primitiveStringify(fields[key]));
	}).join(sep);
};

/*Deserialize a query string to an object
@param   querystring   querystring as a string to deserialize
@param   separator     separator of URIComponent      [optional, default - &]
@param   assign        assigner of key value pair     [optional, default - =]
* */
var decodedURI;
//return a object contain key-value pairs of url value;
queryString.parse = function(querystring, sep, assign) {
 	if(!util.isString(querystring)) {
		throw new Error('querystring must be a string')
	}

	sep = sep || '&';
	assign = assign || '=';
    var obj = {},
        compoArray = [];

	decodedURI = decodes(querystring);

    decodedURI.split(sep).forEach(function(comp) {
        print('comp is ---'+comp)
        comp.split(assign).some(function(element, index, array) {
            print('arr values are ----> '+ element)
            if(hasOwnProperty(obj, element.toString())) {

                compoArray.push(obj[element]);
                compoArray.push(array[1]);

                obj[element] = compoArray;
            } else {
                Object.defineProperty(obj, element, {
                    enumerable:true,
                    writable:true,
                    value:array[1]
                });
            }
            return true;
        });
    });
    return obj;
}

function primitiveStringify(val) {
	if(util.isString(val) || util.isBoolean(val) || isFinite(val)) {
		return val;
	}
	return '';
}

//to do
queryString.escape = {};

queryString.unescape = {};
