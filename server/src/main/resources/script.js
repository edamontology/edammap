
var check = function(id, method, api) {
	var input = document.getElementById(id);
	var output = document.getElementById(id + '-output');
	if (input.value === '') {
		input.classList.remove('input-good');
		input.classList.remove('input-bad');
		output.innerHTML = '';
		return;
	}
	input.readonly = true;
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			input.classList.remove('input-medium');
			output.classList.remove('output-medium');
			if (request.status == 200) {
				input.classList.add('input-good');
				output.classList.add('output-good');
			} else {
				input.classList.add('input-bad');
				output.classList.add('output-bad');
			}
			output.innerHTML = '<span>' + escape(request.responseText) + '</span>';
			input.readonly = false;
		}
	}
	request.open(method, '/api/' + api, true);
	request.send(input.value);
	input.classList.remove('input-good');
	output.classList.remove('output-good');
	input.classList.remove('input-bad');
	output.classList.remove('output-bad');
	input.classList.add('input-medium');
	output.classList.add('output-medium');
	output.innerHTML = '<span>Working...</span>';
};


var entities = {
	'&': '&amp;',
	'<': '&lt;',
	'>': '&gt;',
	'\n': '</span><br><span>'
};
var escape = function(text) {
	return String(text).replace(/[&<>\n]/g, function(s) { return entities[s] });
};
