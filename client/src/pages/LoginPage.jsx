import { useState } from "react";
import { api } from "../api.js";

export default function LoginPage({ onLogin }) {
    const [form, setForm] = useState({ username: "", password: "" });
    const [error, setError] = useState(null);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        try {
            onLogin(await api.login(form.username, form.password));
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <main className="login">
            <form className="card" onSubmit={handleSubmit}>
                <h1>Sign in</h1>
                <label>
                    Username
                    <input name="username" value={form.username} onChange={handleChange} required />
                </label>
                <label>
                    Password
                    <input name="password" type="password" value={form.password} onChange={handleChange} required />
                </label>
                {error && <p className="alert">{error}</p>}
                <button className="btn btn-green">Log in</button>
                <p className="muted">Default admin: <code>admin</code> / <code>admin123</code></p>
            </form>
        </main>
    );
}
