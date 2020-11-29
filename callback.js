var crypto = require('crypto');

function base64decode(data) {
	 while (data.length % 4 !== 0){
                data += '=';
          }
data = data.replace(/-/g, '+').replace(/_/g, '/');
return new Buffer(data, 'base64').toString('utf-8');
}

function parseSignedRequest(signedRequest, secret) {
var encoded_data = signed_request.split('.', 2);
// decode the data
var sig = encoded_data[0];
var json = base64decode(encoded_data[1]);
var data = JSON.parse(json);
if (!data.algorithm || data.algorithm.toUpperCase() != 'HMAC-SHA256') {
throw Error('Unknown algorithm: ' + data.algorithm + '. Expected HMAC-SHA256');
}
var expected_sig = crypto.createHmac('sha256', secret).update(encoded_data[1]).digest('base64').replace(/\+/g, '-').replace(/\//g, '_').replace('=', '');
if (sig !== expected_sig) {
throw Error('Invalid signature: ' + sig + '. Expected ' + expected_sig);
}
return data;
}