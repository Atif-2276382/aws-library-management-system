import {
  BrowserRouter,
  Routes,
  Route
} from "react-router-dom";
import Books from "./pages/Books";
import Members
from "./pages/Members";

function Login() {
  return <h2>Login Page</h2>;
}

function Dashboard() {
  return <h2>Dashboard Page</h2>;
}

function App() {

  return (
    <BrowserRouter>

      <Routes>

        <Route
          path="/"
          element={<Login />}
        />

        <Route
          path="/dashboard"
          element={<Dashboard />}
        />
        <Route
           path="/books"
           element={<Books/>}
        />
        <Route
          path="/members"
          element={<Members/>}
        />

      </Routes>

    </BrowserRouter>
  );
}

export default App;