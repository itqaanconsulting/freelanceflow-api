const state = {
    token: localStorage.getItem("freelanceflow.token") || "",
    customers: [],
    projects: [],
    timeEntries: [],
    invoices: []
};

let activeNoticeTarget = null;

const els = {
    token: document.querySelector("#token"),
    loginForm: document.querySelector("#loginForm"),
    username: document.querySelector("#username"),
    password: document.querySelector("#password"),
    loadData: document.querySelector("#loadData"),
    resetData: document.querySelector("#resetData"),
    loadAudit: document.querySelector("#loadAudit"),
    runDemo: document.querySelector("#runDemo"),
    customerForm: document.querySelector("#customerForm"),
    projectForm: document.querySelector("#projectForm"),
    timeEntryForm: document.querySelector("#timeEntryForm"),
    authDot: document.querySelector("#authDot"),
    authState: document.querySelector("#authState"),
    authDetail: document.querySelector("#authDetail"),
    customerCount: document.querySelector("#customerCount"),
    projectCount: document.querySelector("#projectCount"),
    timeEntryCount: document.querySelector("#timeEntryCount"),
    invoiceCount: document.querySelector("#invoiceCount"),
    customers: document.querySelector("#customers"),
    projects: document.querySelector("#projects"),
    timeEntries: document.querySelector("#timeEntries"),
    invoices: document.querySelector("#invoices"),
    auditEvents: document.querySelector("#auditEvents"),
    notifications: document.querySelector("#notifications"),
    activityLog: document.querySelector("#activityLog")
};

els.token.value = state.token;
document.querySelector("input[name='email']").value = `reviewer-${Date.now()}@example.com`;
document.querySelector("input[name='workDate']").value = new Date().toISOString().slice(0, 10);
syncAuthState();

els.token.addEventListener("input", () => {
    state.token = els.token.value.trim();
    localStorage.setItem("freelanceflow.token", state.token);
    syncAuthState();
});

els.loginForm.addEventListener("submit", event => runAction(() => login(event), event.currentTarget));
els.loadData.addEventListener("click", event => runAction(loadDashboard, event.currentTarget));
els.resetData.addEventListener("click", event => runAction(resetBaseData, event.currentTarget));
els.loadAudit.addEventListener("click", event => runAction(loadAuditEvents, event.currentTarget));
els.runDemo.addEventListener("click", event => runAction(runSampleWorkflow, event.currentTarget));
els.customerForm.addEventListener("submit", event => runAction(() => createCustomer(event), event.currentTarget));
els.projectForm.addEventListener("submit", event => runAction(() => createProject(event), event.currentTarget));
els.timeEntryForm.addEventListener("submit", event => runAction(() => createTimeEntry(event), event.currentTarget));

async function login(event) {
    event.preventDefault();
    const body = new URLSearchParams({
        grant_type: "password",
        client_id: "freelanceflow-api",
        username: els.username.value.trim(),
        password: els.password.value
    });

    const response = await fetch("http://localhost:8180/realms/freelanceflow/protocol/openid-connect/token", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body
    });

    if (!response.ok) {
        throw new Error(`Keycloak login failed with HTTP ${response.status}`);
    }

    const payload = await response.json();
    state.token = payload.access_token;
    els.token.value = state.token;
    localStorage.setItem("freelanceflow.token", state.token);
    syncAuthState();
    log("Logged in with Keycloak.");
    notify("success", "Login successful", `Authenticated as ${els.username.value.trim()}.`);
    await loadDashboard();
}

async function loadDashboard() {
    requireToken();
    const [customers, projects, timeEntries, invoices] = await Promise.all([
        api("/api/customers"),
        api("/api/projects"),
        api("/api/time-entries"),
        api("/api/invoices")
    ]);

    state.customers = customers;
    state.projects = projects;
    state.timeEntries = timeEntries;
    state.invoices = invoices;
    render();
    log("Dashboard loaded from secured API endpoints.");
    notify("success", "Dashboard loaded", "Latest customers, projects, time entries and invoices are visible.");
}

