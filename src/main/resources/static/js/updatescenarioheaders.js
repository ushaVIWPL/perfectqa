        function enableEdit(rowId) {
            let row = document.getElementById("row-" + rowId);
            let inputs = row.querySelectorAll("td[data-editable]");
            inputs.forEach(td => {
                let currentText = td.innerText;
                td.innerHTML = `<input type="text" class="form-control" value="${currentText}">`;
            });
            row.querySelector(".edit-btn").classList.add("d-none");
            row.querySelector(".save-btn").classList.remove("d-none");
        }

        function saveRow(rowId, id) {
            let row = document.getElementById("row-" + rowId);
            let inputs = row.querySelectorAll("td[data-editable] input");
            let updatedData = Array.from(inputs).map(input => input.value);

            fetch('/updateScenario/' + id, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    businessScenario: id,
                    scenarioDescription: updatedData[0],
                    workStream: updatedData[1],
                    activity: updatedData[2],
                    responsible: updatedData[3],
                    expectedOutcome: updatedData[4],
                    tcode: updatedData[5]
                })
            }).then(res => {
                if (res.ok) {
                    updatedData.forEach((val, i) => {
                        inputs[i].parentElement.innerHTML = val;
                    });
                    row.querySelector(".edit-btn").classList.remove("d-none");
                    row.querySelector(".save-btn").classList.add("d-none");
                } else {
                    alert("Error saving data");
                }
            });
        }
        
        
        
        
        function validateAndPad(input) {
    let value = input.value;
    // Remove non-digit characters just in case
    value = value.replace(/\D/g, '');

    // Pad to minimum 3 digits
    if (value.length > 0) {
        input.value = value.padStart(3, '0');
    }
}
        
        
        
        
        
        
 