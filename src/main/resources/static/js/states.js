// ✅ Attach globally so Thymeleaf fragments can access
window.editstates = function (button) {
    const row = button.closest('tr');
    const editableCells = row.querySelectorAll('.editable');

    editableCells.forEach(cell => {
        const currentValue = cell.textContent.trim();
        cell.innerHTML = `<input type="text" class="form-control form-control-sm" value="${currentValue}">`;
    });

    button.style.display = 'none';
    row.querySelector('.save-btn').style.display = 'inline-block';
};

window.saveRow = function (button) {
    const row = button.closest('tr');
    const inputs = row.querySelectorAll('input');
    const stateCode = button.getAttribute('data-code');

    const updatedState = {
        country: inputs[0].value,
        countryCode: inputs[1].value,
        stateSubdivisionName: inputs[2].value
    };

    axios.put(`/states/update/${stateCode}`, updatedState)
        .then(response => {
            alert(response.data);
            row.querySelectorAll('.editable').forEach((cell, index) => {
                cell.textContent = Object.values(updatedState)[index];
            });

            button.style.display = 'none';
            row.querySelector('.edit-btn').style.display = 'inline-block';
        })
        .catch(error => {
            alert("Update failed: " + (error.response ? error.response.data : error.message));
        });
};
