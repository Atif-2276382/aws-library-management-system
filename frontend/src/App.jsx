import {
  BrowserRouter,
  Routes,
  Route
} from "react-router-dom";

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

      </Routes>

    </BrowserRouter>
  );
}

export default App;