/* ===================== Helpers ===================== */
async function parseBody(r) {
    if (r.status === 204) return null; // No Content
    const ct = r.headers.get('content-type') || '';
    if (ct.includes('application/json')) {
        try { return await r.json(); } catch { return null; }
    }
    return await r.text();
}

function isLoginPage() {
    return location.pathname === '/login.html';
}

function redirectToLogin() {
    if (!isLoginPage()) location.href = '/login.html';
}

/* Wrapper padrão para requisições JSON com cookie de sessão */
export async function requestJSON(url, options = {}) {
    const { method = 'GET', body, headers = {}, redirectOn401 = true, redirectOn403 = false } = options;

    const init = {
        method,
        credentials: 'include',
        headers: { ...headers },
    };

    if (body !== undefined) {
        // Se for FormData, deixa o browser definir o boundary do multipart
        if (body instanceof FormData) {
            init.body = body;
        } else {
            init.headers['Content-Type'] = init.headers['Content-Type'] || 'application/json';
            init.body = typeof body === 'string' ? body : JSON.stringify(body);
        }
    }

    const r = await fetch(url, init);

    if (r.status === 401) {
        if (redirectOn401) redirectToLogin();
        throw new Error(`401 Unauthorized on ${url}`);
    }

    if (r.status === 403) {
        // Acesso negado (ex.: role insuficiente). Opcional: mandar para home.
        alert('Acesso negado. Você não possui permissão para executar esta ação.');

        if (redirectOn403 && !isLoginPage()) {
            alert('Acesso negado.');
            location.href = '/index.html';
        }
        const msg = await parseBody(r);
        throw new Error(`403 Forbidden on ${url}: ${typeof msg === 'string' ? msg : JSON.stringify(msg)}`);
    }

    if (!r.ok) {
        const msg = await parseBody(r);
        throw new Error(`${r.status} on ${url}: ${typeof msg === 'string' ? msg : JSON.stringify(msg)}`);
    }

    return parseBody(r);
}

/* Atalhos REST */
export const getJSON = (url) => requestJSON(url);
export const postJSON = (url, body) => requestJSON(url, { method: 'POST', body });
export const putJSON = (url, body) => requestJSON(url, { method: 'PUT', body });
export const del = (url) => requestJSON(url, { method: 'DELETE' });

/* ===================== Sessão ===================== */
export async function getUserSession() {
    // Sem redirecionar aqui para evitar loops na tela de login
    const r = await fetch('/api/auth/me', { credentials: 'include' });
    if (r.status === 401 || r.status === 403) return null;
    try { return await r.json(); } catch { return null; }
}

export async function requireAuth() {
    const user = await getUserSession();
    if (!user && !isLoginPage()) redirectToLogin();
    return user;
}

/* ===================== Navbar parcial ===================== */
/*
  Espera um <div id="navbar"></div> na página.
  Opcional: no navbar.html, marque itens de menu só de admin com data-role="ADMIN"
  que então serão ocultados quando user.role !== 'ADMIN'.
*/
export async function loadNavbar(user) {
    const host = document.getElementById('navbar');
    if (!host) return;

    const html = await fetch('/partials/navbar.html', { credentials: 'include' }).then(r => r.text());
    host.innerHTML = html;

    // Marca link ativo
    const file = location.pathname.split('/').pop() || 'index.html';
    const route = file.replace('.html', '');
    const active = host.querySelector(`a.nav-link[data-route="${route}"]`);
    if (active) active.classList.add('active');

    // Usuário da sessão
    if (!user) user = await getUserSession();
    const userInfo = host.querySelector('#userInfo');
    if (user && userInfo) userInfo.textContent = user.nome;

    // Esconde itens por role (se existir marcação)
    if (user && user.role) {
        host.querySelectorAll('[data-role]').forEach(el => {
            const need = el.getAttribute('data-role');
            if (need && need !== user.role) el.classList.add('d-none');
        });
    }

    // Logout
    const btnLogout = host.querySelector('#btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', async () => {
            if (confirm('Deseja realmente sair?')) {
                try {
                    await requestJSON('/api/logout', { method: 'POST' });
                } finally {
                    redirectToLogin();
                }
            }
        });
    }
}

/* ===================== Boot padrão páginas internas ===================== */
export async function bootPage() {
    const user = await requireAuth();  // garante sessão (redireciona se 401/403)
    await loadNavbar(user);
    return user;
}