async function loadAuditEvents() {
    requireToken();
    const response = await fetch("/api/audit-events", {
        headers: {"Authorization": `Bearer ${state.token}`}
    });

    if (response.status === 403) {
        renderAuditAccessDenied();
        log("Audit events require ADMIN role. Login with admin / admin.");
        notify("warning", "Admin required", "Audit events are only available for admin / admin.");
        return;
    }

    if (!response.ok) {
        throw await apiError("/api/audit-events", response);
    }

    renderAuditEvents(await response.json());
    log("Audit events loaded.");
    notify("success", "Audit loaded", "Audit history is updated.");
}

async function resetBaseData() {
    requireToken();
    const response = await fetch("/api/demo/reset", {
        method: "POST",
        headers: {"Authorization": `Bearer ${state.token}`}
    });

    if (response.status === 403) {
        log("Reset requires ADMIN role. Login with admin / admin.");
        throw new Error("Reset requires ADMIN role. Login with admin / admin.");
    }

    if (!response.ok) {
        throw await apiError("/api/demo/reset", response);
    }

    const result = await response.json();
    log(`Base data reset: ${result.timeEntries} time entries and ${result.auditEvents} audit events created.`);
    notify("success", "Base data reset", `${result.timeEntries} time entries and ${result.auditEvents} audit events created.`);
    await loadDashboard();
    await loadAuditEvents();
}

async function runSampleWorkflow() {
    requireToken();
    const stamp = new Date().toISOString().slice(0, 19).replaceAll(":", "-");
    const today = new Date().toISOString().slice(0, 10);
    const dueDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);

    const customer = await api("/api/customers", {
        method: "POST",
        body: {
            companyName: `Client ${stamp}`,
            contactName: "Jane Doe",
            email: `reviewer-${Date.now()}@example.com`,
            phone: "+31 20 000 0000",
            vatNumber: "NL000000000B01",
            street: "Demo Street 1",
            city: "Amsterdam",
            country: "Netherlands"
        }
    });
    log(`Created customer ${customer.companyName}.`);

    const project = await api("/api/projects", {
        method: "POST",
        body: {
            customerId: customer.id,
            name: "Backend API delivery",
            description: "Spring Boot API implementation, security and invoicing workflow.",
            hourlyRate: 95,
            currency: "EUR",
            status: "ACTIVE",
            startDate: today,
            endDate: null
        }
    });
    log(`Created project ${project.name}.`);

    const entry = await api("/api/time-entries", {
        method: "POST",
        body: {
            projectId: project.id,
            workDate: today,
            hours: 6.5,
            description: "Implemented secured invoice workflow and PDF export."
        }
    });
    log("Created draft time entry.");

    const submitted = await api(`/api/time-entries/${entry.id}/submit`, {method: "POST"});
    log(`Submitted time entry: ${submitted.status}.`);

    const approved = await api(`/api/time-entries/${entry.id}/approve`, {method: "POST"});
    log(`Approved time entry: ${approved.status}.`);

    const invoice = await api("/api/invoices/generate", {
        method: "POST",
        body: {
            projectId: project.id,
            issueDate: today,
            dueDate
        }
    });
    log(`Generated invoice ${invoice.invoiceNumber}.`);

    await loadDashboard();
    notify("success", "Workflow finished", `Invoice ${invoice.invoiceNumber} was generated from a new customer, project and time entry.`);
}

async function createCustomer(event) {
    event.preventDefault();
    requireToken();
    const form = new FormData(event.currentTarget);
    const customer = await api("/api/customers", {
        method: "POST",
        body: {
            companyName: form.get("companyName"),
            contactName: form.get("contactName"),
            email: form.get("email"),
            phone: "",
            vatNumber: "",
            street: "",
            city: form.get("city"),
            country: "Netherlands"
        }
    });
    log(`Created customer ${customer.companyName}.`);
    notify("success", "Customer created", `${customer.companyName} is available in the dashboard.`);
    await loadDashboard();
}

async function createProject(event) {
    event.preventDefault();
    requireToken();
    const form = new FormData(event.currentTarget);
    const project = await api("/api/projects", {
        method: "POST",
        body: {
            customerId: form.get("customerId"),
            name: form.get("name"),
            description: "Created from the web UI.",
            hourlyRate: Number(form.get("hourlyRate")),
            currency: String(form.get("currency")).toUpperCase(),
            status: "ACTIVE",
            startDate: new Date().toISOString().slice(0, 10),
            endDate: null
        }
    });
    log(`Created project ${project.name}.`);
    notify("success", "Project created", `${project.name} is linked to the selected customer.`);
    await loadDashboard();
}

