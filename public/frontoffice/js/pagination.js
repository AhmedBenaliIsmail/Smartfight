class SmartPagination {
    constructor(options) {
        this.containerSelector = options.containerSelector;
        this.itemSelector = options.itemSelector;
        this.paginationContainerId = options.paginationContainerId;
        this.itemsPerPage = options.itemsPerPage || 6;
        this.currentPage = 1;

        this.grid = document.querySelector(this.containerSelector);
        if (!this.grid) return;

        this.allItems = Array.from(this.grid.querySelectorAll(this.itemSelector));
        this.paginationContainer = document.getElementById(this.paginationContainerId);
        if (!this.paginationContainer) return;

        this.filteredItems = this.allItems;
        this.init();
    }

    init() {
        this.renderPage(1);
    }

    setFilteredItems(items) {
        this.filteredItems = items;
        this.renderPage(1);
    }

    renderPage(page) {
        const totalPages = Math.ceil(this.filteredItems.length / this.itemsPerPage) || 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        this.currentPage = page;

        // Hide all original items
        this.allItems.forEach(item => { item.style.display = 'none'; });

        // Show only items for current page from filtered list
        const start = (page - 1) * this.itemsPerPage;
        const end = start + this.itemsPerPage;
        const pageItems = this.filteredItems.slice(start, end);
        
        pageItems.forEach(item => { 
            item.style.display = ''; 
            this.grid.appendChild(item); // Ensure DOM order matches sorted/filtered list
        });

        this.renderControls(totalPages);

        // Handle Empty State
        const existingEmpty = this.grid.querySelector('.sf-pagination-empty');
        if (this.filteredItems.length === 0) {
            if (!existingEmpty) {
                const empty = document.createElement('div');
                empty.className = 'col-12 text-center py-5 text-muted sf-pagination-empty';
                empty.innerHTML = `
                    <i class="fa-solid fa-user-secret fa-4x mb-3 opacity-25"></i>
                    <h3 class="text-white fw-bold">NO INTELLIGENCE FOUND</h3>
                    <p>No warriors match the current tactical filters.</p>
                `;
                this.grid.appendChild(empty);
            }
        } else if (existingEmpty) {
            existingEmpty.remove();
        }
    }

    renderControls(totalPages) {
        this.paginationContainer.innerHTML = '';
        if (totalPages <= 1 && this.filteredItems.length > 0) { 
            this.paginationContainer.style.display = 'none'; 
            return; 
        } else if (this.filteredItems.length === 0) {
            this.paginationContainer.style.display = 'none'; 
            return;
        } else { 
            this.paginationContainer.style.display = 'flex'; 
        }

        const prevBtn = document.createElement('button');
        prevBtn.className = 'page-btn';
        prevBtn.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
        prevBtn.disabled = this.currentPage === 1;
        prevBtn.onclick = () => this.renderPage(this.currentPage - 1);
        this.paginationContainer.appendChild(prevBtn);

        for (let i = 1; i <= totalPages; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.className = 'page-btn ' + (i === this.currentPage ? 'active' : '');
            pageBtn.textContent = i;
            pageBtn.onclick = () => this.renderPage(i);
            this.paginationContainer.appendChild(pageBtn);
        }

        const nextBtn = document.createElement('button');
        nextBtn.className = 'page-btn';
        nextBtn.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
        nextBtn.disabled = this.currentPage === totalPages;
        nextBtn.onclick = () => this.renderPage(this.currentPage + 1);
        this.paginationContainer.appendChild(nextBtn);

        const pageInfo = document.createElement('div');
        pageInfo.className = 'page-info';
        pageInfo.textContent = `Page ${this.currentPage} of ${totalPages}`;
        this.paginationContainer.appendChild(pageInfo);
    }
}

window.SmartPagination = SmartPagination;
