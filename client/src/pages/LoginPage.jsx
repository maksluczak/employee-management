import './LoginPage.scss';
import {useAuth} from "../context/useAuth.js";
import {useState} from "react";
import {apiClient} from "../api/apiClient.js";

export default function LoginPage() {
    const [credentials, setCredentials] = useState({username: "", password: ""});
    const [isLoading, setIsLoading] = useState(false);
    const { login } = useAuth();

    const handleChange = (e) => {
        setCredentials({...credentials, [e.target.name]: e.target.value});
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const response = await apiClient.post("/auth/authenticate", credentials);

            if (response?.token) {
                login(response.token, response.role || "USER");
                alert("Logged in successfully!");
            }
        } catch (err) {
            console.error(err);
            alert(err.message || "Something went wrong!");
        } finally {
            setIsLoading(false);
        }
    }
    return (
        <section className='login-page'>
            <form className='login-form' onSubmit={handleSubmit}>
                <h2>Sign In</h2>
                <div className='input-group'>
                    <label htmlFor='username'>Username</label>
                    <input
                        id='username'
                        name='username'
                        type='text'
                        onChange={handleChange}
                        value={credentials.username}
                        required
                    />
                </div>
                <div className='input-group'>
                    <label htmlFor='password'>Password</label>
                    <input
                        id='password'
                        name='password'
                        type='password'
                        onChange={handleChange}
                        value={credentials.password}
                        required
                    />
                </div>
                <button
                    className='login-button'
                    type='submit'
                    disabled={isLoading}
                >
                    {isLoading ? "Logging in..." : "Login"}
                </button>
            </form>
        </section>
    );
}