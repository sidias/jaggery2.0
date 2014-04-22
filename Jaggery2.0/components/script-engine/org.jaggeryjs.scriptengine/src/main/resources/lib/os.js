var binding = jaggery.bind('os');

exports.endianness = binding.endian;
exports.hostname = binding.hostname;
exports.uptime = binding.uptime;
exports.type = binding.OSType;
exports.release = binding.OSRlease;
exports.platform = jaggery.platform;
exports.tmpdir = binding.tmpdir;
exports.arch = jaggery.arch;

//to implement
//exports.totoalmem, exports.freemem,exports.cpu

