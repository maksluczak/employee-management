const API_URL = "http://localhost:8080/api/v1";
const STORAGE_KEY = "ems-session";

let session = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || "null");
let onSessionEnd = () => {};
let refreshing = null;

export const getSession = () => session;

export function setSession(newSession) {
    session = newSession;
    if (newSession) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(newSession));
    else sessionStorage.removeItem(STORAGE_KEY);
}

export function whenSessionEnds(callback) {
    onSessionEnd = callback;
}

function send(method, path, body) {
    const headers = {};
    if (session) headers.Authorization = `Bearer ${session.token}`;

    const isFile = body instanceof FormData;
    if (body && !isFile) headers["Content-Type"] = "application/json";

    return fetch(API_URL + path, {
        method,
        headers,
        body: isFile ? body : body && JSON.stringify(body),
    });
}

async function request(method, path, body) {
    let response = await send(method, path, body);

    if (response.status === 401 && session && path !== "/auth/refresh") {
        refreshing ??= refreshToken().finally(() => (refreshing = null));
        if (await refreshing) response = await send(method, path, body);
    }
    if (!response.ok) throw new Error(await errorMessage(response));
    return response;
}

async function refreshToken() {
    const response = await send("POST", "/auth/refresh");
    if (!response.ok) {
        setSession(null);
        onSessionEnd();
        return false;
    }
    setSession(await response.json());
    return true;
}

async function errorMessage(response) {
    const body = await response.json().catch(() => null);
    if (!body) return `HTTP ${response.status}`;
    const fieldErrors = Object.entries(body.errors ?? {}).map(([field, msg]) => `${field}: ${msg}`);
    return [body.message, ...fieldErrors].join(" · ");
}

export const api = {
    async login(username, password) {
        const response = await request("POST", "/auth/authenticate", { username, password });
        setSession(await response.json());
        return session;
    },
    get: async (path) => (await request("GET", path)).json(),
    post: async (path, body) => (await request("POST", path, body)).json(),
    put: (path, body) => request("PUT", path, body),
    delete: (path) => request("DELETE", path),
    upload(path, file) {
        const form = new FormData();
        form.append("file", file);
        return request("POST", path, form);
    },
    async image(path) {
        try {
            const response = await request("GET", path);
            return URL.createObjectURL(await response.blob());
        } catch {
            return null;
        }
    },
};