async function createTimeEntry(event) {
    event.preventDefault();
    requireToken();
    const form = new FormData(event.currentTarget);
    const entry = await api("/api/time-entries", {
        method: "POST",
        body: {
            projectId: form.get("projectId"),
            workDate: form.get("workDate"),
            hours: Number(form.get("hours")),
            description: form.get("description")
        }
    });
    log(`Created draft time entry for ${entry.projectName}.`);
    notify("success", "Time entry created", `${entry.hours} hours logged for ${entry.projectName}.`);
    await loadDashboard();
}

async function submitTimeEntry(id) {
    const entry = await api(`/api/time-entries/${id}/submit`, {method: "POST"});
    log(`Submitted time entry for ${entry.projectName}.`);
    notify("success", "Time entry submitted", `${entry.projectName} is ready for approval.`);
    await loadDashboard();
}

async function approveTimeEntry(id) {
    const entry = await api(`/api/time-entries/${id}/approve`, {method: "POST"});
    log(`Approved time entry for ${entry.projectName}.`);
    notify("success", "Time entry approved", `${entry.projectName} can now be invoiced.`);
    await loadDashboard();
}

async function generateInvoice(projectId) {
    const today = new Date().toISOString().slice(0, 10);
    const dueDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
    const invoice = await api("/api/invoices/generate", {
        method: "POST",
        body: {projectId, issueDate: today, dueDate}
    });
    log(`Generated invoice ${invoice.invoiceNumber}.`);
    notify("success", "Invoice generated", `${invoice.invoiceNumber} is visible in the invoice list.`);
    await loadDashboard();
}

