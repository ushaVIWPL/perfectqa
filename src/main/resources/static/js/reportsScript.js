$(document).ready(function () {
	// Initialize the date picker
	$('#month-picker').datepicker({
		format: 'MM yyyy',
		startView: 'months',
		minViewMode: 'months',
		autoclose: true,
		startDate: '01-2025',
		endDate: '12-2025'
	});

	// Initialize the select picker
	$('.selectpicker').selectpicker({
		dropupAuto: false
	});
});
function clearDateError() {
	var dateError = document.getElementById("dateInputError");
	if(dateError != null) {
		dateError.style.display = "none";
	}	
}

function clearReportTypeError() {
	var fileError = document.getElementById("reportTypeInputError");
	if(fileError != null) {
		fileError.style.display = "none";
	}
}

function clearHeadCountInputError() {
	var headCountInputError = document.getElementById("headCountInputError");
	if(headCountInputError != null) {
		headCountInputError.style.display = "none";
	}
}