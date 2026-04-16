import './LoginPage.scss';

export default function LoginPage() {
    return (
        <section className='login-page'>
            <form className='login-form'>
                <h2>Login</h2>
                <div className='input-group'>
                    <label htmlFor='username'>Username</label>
                    <input
                        id='username'
                        name='username'
                        type='text'
                        required
                    />
                </div>
                <div className='input-group'>
                    <label htmlFor='password'>Password</label>
                    <input
                        id='password'
                        name='password'
                        type='password'
                        required
                    />
                </div>
                <button className='login-button' type='submit'>Login</button>
            </form>
        </section>
    );
}