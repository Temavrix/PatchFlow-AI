import "./Sidebar.css";
import { useState } from "react";
import { Link } from "react-router-dom";
import { signOut } from "firebase/auth";
import { auth } from "../firebase";
import { useNavigate } from "react-router-dom";


function Sidebar() {

  const navigate = useNavigate();

  const handleLogout = async () => {
    await signOut(auth);
    navigate("/");
  };

  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <button className="menu-btn" onClick={() => setIsOpen(!isOpen)}>
        ☰
      </button>

      <aside className={`sidebar ${isOpen ? "open" : ""}`}>
        <nav>
          <ul>
            <li><Link to="/board">Board</Link></li>
            <li><Link to="/issues">My Issues</Link></li>
            <li><Link to="/projects">Projects</Link></li>
            <li>Teams</li>
            <li>Settings</li>
            <li onClick={handleLogout}>Logout</li>
          </ul>
        </nav>
      </aside>

      {isOpen && (
        <div
          className="overlay"
          onClick={() => setIsOpen(false)}
        />
      )}
    </>
  );
}

export default Sidebar;