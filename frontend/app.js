const API_URL_BASE = 'http://localhost:8080';


function decodificarJWT(token) {
    try {
        const payloadBase64 = token.split('.')[1];
        const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(payloadJson);
    } catch (error) {
        console.error('Error al decodificar el token:', error);
        return null;
    }
}

function obtenerRolesDelToken() {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) return [];
    const payload = decodificarJWT(token);
    return payload?.roles ?? [];
}


async function fetchWithAuth(url, options = {}) {
    const token = sessionStorage.getItem('jwt_token');

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...(options.headers || {})
    };

    const respuesta = await fetch(url, { ...options, headers });

    if (respuesta.status === 401 || respuesta.status === 403) {
        sessionStorage.removeItem('jwt_token');
        sessionStorage.removeItem('jwt_username');
        window.location.href = 'index.html';
        throw new Error('Sesión expirada o sin permisos');
    }

    return respuesta;
}

function mostrarErroresValidacion(cuerpoError) {
    const contenedor = document.getElementById('alertaErrores');
    if (!contenedor) return;

    let mensajes = [];
    if (cuerpoError.errores) {
        mensajes = Object.entries(cuerpoError.errores).map(([campo, msg]) => `${campo}: ${msg}`);
    } else if (cuerpoError.error) {
        mensajes = [cuerpoError.error];
    }

    contenedor.innerHTML = `<strong>Error:</strong><ul>${mensajes.map(m => `<li>${m}</li>`).join('')}</ul>`;
    contenedor.hidden = false;
}

//login

const formularioLogin = document.getElementById('loginForm');

