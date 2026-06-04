const state = {
    token: localStorage.getItem("freelanceflow.token") || "",
    customers: [],
    projects: [],
    timeEntries: [],
    invoices: []
};

const els = {
    token: document.querySelector("#token"),
    loginForm: document.querySelector("#loginForm"),
    username: document.querySelector("#username"),
    password: document.querySelector("#password"),
    loadData: document.querySelector("#loadData"),
    runDemo: document.querySelector("#runDemo"),
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
    activityLog: document.querySelector("#activityLog")
};

els.token.value = state.token;
syncAuthState();

els.token.addEventListener("input", () => {
    state.token = els.token.value.trim();
    localStorage.setItem("freelanceflow.token", state.token);
    syncAuthState();
});

els.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await login();
});

els.loadData.addEventListener("click", loadDashboard);
els.runDemo.addEventListener("click", runSampleWorkflow);

async function login() {
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
    log("Logged in with Keycloak demo user.");
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
}

async function runSampleWorkflow() {
    requireToken();
    const stamp = new Date().toISOString().slice(0, 19).replaceAll(":", "-");
    const today = new Date().toISOString().slice(0, 10);
    const dueDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);

    const customer = await api("/api/customers", {
        method: "POST",
        body: {
            companyName: `Demo Client ${stamp}`,
            contactName: "Portfolio Reviewer",
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
        const text = await response.text();
        throw new Error(`${path} failed with HTTP ${response.status}: ${text}`);
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
            </div>
        `).join("")
        : "No projects found.";

    els.timeEntries.className = state.timeEntries.length ? "table" : "table empty";
    els.timeEntries.innerHTML = state.timeEntries.length ? `
        <table>
            <thead>
            <tr><th>Date</th><th>Project</th><th>Description</th><th>Hours</th><th>Status</th></tr>
            </thead>
            <tbody>
            ${state.timeEntries.map(entry => `
                <tr>
                    <td>${entry.workDate}</td>
                    <td>${escapeHtml(entry.projectName)}</td>
                    <td>${escapeHtml(entry.description)}</td>
                    <td>${entry.hours}</td>
                    <td><span class="badge ${entry.status.toLowerCase()}">${entry.status}</span></td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    ` : "No time entries found.";

    els.invoices.className = state.invoices.length ? "table" : "table empty";
    els.invoices.innerHTML = state.invoices.length ? `
        <table>
            <thead>
            <tr><th>Invoice</th><th>Customer</th><th>Project</th><th>Due</th><th>Total</th><th>Status</th><th>PDF</th></tr>
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
                    <td><button class="link" type="button" data-pdf="${invoice.id}">Download</button></td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    ` : "No invoices found.";

    document.querySelectorAll("[data-pdf]").forEach(button => {
        button.addEventListener("click", () => downloadPdf(button.dataset.pdf));
    });
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

function money(value, currency) {
    return new Intl.NumberFormat("en-US", {style: "currency", currency: currency || "EUR"}).format(Number(value || 0));
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
    log(event.message);
    els.authDetail.innerHTML = `<span class="error">${escapeHtml(event.message)}</span>`;
});

window.addEventListener("unhandledrejection", event => {
    const message = event.reason?.message || String(event.reason);
    log(message);
    els.authDetail.innerHTML = `<span class="error">${escapeHtml(message)}</span>`;
});
