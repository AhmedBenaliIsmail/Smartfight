/**
 * Global Live Search Filter
 * 
 * Usage:
 * 1. Add class 'live-search-input' to your search input field.
 * 2. Add class 'searchable-item' to the elements you want to filter (e.g., table rows, cards).
 * 3. (Optional) Add 'data-search-target="[selector]"' to the input if you want to scope the search to a specific container.
 */

document.addEventListener('DOMContentLoaded', () => {
    const searchInputs = document.querySelectorAll('.live-search-input');

    searchInputs.forEach(input => {
        input.addEventListener('input', function() {
            const query = this.value.toLowerCase().trim();
            const targetSelector = this.getAttribute('data-search-target');
            
            let searchableItems;
            
            if (targetSelector) {
                const container = document.querySelector(targetSelector);
                if (container) {
                    searchableItems = container.querySelectorAll('.searchable-item');
                } else {
                    searchableItems = [];
                }
            } else {
                searchableItems = document.querySelectorAll('.searchable-item');
            }

            searchableItems.forEach(item => {
                const textContent = item.textContent.toLowerCase();
                if (textContent.includes(query)) {
                    item.style.display = ''; // Restore original display
                } else {
                    item.style.display = 'none'; // Hide
                }
            });
        });
    });
});
