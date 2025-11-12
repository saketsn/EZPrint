document.addEventListener('DOMContentLoaded', () => {
    // --- Get Shop ID from URL Path ---
    const pathParts = window.location.pathname.split('/');
    const shopId = pathParts[pathParts.length - 1];

    // --- Elements ---
    const themeToggle = document.getElementById('theme-toggle');
    const htmlEl = document.documentElement;
    const shopNameEl = document.getElementById('shop-name');
    const printForm = document.getElementById('print-form');
    const submitButton = document.getElementById('submit-btn');
    const fullNameInput = document.getElementById('fullName');
    const usnIdInput = document.getElementById('usnId');
    const phoneNoInput = document.getElementById('phoneNo');
    const emailIdInput = document.getElementById('emailId');
    const idProofInput = document.getElementById('idProof');
    const idProofDropzone = document.querySelector('.id-proof-dropzone');
    const dropZone = document.getElementById('drop-zone');
    const printFilesError = document.getElementById('print-files-error');
    const idProofText = document.getElementById('id-proof-text');
    const fileUploadInput = document.getElementById('file-upload');
    const fileListContainer = document.getElementById('file-list-container');
    const toastEl = document.getElementById('toast');
    
    // --- Modal elements ---
    const confirmationModalEl = document.getElementById('confirmationModal');
    const confirmationModal = new bootstrap.Modal(confirmationModalEl);
    const confirmSubmitBtn = document.getElementById('confirm-submit-btn-modal');

    // --- Pricing elements ---
    const viewPricingBtn = document.getElementById('view-pricing-btn');
    const priceListContainer = document.getElementById('price-list-container');
    const pricingViewDefault = document.getElementById('pricing-view-default');
    
    let uploadedFiles = [];

    // --- Shop Info Management ---
    function displayShopInfo() {
        if (!shopId || isNaN(parseInt(shopId))) {
            if (shopNameEl) shopNameEl.textContent = "Invalid Shop Link";
            if (submitButton) submitButton.disabled = true;
            return;
        }
        fetch(`/api/shops/details/${shopId}`)
            .then(res => res.ok ? res.json() : Promise.reject('Shop not found'))
            .then(data => {
                if (data.success && data.shop) {
                    const shopName = data.shop.shop_name;
                    if (shopNameEl) shopNameEl.textContent = shopName;
                    document.getElementById('summary-shop-name-modal').textContent = shopName;
                } else {
                    if (shopNameEl) shopNameEl.textContent = "Shop Not Found";
                    if (submitButton) submitButton.disabled = true;
                }
            })
            .catch(err => {
                console.error("Error fetching shop details:", err);
                if (shopNameEl) shopNameEl.textContent = "Could Not Load Shop Details";
                if (submitButton) submitButton.disabled = true;
            });
    }
    displayShopInfo();

    // --- Theme Management ---
    if (localStorage.getItem('theme') === 'dark') {
        htmlEl.classList.add('dark');
    } else {
        htmlEl.classList.remove('dark');
    }
    themeToggle.addEventListener('click', () => {
        htmlEl.classList.toggle('dark');
        localStorage.setItem('theme', htmlEl.classList.contains('dark') ? 'dark' : 'light');
    });

    // --- Toast Notification Handler ---
    function showToast(message, type = 'error') {
        toastEl.querySelector('p').textContent = message;
        toastEl.className = 'toast-ui';
        toastEl.classList.add(type, 'show');
        setTimeout(() => toastEl.classList.remove('show'), 3000);
    }

    // --- ID Proof Upload Handler ---
    idProofInput.addEventListener('change', () => {
        if (idProofInput.files.length > 0) {
            idProofText.textContent = idProofInput.files[0].name;
            validateField(idProofInput);
        } else {
            idProofText.innerHTML = `<span class="fw-semibold">Click to upload</span> (JPG, PNG, PDF)`;
        }
    });

    // --- File Drop Zone & Upload Logic ---
    const MAX_FILES = 10;
    const MAX_SIZE_MB = 50;
    const ALLOWED_TYPES = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 'image/jpeg', 'image/png'];

    dropZone.addEventListener('click', () => fileUploadInput.click());
    dropZone.addEventListener('dragover', (e) => { e.preventDefault(); dropZone.classList.add('drag-over'); });
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('drag-over');
        handleFiles(e.dataTransfer.files);
    });
    fileUploadInput.addEventListener('change', () => handleFiles(fileUploadInput.files));

    function handleFiles(files) {
        for (const file of files) {
            if (uploadedFiles.length >= MAX_FILES) { showToast(`Cannot upload more than ${MAX_FILES} files.`); break; }
            if (file.size > MAX_SIZE_MB * 1024 * 1024) { showToast(`File "${file.name}" exceeds the ${MAX_SIZE_MB}MB size limit.`); continue; }
            if (!ALLOWED_TYPES.includes(file.type)) { showToast(`File type for "${file.name}" is not supported.`); continue; }
            const fileId = `file-${Date.now()}-${Math.random()}`;
            const fileData = { id: fileId, file: file, copies: 1, options: { isColor: false, isDuplex: false, orientation: 'portrait', pageRange: 'All' } };
            uploadedFiles.push(fileData);
            renderFileItem(fileData);
        }
        if (uploadedFiles.length > 0) clearFilesError();
        fileUploadInput.value = '';
    }

    function renderFileItem(fileData) {
        const container = document.createElement('div');
        container.id = fileData.id;
        container.className = 'file-item-container mb-2';
        const fileSizeMB = (fileData.file.size / (1024 * 1024)).toFixed(2);
        container.innerHTML = `
            <div class="file-item">
                <div class="file-item-icon">${getFileIcon(fileData.file.type)}</div>
                <div class="file-item-content">
                    <p class="file-name">${fileData.file.name}</p>
                    <div class="file-item-meta">
                        <p class="file-size">${fileSizeMB} MB</p>
                        <div class="file-item-actions">
                            <div class="copy-counter">
                                <button type="button" class="copy-counter-btn minus-btn"><i data-lucide="minus"></i></button>
                                <span class="copy-counter-num">${fileData.copies}</span>
                                <button type="button" class="copy-counter-btn plus-btn"><i data-lucide="plus"></i></button>
                            </div>
                            <button type="button" class="settings-btn" title="Print Settings"><i data-lucide="settings-2"></i></button>
                            <button type="button" class="remove-file-btn" title="Remove File"><i data-lucide="trash-2"></i></button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="file-settings-panel"></div>
        `;
        fileListContainer.appendChild(container);
        lucide.createIcons();
        attachFileItemListeners(container, fileData);
    }

    function attachFileItemListeners(container, fileData) {
        const copyNumEl = container.querySelector('.copy-counter-num');
        const settingsBtn = container.querySelector('.settings-btn');
        const settingsPanel = container.querySelector('.file-settings-panel');
        container.querySelector('.minus-btn').addEventListener('click', () => { if (fileData.copies > 1) { fileData.copies--; copyNumEl.textContent = fileData.copies; } });
        container.querySelector('.plus-btn').addEventListener('click', () => { fileData.copies++; copyNumEl.textContent = fileData.copies; });
        container.querySelector('.remove-file-btn').addEventListener('click', () => { uploadedFiles = uploadedFiles.filter(f => f.id !== fileData.id); container.remove(); });
        settingsBtn.addEventListener('click', () => {
            const isOpen = settingsPanel.classList.toggle('open');
            settingsBtn.classList.toggle('active', isOpen);
            if (isOpen && settingsPanel.innerHTML === '') {
                settingsPanel.innerHTML = getSettingsHTML(fileData.id, fileData.options);
                attachSettingsListeners(settingsPanel, fileData);
            }
        });
    }
    
    function getSettingsHTML(id, options) {
        return `
            <div class="d-flex flex-column gap-3">
                <div class="row">
                    <div class="col-sm-6"><div class="d-flex justify-content-between align-items-center"><label for="isColor-${id}" class="form-label mb-0">Color Print</label><div class="toggle-switch"><input type="checkbox" id="isColor-${id}" class="toggle-switch-checkbox" ${options.isColor ? 'checked' : ''}><label class="toggle-switch-label" for="isColor-${id}"></label></div></div></div>
                    <div class="col-sm-6 mt-3 mt-sm-0"><div class="d-flex justify-content-between align-items-center"><label for="isDuplex-${id}" class="form-label mb-0">Print on Both Sides</label><div class="toggle-switch"><input type="checkbox" id="isDuplex-${id}" class="toggle-switch-checkbox" ${options.isDuplex ? 'checked' : ''}><label class="toggle-switch-label" for="isDuplex-${id}"></label></div></div></div>
                </div>
                <div class="row">
                    <div class="col-sm-6"><label class="form-label">Orientation</label><div class="segmented-control"><input type="radio" name="orientation-${id}" id="portrait-${id}" value="portrait" ${options.orientation === 'portrait' ? 'checked' : ''}><label for="portrait-${id}">Portrait</label><input type="radio" name="orientation-${id}" id="landscape-${id}" value="landscape" ${options.orientation === 'landscape' ? 'checked' : ''}><label for="landscape-${id}">Landscape</label></div></div>
                    <div class="col-sm-6 mt-3 mt-sm-0"><label class="form-label" for="pageRange-${id}">Page Range</label><input type="text" id="pageRange-${id}" class="form-control form-control-sm" value="${options.pageRange}"></div>
                </div>
            </div>
        `;
    }

    function attachSettingsListeners(panel, fileData) {
        panel.querySelector(`input[id^="pageRange-"]`).addEventListener('input', (e) => { fileData.options.pageRange = e.target.value.trim() || 'All'; });
        panel.querySelector(`input[id^="isColor-"]`).addEventListener('change', (e) => { fileData.options.isColor = e.target.checked; });
        panel.querySelector(`input[id^="isDuplex-"]`).addEventListener('change', (e) => { fileData.options.isDuplex = e.target.checked; });
        panel.querySelectorAll(`input[name^="orientation-"]`).forEach(el => el.addEventListener('change', (e) => { fileData.options.orientation = e.target.value; }));
    }

    function getFileIcon(fileType) {
        if (fileType.includes('pdf')) return '<i data-lucide="file-text"></i>';
        if (fileType.includes('word')) return '<i data-lucide="file-text"></i>';
        if (fileType.includes('presentation')) return '<i data-lucide="slideshow"></i>';
        if (fileType.includes('image')) return '<i data-lucide="file-image"></i>';
        return '<i data-lucide="file"></i>';
    }

    // --- Validation Logic ---
    const setError = (input, message) => { const parentContainer = input.closest('.col-md-6, .col-12'); const feedbackEl = parentContainer.querySelector('.invalid-feedback'); input.classList.add('is-invalid'); if (input.id === 'idProof') idProofDropzone.classList.add('is-invalid'); if (feedbackEl) feedbackEl.textContent = message; };
    const setSuccess = (input) => { const parentContainer = input.closest('.col-md-6, .col-12'); const feedbackEl = parentContainer.querySelector('.invalid-feedback'); input.classList.remove('is-invalid'); if (input.id === 'idProof') idProofDropzone.classList.remove('is-invalid'); if (feedbackEl) feedbackEl.textContent = ''; };
    const setFilesError = (message) => { dropZone.classList.add('is-invalid'); printFilesError.textContent = message; };
    const clearFilesError = () => { dropZone.classList.remove('is-invalid'); printFilesError.textContent = ''; };
    const validateField = (input) => {
        const value = input.value.trim(); const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; const phoneRegex = /^\d{10}$/; const label = document.querySelector(`label[for="${input.id}"]`); const labelText = label ? label.innerText.replace(' *', '') : 'This field';
        if (input.required && value === '' && input.type !== 'file') { setError(input, `${labelText} is required.`); return false; }
        if (input.id === 'emailId' && value !== '' && !emailRegex.test(value)) { setError(input, 'Please enter a valid email address.'); return false; }
        if (input.id === 'phoneNo' && input.required && !phoneRegex.test(value)) { setError(input, 'Phone number must be exactly 10 digits.'); return false; }
        if (input.id === 'idProof' && input.required && input.files.length === 0) { setError(input, 'Please upload your ID proof file.'); return false; }
        setSuccess(input); return true;
    };
    [fullNameInput, usnIdInput, phoneNoInput, emailIdInput].forEach(input => { input.addEventListener('input', () => { if (input.classList.contains('is-invalid')) setSuccess(input); }); input.addEventListener('blur', () => validateField(input)); });
    const validateForm = () => {
        let isFormValid = true;[fullNameInput, usnIdInput, phoneNoInput, idProofInput].forEach(input => { if (!validateField(input)) isFormValid = false; });
        if (emailIdInput.value.trim() !== '' && !validateField(emailIdInput)) isFormValid = false;
        if (uploadedFiles.length === 0) { setFilesError('Please upload at least one file to print.'); isFormValid = false; } else { clearFilesError(); }
        return isFormValid;
    };

    // --- View Pricing Logic ---
    viewPricingBtn.addEventListener('click', async (e) => {
        e.preventDefault(); // THE FIX: Prevents the link from jumping to the top of the page
        const originalBtnHtml = viewPricingBtn.innerHTML;
        viewPricingBtn.disabled = true;
        viewPricingBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Loading...`;
        
        try {
            const response = await fetch(`/api/shops/details/${shopId}`);
            if (!response.ok) throw new Error('Could not fetch prices.');
            const data = await response.json();

            if (data.success && data.shop) {
                const prices = data.shop;
                const priceListContent = `
                    <div class="price-grid">
                        <div class="price-item">
                            <dt>B&W Single-Sided</dt>
                            <dd>₹${parseFloat(prices.price_per_bw_page).toFixed(2)}</dd>
                        </div>
                        <div class="price-item">
                            <dt>B&W Both-Sided</dt>
                            <dd>₹${parseFloat(prices.price_per_bw_duplex).toFixed(2)}</dd>
                        </div>
                        <div class="price-item">
                            <dt>Color Single-Sided</dt>
                            <dd>₹${parseFloat(prices.price_per_color_page).toFixed(2)}</dd>
                        </div>
                        <div class="price-item">
                            <dt>Color Both-Sided</dt>
                            <dd>₹${parseFloat(prices.price_per_color_duplex).toFixed(2)}</dd>
                        </div>
                    </div>
                `;
                document.getElementById('price-grid-content').innerHTML = priceListContent;
                
                pricingViewDefault.classList.add('d-none');
                priceListContainer.classList.remove('d-none');
            } else {
                throw new Error(data.message || 'Could not load price list.');
            }
        } catch (error) {
            showToast(error.message);
            viewPricingBtn.disabled = false;
            viewPricingBtn.innerHTML = originalBtnHtml;
        }
    });

    // --- Close Pricing Logic ---
    document.getElementById('close-pricing-btn').addEventListener('click', () => {
        pricingViewDefault.classList.remove('d-none');
        priceListContainer.classList.add('d-none');
        viewPricingBtn.disabled = false;
        viewPricingBtn.innerHTML = `<i data-lucide="tag" class="me-2" style="width:1rem; height:1rem;"></i>View this shop's price list`;
        lucide.createIcons(); // Re-render the icon
    });

    // --- Form Submission Flow ---
    printForm.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!validateForm()) {
            showToast('Please fix the errors before submitting.');
            return;
        }

        document.getElementById('summary-total-files-modal').textContent = uploadedFiles.length;
        const totalCopies = uploadedFiles.reduce((acc, file) => acc + file.copies, 0);
        document.getElementById('summary-total-copies-modal').textContent = totalCopies;
        
        const fileListHtml = uploadedFiles.map(f => `<div class="text-truncate">- ${f.file.name} <strong>(x${f.copies})</strong></div>`).join('');
        document.getElementById('summary-file-list').innerHTML = fileListHtml;

        confirmationModal.show();
    });

    confirmSubmitBtn.addEventListener('click', async () => {
        confirmationModal.hide();
        const originalButtonText = submitButton.innerHTML;
        submitButton.disabled = true;
        submitButton.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Submitting...`;

        const formData = new FormData();
        formData.append('shopId', shopId);
        formData.append('fullName', fullNameInput.value);
        formData.append('usnId', usnIdInput.value);
        formData.append('phoneNo', phoneNoInput.value);
        formData.append('emailId', emailIdInput.value);
        formData.append('additionalNotes', document.getElementById('additionalNotes').value);
        formData.append('idProof', idProofInput.files[0]);

        const filesMetadata = uploadedFiles.map(fileData => ({
            originalName: fileData.file.name,
            copies: fileData.copies,
            options: fileData.options
        }));
        formData.append('filesMetadata', JSON.stringify(filesMetadata));

        uploadedFiles.forEach(fileData => {
            formData.append('printFiles', fileData.file);
        });

        try {
            const response = await fetch('/api/orders/submit', { method: 'POST', body: formData });
            const result = await response.json();
            if (response.ok && result.success) {
                showToast('Order submitted! Your Code is ' + result.orderCode, 'success');
                printForm.reset();
                uploadedFiles = [];
                fileListContainer.innerHTML = '';
                idProofText.innerHTML = `<span class="fw-semibold">Click to upload</span> (JPG, PNG, PDF)`;
                document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
                clearFilesError();
            } else {
                showToast(result.message || 'An unknown error occurred.');
            }
        } catch (error) {
            showToast('Could not connect to the server. Please try again later.');
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
        }
    });

    lucide.createIcons();
});