import "./login.css";
import patch from "./assets/patchflow.ico";
import { useNavigate } from "react-router-dom";

import { signInWithPopup } from "firebase/auth";
import { auth, provider } from "../firebase";

function Login() {
  const navigate = useNavigate();

  const googleSignIn = async () => {
    try {
      const result = await signInWithPopup(auth, provider);
      navigate("/issues");
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <section className="card">
      <div>
        <img src={patch} alt="patchflow" width="32" height="32" />
        <h1>Log in to PatchFlow</h1>

        <button className="button" onClick={googleSignIn}>
          Continue with Google
        </button>

        <br />
        <br />

        Don't have an account? Sign up
      </div>
    </section>
  );
}

export default Login;