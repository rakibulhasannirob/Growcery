// Common JavaScript for Growcery Application

// Document Ready Function
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Auto dismiss alerts after 5 seconds
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
    
    // Cart quantity update
    setupCartQuantityButtons();
    
    // Product category filter
    setupCategoryFilter();
    
    // Product search functionality
    setupProductSearch();
});

// Setup cart quantity increment/decrement buttons
// Setup cart quantity increment/decrement buttons
// Note: This is now overridden in cart.html with a version that auto-submits the form
function setupCartQuantityButtons() {
    const decrementButtons = document.querySelectorAll('.quantity-decrement');
    const incrementButtons = document.querySelectorAll('.quantity-increment');
    
    // Skip if we're on the cart page (will be handled by cart-specific JS)
    if (window.location.pathname.includes('/cart')) {
        return;
    }
    
    decrementButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.nextElementSibling;
            const value = parseInt(input.value);
            if (value > 1) {
                input.value = value - 1;
            }
        });
    });
    
    incrementButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.previousElementSibling;
            const value = parseInt(input.value);
            const max = parseInt(input.getAttribute('max') || 99);
            if (value < max) {
                input.value = value + 1;
            }
        });
    });
}

// Setup category filter for products page
function setupCategoryFilter() {
    const categorySelect = document.getElementById('categoryFilter');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            const category = this.value;
            if (category) {
                window.location.href = '/products?category=' + category;
            } else {
                window.location.href = '/products';
            }
        });
    }
}

// Setup product search functionality
function setupProductSearch() {
    const searchInput = document.getElementById('productSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const productCards = document.querySelectorAll('.product-card-container');
            
            productCards.forEach(card => {
                const productName = card.querySelector('.product-title').textContent.toLowerCase();
                const productDesc = card.querySelector('.product-description')?.textContent.toLowerCase() || '';
                
                if (productName.includes(searchTerm) || productDesc.includes(searchTerm)) {
                    card.style.display = '';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    }
}

// Confirmation dialog for delete actions
function confirmDelete(message, formId) {
    if (confirm(message || 'Are you sure you want to delete this item?')) {
        document.getElementById(formId).submit();
    }
    return false;
}

// Format price as currency
function formatPrice(price) {
    return new Intl.NumberFormat('en-US', { 
        style: 'currency', 
        currency: 'USD' 
    }).format(price);
}

// Add to cart form submission with validation
function addToCart(formId, productName) {
    const form = document.getElementById(formId);
    const quantityInput = form.querySelector('input[name="quantity"]');
    const quantity = parseInt(quantityInput.value);
    const maxStock = parseInt(quantityInput.getAttribute('max') || 0);
    
    if (quantity > maxStock) {
        alert(`Sorry, only ${maxStock} units of "${productName}" are available.`);
        return false;
    }
    
    form.submit();
    return true;
}