if (formularioLogin) {
    formularioLogin.addEventListener('submit', async (evento) => {
        evento.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const mensajeError = document.getElementById('loginError');

        try {
            const respuesta = await fetch(`${API_URL_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!respuesta.ok) {
                mensajeError.textContent = 'Usuario o contraseña incorrectos';
                mensajeError.hidden = false;
                return;
            }

            const datos = await respuesta.json();

            sessionStorage.setItem('jwt_token', datos.token);
            sessionStorage.setItem('jwt_username', datos.username);

            window.location.href = 'dashboard.html';

        } catch (error) {
            console.error('Error al iniciar sesión:', error);
            mensajeError.textContent = 'No se pudo conectar con el servidor';
            mensajeError.hidden = false;
        }
    });
}



let envios = [];
let filtroActual = 'TODOS';

async function inicializarDashboard() {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    const roles = obtenerRolesDelToken();
    const username = sessionStorage.getItem('jwt_username');

    document.getElementById('nombreUsuario').textContent = `${username}`;

    //solo visible para admin
    if (roles.includes('ROLE_ADMIN')) {
        document.getElementById('bitacoraPanel').hidden = false;
    }

    await cargarEnvios();
}

async function cargarEnvios() {
    try {
        const respuesta = await fetchWithAuth(`${API_URL_BASE}/api/envios/optimizados`);
        envios = await respuesta.json();
        actualizarKPIs();
        renderizarGrid();
    } catch (error) {
        console.error('Error al cargar envíos:', error);
    }
}

function actualizarKPIs() {
    document.getElementById('kpiTotalEnvios').textContent = envios.length;
    document.getElementById('kpiEntregados').textContent =
        envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;

   
    const roles = obtenerRolesDelToken();
    if (roles.includes('ROLE_ADMIN')) {
        fetchWithAuth(`${API_URL_BASE}/api/vehiculos`)
            .then(r => r.json())
            .then(vehiculos => {
                document.getElementById('kpiVehiculosActivos').textContent =
                    vehiculos.filter(v => v.estado === 'DISPONIBLE' || v.estado === 'EN_RUTA').length;
            })
            .catch(() => {
                document.getElementById('kpiVehiculosActivos').textContent = '—';
            });
    } else {
        document.getElementById('kpiVehiculosActivos').textContent = '—';
    }
}

function renderizarGrid() {
    const contenedor = document.getElementById('enviosGrid');
    contenedor.innerHTML = '';

    const roles = obtenerRolesDelToken();
    const puedeMarcarEnTransito = roles.includes('ROLE_ADMIN');
    const puedeMarcarEntregado = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_CONDUCTOR');

    const enviosFiltrados = filtroActual === 'TODOS'
        ? envios
        : envios.filter(e => e.estadoEnvio === filtroActual);

    enviosFiltrados.forEach(envio => {
        const tarjeta = document.createElement('article');
        tarjeta.className = 'tarjeta-envio';

        let botonesAccion = '';
        if (puedeMarcarEnTransito && envio.estadoEnvio === 'PENDIENTE') {
            botonesAccion += `<button onclick="actualizarEstado(${envio.id}, 'EN_TRANSITO')">Marcar en Tránsito</button>`;
        }
        if (puedeMarcarEntregado && envio.estadoEnvio === 'EN_TRANSITO') {
            botonesAccion += `<button onclick="actualizarEstado(${envio.id}, 'ENTREGADO')">Marcar Entregado</button>`;
        }

        tarjeta.innerHTML = `
            <h3>${envio.codigoRastreo}</h3>
            <span class="pill-status ${envio.estadoEnvio}">${envio.estadoEnvio}</span>
            <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
            <p><strong>Peso:</strong> ${envio.pesoKg} kg</p>
            <p><strong>Vehículo:</strong> ${envio.placaVehiculo ?? '—'}</p>
            <p><strong>Conductor:</strong> ${envio.nombreConductor ?? '—'}</p>
            ${botonesAccion ? `<div class="tarjeta-acciones">${botonesAccion}</div>` : ''}
            ${roles.includes('ROLE_ADMIN') ? `<button class="btn-bitacora" onclick="cargarBitacora(${envio.id})">Ver Bitácora</button>` : ''}
        `;
        contenedor.appendChild(tarjeta);
    });
}

async function actualizarEstado(id, nuevoEstado) {
    try {
        const respuesta = await fetchWithAuth(`${API_URL_BASE}/api/envios/${id}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado, observaciones: '' })
        });

        if (!respuesta.ok) {
            const errorData = await respuesta.json();
            mostrarErroresValidacion(errorData);
            return;
        }

        document.getElementById('alertaErrores').hidden = true;
        await cargarEnvios();
    } catch (error) {
        console.error('Error al actualizar estado:', error);
    }
}

async function cargarBitacora(envioId) {
    try {
        const respuesta = await fetchWithAuth(`${API_URL_BASE}/api/envios/${envioId}/bitacora`);
        const bitacora = await respuesta.json();

        const contenedor = document.getElementById('bitacoraContenido');
        if (bitacora.length === 0) {
            contenedor.innerHTML = '<p>Sin registros para este envío.</p>';
            return;
        }

        contenedor.innerHTML = bitacora.map(entrada => `
            <div class="entrada-bitacora">
                <p><strong>${entrada.estadoAnterior} → ${entrada.estadoNuevo}</strong></p>
                <p>${new Date(entrada.fechaCambio).toLocaleString()}</p>
                <p>${entrada.usuario}</p>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error al cargar bitácora:', error);
    }
}

const navFiltros = document.querySelectorAll('.nav-filtro');
navFiltros.forEach(boton => {
    boton.addEventListener('click', () => {
        navFiltros.forEach(b => b.classList.remove('activo'));
        boton.classList.add('activo');
        filtroActual = boton.dataset.estado;
        renderizarGrid();
    });
});

//logout
const btnLogout = document.getElementById('btnLogout');
if (btnLogout) {
    btnLogout.addEventListener('click', () => {
        sessionStorage.removeItem('jwt_token');
        sessionStorage.removeItem('jwt_username');
        window.location.href = 'index.html';
    });
}

if (document.getElementById('enviosGrid')) {
    inicializarDashboard();
}