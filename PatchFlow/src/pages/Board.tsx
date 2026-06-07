import { useEffect, useState } from "react";
import Sidebar from "./Sidebar";
import { collection, onSnapshot, orderBy, query } from "firebase/firestore";
import { db } from "../firebase";
import "./Board.css";

function Board() {
  const [issues, setIssues] = useState<any[]>([]);

  useEffect(() => {
    const q = query(
      collection(db, "issues"),
      orderBy("createdAt", "desc")
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));

      setIssues(data);
    });

    return () => unsubscribe();
  }, []);

  const todoIssues = issues.filter((issue) => issue.status === "Todo");

  const progressIssues = issues.filter((issue) => issue.status === "In Progress");

  const reviewIssues = issues.filter((issue) => issue.status === "Code Review");

  return (
    <div className="layout">
      <Sidebar/>

    <main className="content">
        <div className="board-container">

          <div className="board-column">
            <h3>Todo ({todoIssues.length})</h3>

            {todoIssues.map((issue) => (
              <div key={issue.id} className="issue-board-card">
                <div className="issue-board-title">
                  {issue.title}
                </div>

                <div className="issue-board-project">
                  {issue.projectName}
                </div>

                <div className={`priority-badge ${issue.priority.toLowerCase().replace(" ", "-")}`}>
                  {issue.priority}
                </div>
              </div>
            ))}
          </div>

          <div className="board-column">
            <h3>In Progress ({progressIssues.length})</h3>

            {progressIssues.map((issue) => (
              <div key={issue.id} className="issue-board-card">
                <div className="issue-board-title">
                  {issue.title}
                </div>

                <div className="issue-board-project">
                  {issue.projectName}
                </div>

                <div className={`priority-badge ${issue.priority.toLowerCase().replace(" ", "-")}`}>
                  {issue.priority}
                </div>
              </div>
            ))}
          </div>

          <div className="board-column">
            <h3>Code Review ({reviewIssues.length})</h3>

            {reviewIssues.map((issue) => (
              <div key={issue.id} className="issue-board-card">
                <div className="issue-board-title">
                  {issue.title}
                </div>

                <div className="issue-board-project">
                  {issue.projectName}
                </div>

                <div
                  className={`priority-badge ${issue.priority.toLowerCase().replace(" ", "-")}`}>
                  {issue.priority}
                </div>
              </div>
            ))}
          </div>

        </div>
    </main>
    </div>
  );
}

export default Board;