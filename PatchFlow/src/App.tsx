import { Routes, Route, Navigate } from "react-router-dom";
import { onAuthStateChanged } from "firebase/auth";
import { auth } from "./firebase";
import { useEffect, useState } from "react";

import Login from "./pages/Login";
import Issues from "./pages/Issues";
import Projects from "./pages/Projects";
import Board from "./pages/Board";

function App() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to="/issues" /> : <Login />}/>

      <Route path="/issues" element={user ? <Issues /> : <Navigate to="/" />}/>

      <Route path="/projects" element={ user ? <Projects /> : <Navigate to="/" />}/>

      <Route path="/board" element={ user ? <Board /> : <Navigate to="/" />}/>
    </Routes>
  );
}

export default App;