async function markInvoicePaid(id) {
    const invoice = await api(`/api/invoices/${id}/mark-paid`, {method: "POST"});
    log(`Marked invoice ${invoice.invoiceNumber} as paid.`);
    notify("success", "Invoice paid", `${invoice.invoiceNumber} has status PAID.`);
    await loadDashboard();
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        method: options.method || "GET",
        headers: {
            "Authorization": `Bearer ${state.token}`,
            ...(options.body ? {"Content-Type": "application/json"} : {})
        },
        body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (!response.ok) {
        throw await apiError(path, response);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function render() {
    els.customerCount.textContent = state.customers.length;
    els.projectCount.textContent = state.projects.length;
    els.timeEntryCount.textContent = state.timeEntries.length;
    els.invoiceCount.textContent = state.invoices.length;
    renderSelects();

    els.customers.className = state.customers.length ? "list" : "list empty";
    els.customers.innerHTML = state.customers.length
        ? state.customers.map(customer => `
            <div class="item">
                <strong>${escapeHtml(customer.companyName)}</strong>
                <div class="meta">${escapeHtml(customer.contactName || "No contact")} - ${escapeHtml(customer.email)}</div>
            </div>
        `).join("")
        : "No customers found.";

    els.projects.className = state.projects.length ? "list" : "list empty";
    els.projects.innerHTML = state.projects.length
        ? state.projects.map(project => `
            <div class="item">
                <strong>${escapeHtml(project.name)}</strong>
                <div class="meta">${escapeHtml(project.customerName)} - ${money(project.hourlyRate, project.currency)} / hour</div>
                <span class="badge ${project.status.toLowerCase()}">${project.status}</span>
                <div class="row-actions">
                    <button class="link" type="button" data-generate-invoice="${project.id}">Generate invoice</button>
                </div>
            </div>
        `).join("")
        : "No projects found.";

    els.timeEntries.className = state.timeEntries.length ? "table" : "table empty";
    els.timeEntries.innerHTML = state.timeEntries.length ? `
        <table>
            <thead>
            <tr><th>Date</th><th>Project</th><th>Description</th><th>Hours</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
            ${state.timeEntries.map(entry => `
                <tr>
                    <td>${entry.workDate}</td>
                    <td>${escapeHtml(entry.projectName)}</td>
                    <td>${escapeHtml(entry.description)}</td>
                    <td>${entry.hours}</td>
                    <td><span class="badge ${entry.status.toLowerCase()}">${entry.status}</span></td>
                    <td>${timeEntryActions(entry)}</td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    ` : "No time entries found.";

    els.invoices.className = state.invoices.length ? "table" : "table empty";
    els.invoices.innerHTML = state.invoices.length ? `
        <table>
            <thead>
            <tr><th>Invoice</th><th>Customer</th><th>Project</th><th>Due</th><th>Total</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
            ${state.invoices.map(invoice => `
                <tr>
                    <td>${escapeHtml(invoice.invoiceNumber)}</td>
                    <td>${escapeHtml(invoice.customerName)}</td>
                    <td>${escapeHtml(invoice.projectName)}</td>
                    <td>${invoice.dueDate}</td>
                    <td>${money(invoice.totalAmount, invoice.currency)}</td>
                    <td><span class="badge ${invoice.status.toLowerCase()}">${invoice.status}</span></td>
                    <td>
                        <div class="table-actions">
                            <button class="link" type="button" data-pdf="${invoice.id}">Download PDF</button>
                            ${invoice.status === "ISSUED" ? `<button class="link" type="button" data-mark-paid="${invoice.id}">Mark paid</button>` : ""}
                        </div>
                    </td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    ` : "No invoices found.";

    document.querySelectorAll("[data-pdf]").forEach(button => {
        button.addEventListener("click", event => runAction(() => downloadPdf(button.dataset.pdf), event.currentTarget));
    });
    document.querySelectorAll("[data-submit-entry]").forEach(button => {
        button.addEventListener("click", event => runAction(() => submitTimeEntry(button.dataset.submitEntry), event.currentTarget));
    });
    document.querySelectorAll("[data-approve-entry]").forEach(button => {
        button.addEventListener("click", event => runAction(() => approveTimeEntry(button.dataset.approveEntry), event.currentTarget));
    });
    document.querySelectorAll("[data-generate-invoice]").forEach(button => {
        button.addEventListener("click", event => runAction(() => generateInvoice(button.dataset.generateInvoice), event.currentTarget));
    });
    document.querySelectorAll("[data-mark-paid]").forEach(button => {
        button.addEventListener("click", event => runAction(() => markInvoicePaid(button.dataset.markPaid), event.currentTarget));
    });
}

function renderAuditAccessDenied() {
    els.auditEvents.className = "empty";
    els.auditEvents.innerHTML = `<span class="error">Audit events require ADMIN role. Login with admin / admin and click Load audit events.</span>`;
}

function renderAuditEvents(events) {
    els.auditEvents.className = events.length ? "table" : "table empty";
    els.auditEvents.innerHTML = events.length ? `
        <table>
            <thead>
            <tr><th>Time</th><th>Event</th><th>Aggregate</th><th>Message</th></tr>
            </thead>
            <tbody>
            ${events.map(event => `
                <tr>
                    <td>${formatDateTime(event.createdAt)}</td>
                    <td><span class="badge ${event.eventType.toLowerCase()}">${escapeHtml(event.eventType)}</span></td>
                    <td>
                        <strong>${escapeHtml(event.aggregateType)}</strong>
                        <div class="meta mono">${escapeHtml(event.aggregateId)}</div>
                    </td>
                    <td>${escapeHtml(event.message)}</td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    ` : "No audit events found.";
}

function renderSelects() {
    const customerOptions = options(state.customers, customer => customer.companyName);
    const projectOptions = options(state.projects, project => `${project.customerName} - ${project.name}`);
    document.querySelectorAll("select[name='customerId']").forEach(select => {
        select.innerHTML = customerOptions || `<option value="">Create a customer first</option>`;
        select.disabled = !customerOptions;
    });
    document.querySelectorAll("select[name='projectId']").forEach(select => {
        select.innerHTML = projectOptions || `<option value="">Create a project first</option>`;
        select.disabled = !projectOptions;
    });
}

function options(items, label) {
    return items.map(item => `<option value="${item.id}">${escapeHtml(label(item))}</option>`).join("");
}

function timeEntryActions(entry) {
    if (entry.status === "DRAFT") {
        return `<button class="link" type="button" data-submit-entry="${entry.id}">Submit</button>`;
    }
    if (entry.status === "SUBMITTED") {
        return `<button class="link" type="button" data-approve-entry="${entry.id}">Approve</button>`;
    }
    return `<span class="meta">No action</span>`;
}

async function downloadPdf(invoiceId) {
    requireToken();
    const response = await fetch(`/api/invoices/${invoiceId}/pdf`, {
        headers: {"Authorization": `Bearer ${state.token}`}
    });

    if (!response.ok) {
        throw new Error(`PDF download failed with HTTP ${response.status}`);
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `invoice-${invoiceId}.pdf`;
    link.click();
    URL.revokeObjectURL(url);
    log(`Downloaded PDF for invoice ${invoiceId}.`);
    notify("success", "PDF downloaded", `Invoice ${invoiceId} PDF download started.`);
}

function requireToken() {
    if (!state.token) {
        throw new Error("No bearer token available. Login or paste a token first.");
    }
}

function syncAuthState() {
    const connected = Boolean(state.token);
    els.authDot.classList.toggle("connected", connected);
    els.authState.textContent = connected ? "Token available" : "Not authenticated";
    els.authDetail.textContent = connected ? "Secured API calls can be made." : "Login or paste a bearer token to call the API.";
}

function log(message) {
    const item = document.createElement("li");
    item.textContent = `${new Date().toLocaleTimeString()} - ${message}`;
    els.activityLog.prepend(item);
}

function runAction(action, target = null) {
    const previousTarget = activeNoticeTarget;
    activeNoticeTarget = target;
    Promise.resolve()
        .then(action)
        .catch(showError)
        .finally(() => {
            activeNoticeTarget = previousTarget;
        });
}

async function apiError(path, response) {
    const payload = await readErrorPayload(response);
    const message = errorMessage(payload) || `${path} failed with HTTP ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.path = path;
    return error;
}

async function readErrorPayload(response) {
    const text = await response.text();
    if (!text) {
        return null;
    }
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

function errorMessage(payload) {
    if (!payload) {
        return "";
    }
    if (typeof payload === "string") {
        return payload;
    }
    if (payload.validationErrors && Object.keys(payload.validationErrors).length) {
        return Object.entries(payload.validationErrors)
            .map(([field, message]) => `${field}: ${message}`)
            .join("; ");
    }
    return payload.message || payload.error || "";
}

function showError(error) {
    const message = error?.message || String(error);
    log(message);
    notify("error", "Action failed", message);
    els.authDetail.innerHTML = `<span class="error">${escapeHtml(message)}</span>`;
}

function notify(type, title, message) {
    const item = document.createElement("article");
    item.className = `notice ${type}`;
    item.innerHTML = `
        <div>
            <strong>${escapeHtml(title)}</strong>
            <p>${escapeHtml(message)}</p>
        </div>
        <button type="button" aria-label="Dismiss notification">Close</button>
    `;
    item.querySelector("button").addEventListener("click", () => item.remove());
    const localTarget = noticeTarget(activeNoticeTarget);
    if (localTarget) {
        item.classList.add("inline-notice");
        localTarget.parentNode.insertBefore(item, localTarget.nextSibling);
    } else {
        els.notifications.prepend(item);
    }

    trimNotices(localTarget ? localTarget.parentNode : els.notifications);

    if (type !== "error") {
        window.setTimeout(() => item.remove(), 6000);
    }
}

function noticeTarget(target) {
    if (!target || !target.isConnected) {
        return null;
    }
    return target.closest("form")
        || target.closest(".actions")
        || target.closest("td")
        || target.closest(".item")
        || null;
}

function trimNotices(container) {
    const notices = Array.from(container.querySelectorAll(":scope > .notice"));
    notices.slice(4).forEach(notice => notice.remove());
}

function money(value, currency) {
    return new Intl.NumberFormat("en-US", {style: "currency", currency: currency || "EUR"}).format(Number(value || 0));
}

function formatDateTime(value) {
    return new Intl.DateTimeFormat("en-US", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

window.addEventListener("error", event => {
    showError(event.error || new Error(event.message));
});

window.addEventListener("unhandledrejection", event => {
    showError(event.reason);
});
