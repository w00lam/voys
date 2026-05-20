import { useState } from 'react';
import './App.css';
import { getHealth, type HealthResponse } from './features/health/api';

type HealthState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'ready'; data: HealthResponse }
  | { status: 'failed'; message: string };

function App() {
  const [health, setHealth] = useState<HealthState>({ status: 'idle' });

  async function checkBackend() {
    setHealth({ status: 'loading' });

    try {
      const data = await getHealth();
      setHealth({ status: 'ready', data });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Backend check failed.';
      setHealth({ status: 'failed', message });
    }
  }

  return (
    <main className="app-shell">
      <section className="workspace">
        <div className="eyebrow">Voys bootstrap</div>
        <h1>Record, transcribe, and search long voice notes.</h1>
        <p className="intro">
          The project foundation is ready for a React recording client backed by a Spring Boot API.
        </p>

        <div className="status-panel">
          <div>
            <h2>Backend connection</h2>
            <p>Calls <code>/api/health</code> with browser credentials included.</p>
          </div>
          <button type="button" onClick={checkBackend} disabled={health.status === 'loading'}>
            {health.status === 'loading' ? 'Checking...' : 'Check API'}
          </button>
        </div>

        {health.status === 'ready' && (
          <p className="result success">
            API is {health.data.status}. Last checked at {new Date(health.data.timestamp).toLocaleString()}.
          </p>
        )}

        {health.status === 'failed' && (
          <p className="result failure">{health.message}</p>
        )}
      </section>
    </main>
  );
}

export default App;

