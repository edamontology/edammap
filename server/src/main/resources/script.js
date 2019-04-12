
var entities = {
	'&': '&amp;',
	'<': '&lt;',
	'>': '&gt;',
};
var escape = function(text) {
	return String(text).replace(/[&<>]/g, function(s) { return entities[s] });
};

var get_params = function(selector) {
	var inputs = document.querySelectorAll(selector);
	var params = {};
	for (var i = 0; i < inputs.length; ++i) {
		var input = inputs[i];
		if (input.tagName == 'SELECT') {
			var options = input.getElementsByTagName('option');
			var differ = false;
			var selected = [];
			for (var j = 0; j < options.length; ++j) {
				var option = options[j];
				if (option.selected && option.dataset.default != 'selected' || !option.selected && option.dataset.default == 'selected') {
					if (input.multiple) {
						differ = true;
					} else {
						params[input.id] = input.value;
						break;
					}
				}
				if (input.multiple && option.selected) {
					selected.push(option.value);
				}
			}
			if (differ) {
				params[input.id] = selected;
			}
		} else if (input.type == 'checkbox') {
			if (input.checked && input.dataset.default != 'true' || !input.checked && input.dataset.default == 'true') {
				params[input.id] = input.checked;
			}
		} else if (input.value != input.dataset.default) {
			params[input.id] = input.value;
		}
	}
	return params;
}

var check = function(id, endpoint) {
	var input = document.getElementById(id);
	var output = document.getElementById(id + '-output');
	if (input.value === '') {
		input.classList.remove('input-good');
		input.classList.remove('input-bad');
		output.innerHTML = '';
		return;
	}
	input.readOnly = true;
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4 && request.status > 0) {
			input.classList.remove('input-medium');
			output.classList.remove('output-medium');
			var json = JSON.parse(request.responseText);
			var output_html = '';
			if (json.success) {
				input.classList.add('input-good');
				output.classList.add('output-good');
				var values = json[id];
				for (var key in values) {
					var value = values[key];
					if (id != 'annotations') {
						output_html += '<span';
						if (value.status != 'final' && value.status != 'totally final') {
							if (value.status != 'non-final') {
								output_html += ' class="output-bad"';
							} else {
								output_html += ' class="output-medium"';
							}
						}
						output_html += '>';
						if (value.id === Object(value.id)) {
							output_html += '[' + escape([value.id.pmid, value.id.pmcid, value.id.doi].filter(String).join(', ')) + ']';
						} else {
							output_html += escape(value.id);
						}
						output_html += ' : ' + escape(value.status) + '</span><br>';
					} else {
						output_html += '<span>' + escape(value.uri) + ' : ' + escape(value.label) + '</span><br>';
					}
				}
			} else {
				input.classList.add('input-bad');
				output.classList.add('output-bad');
				if (json.message) {
					output_html += '<span>' + escape(json.message) + '</span>';
				} else {
					output_html += '<span>Internal Server Error</span>';
				}
				if (json.time) {
					output_html += '<br><span>' + escape(json.time) + '</span>';
				}
			}
			output.innerHTML = output_html;
			input.readOnly = false;
		}
	}
	var params = {};
	if (id != 'annotations') {
		params = get_params('#tab-title-fetcherArgs ~ .tab-content > .param:not(.param-disabled) input:not([type=hidden]), #tab-title-fetcherArgs ~ .tab-content > .param select');
	}
	params[id] = input.value;
	request.open('POST', endpoint, true);
	request.setRequestHeader('Content-Type', 'application/json');
	request.send(JSON.stringify(params));
	input.classList.remove('input-good');
	output.classList.remove('output-good');
	input.classList.remove('input-bad');
	output.classList.remove('output-bad');
	input.classList.add('input-medium');
	output.classList.add('output-medium');
	output.innerHTML = '<span>Working...</span>';
};

var param = function() {
	var params = get_params('.param:not(.param-disabled) input:not([type=hidden]), .param select');
	var href = window.location.pathname;
	var i = 0;
	var i_max = 0;
	for (var key in params) {
		++i_max;
	}
	if (i_max > 0) {
		href += '?';
	}
	for (var key in params) {
		var value = params[key];
		if (Array.isArray(value) && value.length > 0) {
			for (var j = 0; j < value.length; ++j) {
				href += key + '=' + value[j];
				if (j < value.length - 1) {
					href += '&';
				}
			}
		} else {
			href += key + '=' + value;
		}
		if (i < i_max - 1) {
			href += '&';
		}
		++i;
	}
	href += window.location.hash;
	history.replaceState(null, '', href);
}
