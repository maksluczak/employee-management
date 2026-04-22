class ApiClient {
    #backendUrl = "http://localhost:8080/api/v1";
    #token = null;
    #refreshHandler = null;

    setToken(token) {
        this.#token = token;
    }

    onTokenExpired(handler) {
        this.#refreshHandler = handler;
    }

    async #getHeaders(customHeaders = {}) {
        const headers = {
            "Content-Type": "application/json",
        };

        if (this.#token) {
            headers["Authorization"] = `Bearer ${this.#token}`;
        }

        return { ...headers, ...customHeaders };
    }

    async #handleResponse(response) {
        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: "Unknown error" }));
            throw new Error(err.message || `HTTP Error: ${response.status}`);
        }
        if (response.status === 204) return undefined;
        return response.json();
    }

    async #request(method, endpoint, options = {}, isRetry = false) {
        const url = `${this.#backendUrl}${endpoint}`;
        const headers = await this.#getHeaders(options.headers);

        const response = await fetch(url, {
            method,
            headers,
            body: options.data ? JSON.stringify(options.data) : undefined,
            credentials: "include"
        });

        if (response.status === 401 && this.#refreshHandler && !isRetry) {
            const newToken = await this.#refreshHandler();
            if (newToken) {
                return this.#request(method, endpoint, options, true);
            }
        }

        return this.#handleResponse(response);
    }

    async #handleBlobResponse(response) {
        if (response.status === 404) {
            return null;
        }
        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: "Unknown error" }));
            throw new Error(err.message || `HTTP Error: ${response.status}`);
        }
        const blob = await response.blob();
        return URL.createObjectURL(blob);
    }

    async #imageRequest(endpoint, isRetry = false) {
        const url = `${this.#backendUrl}${endpoint}`;
        const headers = await this.#getHeaders();

        const response = await fetch(url, {
            method: "GET",
            headers,
            credentials: "include"
        });

        if (response.status === 401 && this.#refreshHandler && !isRetry) {
            const newToken = await this.#refreshHandler();
            if (newToken) {
                return this.#imageRequest(endpoint, true);
            }
        }

        return this.#handleBlobResponse(response);
    }

    get(endpoint, headers) { return this.#request("GET", endpoint, { headers }); }
    post(endpoint, data, headers) { return this.#request("POST", endpoint, { data, headers }); }
    put(endpoint, data, headers) { return this.#request("PUT", endpoint, { data, headers }); }
    patch(endpoint, data, headers) { return this.#request("PATCH", endpoint, { data, headers }); }
    delete(endpoint, headers) { return this.#request("DELETE", endpoint, { headers }); }
    getEmployeeImage(id) { return this.#imageRequest(`/employees/${id}/profile-image`); }
    getMyImage() { return this.#imageRequest(`/employees/me/profile-image`); }
}

export const apiClient = new ApiClient();