
const projectname = "/ShortUrlProject"

function copyToClipboard(text, element) {
    navigator.clipboard.writeText(text).then(function() {
        const originalClass = element.className;
        element.className = 'fa-solid fa-check copy-icon';
        element.style.color = '#198754';
        
        const originalTitle = element.title;
        element.title = 'Copied!';
        
        setTimeout(function() {
            element.className = originalClass;
            element.style.color = '';
            element.title = originalTitle;
        }, 2000);
    }).catch(function(err) {
        console.error('Could not copy text: ', err);
        showToast('Failed to copy to clipboard', 'danger');
    });
}

// Function to show toast messages
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toast-container');
    
    const toast = document.createElement('div');
    toast.className = `toast`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="toast-header bg-${type} text-white">
            <strong class="me-auto">${type === 'success' ? 'Success' : 'Error'}</strong>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;

    toastContainer.appendChild(toast);
    
    const bsToast = new bootstrap.Toast(toast, {
        autohide: true,
        delay: 3000
    });
    bsToast.show();
    
    setTimeout(() => toast.remove(), 3000);
}

// URL submit code
document.getElementById('urlForm').addEventListener('submit', function(event) {
    event.preventDefault();
    
    const urlInput = document.getElementById('urlInput');
    const submitBtn = document.getElementById('submitBtn');

    if (!urlInput.checkValidity()) {
        showToast('Please enter a valid URL (must start with http:// or https://)', 'danger');
        return;
    }
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...';
    
    const formData = new URLSearchParams();
    formData.append("url", urlInput.value);

    
    fetch(`${projectname}/api/addurl`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Shorten';
        
        if (data.success) {
            showToast('URL shortened successfully!', 'success');
            urlInput.value = '';

            const tableBody = document.getElementById('urlTableBody');
            const existingRows = tableBody.querySelectorAll('tr:not(.empty-state-row)');
            const rowCount = existingRows.length + 1;

            const emptyStateRow = tableBody.querySelector('.empty-state-row');
            if (emptyStateRow) emptyStateRow.remove();

            const newRow = document.createElement('tr');
            newRow.innerHTML = `
                <td scope="row">${rowCount}</td>
                <td>
                    <a href="${data.shortUrl}" class="short-url" target="_blank">
                        ${data.shortUrl}
                    </a>
                    <i class="fa-regular fa-copy copy-icon" 
                       onclick="copyToClipboard('${data.shortUrl}', this)" 
                       title="Copy to clipboard"></i>
                </td>
                <td title="${data.targetUrl}">
                    <a href="${data.targetUrl}" class="target-url" target="_blank">
                        ${data.targetUrl.length > 6 ? 
                          data.targetUrl.substring(0, 6) + '...' : 
                          data.targetUrl}
                    </a>
                </td>
                <td>0</td>
                <td>
                    <button class="btn btn-sm btn-success edit-btn"
                        data-bs-toggle="modal"
                        data-bs-target="#editModal"
                        data-url-id="${data.id}"
                        data-url="${data.targetUrl}">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button class="btn btn-sm btn-danger delete-btn" 
                        data-url-id="${data.id}">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;

            tableBody.prepend(newRow); 

            setupEditButtonListeners();
            setupDeleteButtonListeners();
        } else {
        	console.log(data);
            showToast(data.message || 'Error shortening URL', 'danger');
        }
    })
    .catch(error => {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Shorten';
        showToast('Server error: ' + error, 'danger');
        console.error('Error:', error);
    });
});

//listeners for edit buttons
function setupEditButtonListeners() {
    document.querySelectorAll('.edit-btn').forEach(button => {
        button.addEventListener('click', function() {
            const urlId = this.getAttribute('data-url-id');
            const url = this.getAttribute('data-url');
            
            document.getElementById('editUrlId').value = urlId;
            document.getElementById('editUrlInput').value = url;
        });
    });
}

//listeners for delete buttons
function setupDeleteButtonListeners() {
    document.querySelectorAll('.delete-btn').forEach(button => {
        button.addEventListener('click', function() {
            const urlId = this.getAttribute('data-url-id');
            
            const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
            document.getElementById('confirmDeleteBtn').setAttribute('data-url-id', urlId);
            deleteModal.show();
        });
    });
}

// Edit form code
document.getElementById('editForm').addEventListener('submit', function(event) {
    event.preventDefault();
    
    const urlId = document.getElementById('editUrlId').value;
    const urlValue = document.getElementById('editUrlInput').value;

    
    const formData = new URLSearchParams();
    formData.append('id', urlId);
    formData.append('url', urlValue);
    
    fetch(`${projectname}/api/updateurl`, {
        method: 'POST',
		headers: {
			"Content-Type": "application/x-www-form-urlencoded",
		},
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        const editModal = bootstrap.Modal.getInstance(document.getElementById('editModal'));
        editModal.hide();
        
        if (data.success) {
            showToast('URL updated successfully', 'success');
           
			setTimeout(()=>{location.reload()},2000)
        } else {
            showToast(data.message || 'Error updating URL', 'danger');
        }
    })
    .catch(error => {
        showToast('Server error: ' + error, 'danger');
        console.error('Error:', error);
    });
});

// Delete confirmation code
document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
    const urlId = this.getAttribute('data-url-id');
    
    const formData = new URLSearchParams();
    formData.append('id', urlId);
    
    fetch(`${projectname}/api/deleteurl`, {
		  method: "POST",
	      headers: {
	        "Content-Type": "application/x-www-form-urlencoded",
	      },
	      body: formData,
    })
    .then(response => {
		
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        const deleteModal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
        deleteModal.hide();
        
        if (data.success) {
            showToast('URL deleted successfully', 'success');
            setTimeout(()=>{location.reload()},1000)
            
        } else {
            showToast(data.message || 'Error deleting URL', 'danger');
        }
    })
    .catch(error => {
        showToast('Server error: ' + error, 'danger');
        console.error('Error:', error);
    });
});

// Initialize event listeners when page loads
window.addEventListener('DOMContentLoaded',()=> {
    setupEditButtonListeners();
    setupDeleteButtonListeners();
});