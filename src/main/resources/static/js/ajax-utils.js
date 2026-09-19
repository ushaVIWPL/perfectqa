/**
 * AJAX Utility Functions - Prevents page reloads
 * Include this file and SweetAlert2 in your pages
 */

// Generic AJAX form submission
function submitFormAjax(formId, successMessage, redirectUrl) {
    const form = document.getElementById(formId);
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(this);
        const action = this.action || this.dataset.action;
        const method = this.method || 'POST';
        
        // Show loading
        Swal.fire({
            title: 'Processing...',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
        
        fetch(action, {
            method: method,
            body: formData
        })
        .then(response => {
            if (response.redirected) {
                // If server redirects, follow it
                if (window.htmx) {
                    htmx.ajax('GET', response.url, {target:'body', swap:'innerHTML transition:true'});
                } else {
                    window.location.href = response.url;
                }
                return null;
            }
            return response.json().catch(() => ({ success: true }));
        })
        .then(data => {
            if (data === null) return; // Already redirected
            
            if (data.success !== false) {
                Swal.fire({
                    icon: 'success',
                    title: 'Success!',
                    text: successMessage || data.message || 'Operation completed successfully',
                    timer: 2000,
                    showConfirmButton: false
                }).then(() => {
                    const targetUrl = redirectUrl || data.redirectUrl;
                    if (targetUrl) {
                        if (window.htmx) {
                            htmx.ajax('GET', targetUrl, {target:'body', swap:'innerHTML transition:true'});
                        } else {
                            window.location.href = targetUrl;
                        }
                    } else {
                        if (window.htmx) {
                            htmx.ajax('GET', window.location.href, {target:'body', swap:'innerHTML transition:true'});
                        } else {
                            location.reload();
                        }
                    }
                });
            } else {
                Swal.fire('Error', data.message || 'Operation failed', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire('Error', 'Something went wrong. Please try again.', 'error');
        });
    });
}

// Generic delete with confirmation
function deleteWithConfirmation(url, itemName, redirectUrl) {
    Swal.fire({
        title: 'Delete ' + (itemName || 'Item') + '?',
        text: 'This action cannot be undone!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if (result.isConfirmed) {
            Swal.fire({
                title: 'Deleting...',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });
            
            fetch(url, { method: 'POST' })
            .then(response => response.json().catch(() => ({ success: true })))
            .then(data => {
                if (data.success !== false) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Deleted!',
                        text: (itemName || 'Item') + ' has been deleted.',
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => {
                        if (redirectUrl) {
                            if (window.htmx) {
                                htmx.ajax('GET', redirectUrl, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                window.location.href = redirectUrl;
                            }
                        } else {
                            if (window.htmx) {
                                htmx.ajax('GET', window.location.href, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                location.reload();
                            }
                        }
                    });
                } else {
                    Swal.fire('Error', data.message || 'Delete failed', 'error');
                }
            })
            .catch(error => {
                Swal.fire('Error', 'Something went wrong', 'error');
            });
        }
    });
}

// Generic action with confirmation
function actionWithConfirmation(url, title, text, confirmText, successMessage, redirectUrl) {
    Swal.fire({
        title: title || 'Confirm Action',
        text: text || 'Are you sure?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#0d6efd',
        cancelButtonColor: '#6c757d',
        confirmButtonText: confirmText || 'Yes'
    }).then((result) => {
        if (result.isConfirmed) {
            Swal.fire({
                title: 'Processing...',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });
            
            fetch(url, { method: 'POST' })
            .then(response => response.json().catch(() => ({ success: true })))
            .then(data => {
                if (data.success !== false) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Success!',
                        text: successMessage || data.message || 'Operation completed',
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => {
                        if (redirectUrl) {
                            if (window.htmx) {
                                htmx.ajax('GET', redirectUrl, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                window.location.href = redirectUrl;
                            }
                        } else {
                            if (window.htmx) {
                                htmx.ajax('GET', window.location.href, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                location.reload();
                            }
                        }
                    });
                } else {
                    Swal.fire('Error', data.message || 'Operation failed', 'error');
                }
            })
            .catch(error => {
                Swal.fire('Error', 'Something went wrong', 'error');
            });
        }
    });
}

// Show success toast
function showSuccess(message, timer) {
    Swal.fire({
        icon: 'success',
        title: 'Success!',
        text: message,
        timer: timer || 2000,
        showConfirmButton: false,
        toast: true,
        position: 'top-end'
    });
}

// Show error toast
function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Error',
        text: message,
        toast: true,
        position: 'top-end',
        timer: 3000,
        showConfirmButton: false
    });
}

// Show loading
function showLoading(message) {
    Swal.fire({
        title: message || 'Loading...',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });
}

// Hide loading
function hideLoading() {
    Swal.close();
}

// AJAX GET request
function ajaxGet(url, callback) {
    fetch(url)
    .then(response => response.json())
    .then(data => callback(null, data))
    .catch(error => callback(error, null));
}

// AJAX POST request
function ajaxPost(url, data, callback) {
    const formData = data instanceof FormData ? data : JSON.stringify(data);
    const headers = data instanceof FormData ? {} : { 'Content-Type': 'application/json' };
    
    fetch(url, {
        method: 'POST',
        body: formData,
        headers: headers
    })
    .then(response => response.json())
    .then(data => callback(null, data))
    .catch(error => callback(error, null));
}

// Initialize all forms with data-ajax attribute
document.addEventListener('DOMContentLoaded', function() {
    // Auto-initialize forms with data-ajax="true"
    document.querySelectorAll('form[data-ajax="true"]').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const action = this.action;
            const successMsg = this.dataset.successMessage || 'Saved successfully!';
            const redirectUrl = this.dataset.redirect;
            
            showLoading('Saving...');
            
            fetch(action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.redirected) {
                    if (window.htmx) {
                        htmx.ajax('GET', response.url, {target:'body', swap:'innerHTML transition:true'});
                    } else {
                        window.location.href = response.url;
                    }
                    return null;
                }
                return response.json().catch(() => ({ success: true }));
            })
            .then(data => {
                if (data === null) return;
                
                if (data.success !== false) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Success!',
                        text: successMsg,
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => {
                        if (redirectUrl) {
                            if (window.htmx) {
                                htmx.ajax('GET', redirectUrl, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                window.location.href = redirectUrl;
                            }
                        } else {
                            if (window.htmx) {
                                htmx.ajax('GET', window.location.href, {target:'body', swap:'innerHTML transition:true'});
                            } else {
                                location.reload();
                            }
                        }
                    });
                } else {
                    Swal.fire('Error', data.message || 'Operation failed', 'error');
                }
            })
            .catch(error => {
                Swal.fire('Error', 'Something went wrong', 'error');
            });
        });
    });
    
    // Auto-initialize delete buttons with data-delete attribute
    document.querySelectorAll('[data-delete]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const url = this.dataset.delete;
            const itemName = this.dataset.itemName || 'Item';
            const redirect = this.dataset.redirect;
            deleteWithConfirmation(url, itemName, redirect);
        });
    });
    
    // Auto-initialize action buttons with data-action-confirm attribute
    document.querySelectorAll('[data-action-confirm]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const url = this.dataset.actionConfirm;
            const title = this.dataset.title;
            const text = this.dataset.text;
            const confirmText = this.dataset.confirmText;
            const successMsg = this.dataset.successMessage;
            const redirect = this.dataset.redirect;
            actionWithConfirmation(url, title, text, confirmText, successMsg, redirect);
        });
    });
});

console.log('AJAX Utils loaded successfully with HTMX transitions');
