import { type FormEvent, useEffect, useState } from 'react';
import './App.css';
import {
  getCurrentUser,
  login,
  logout,
  signUp,
  type CurrentUser,
} from './features/auth/api';
import { getHealth, type HealthResponse } from './features/health/api';
import { uploadRecording, type CreatedMemo } from './features/recorder/api';
import { getErrorMessage } from './shared/api/client';

type HealthState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'ready'; data: HealthResponse }
  | { status: 'failed'; message: string };

type AuthState =
  | { status: 'loading' }
  | { status: 'guest' }
  | { status: 'authenticated'; user: CurrentUser };

type RecorderState =
  | { status: 'idle' }
  | { status: 'recording'; startedAt: number; elapsedSeconds: number; recorder: MediaRecorder }
  | { status: 'uploading' }
  | { status: 'uploaded'; memo: CreatedMemo }
  | { status: 'failed'; message: string };

function App() {
  const [health, setHealth] = useState<HealthState>({ status: 'idle' });
  const [auth, setAuth] = useState<AuthState>({ status: 'loading' });
  const [mode, setMode] = useState<'login' | 'signup'>('login');
  const [email, setEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [password, setPassword] = useState('');
  const [authMessage, setAuthMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [recorder, setRecorder] = useState<RecorderState>({ status: 'idle' });

  useEffect(() => {
    let active = true;

    getCurrentUser()
      .then((user) => {
        if (active) {
          setAuth({ status: 'authenticated', user });
        }
      })
      .catch(() => {
        if (active) {
          setAuth({ status: 'guest' });
        }
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (recorder.status !== 'recording') {
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      setRecorder((current) => {
        if (current.status !== 'recording') {
          return current;
        }

        return {
          ...current,
          elapsedSeconds: Math.floor((Date.now() - current.startedAt) / 1000),
        };
      });
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [recorder.status]);

  async function checkBackend() {
    setHealth({ status: 'loading' });

    try {
      const data = await getHealth();
      setHealth({ status: 'ready', data });
    } catch (error) {
      const message = getErrorMessage(error);
      setHealth({ status: 'failed', message });
    }
  }

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setAuthMessage(null);

    try {
      const user = mode === 'signup'
        ? await signUp({ email, password, displayName })
        : await login({ email, password });

      setAuth({ status: 'authenticated', user });
      setPassword('');
      setAuthMessage(mode === 'signup' ? 'Account created.' : 'Logged in.');
    } catch (error) {
      setAuthMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function submitLogout() {
    setIsSubmitting(true);
    setAuthMessage(null);

    try {
      await logout();
      setAuth({ status: 'guest' });
      setAuthMessage('Logged out.');
    } catch (error) {
      setAuthMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function startRecording() {
    if (!navigator.mediaDevices?.getUserMedia) {
      setRecorder({ status: 'failed', message: 'Recording is not supported in this browser.' });
      return;
    }

    const mimeType = 'audio/webm;codecs=opus';
    if (!MediaRecorder.isTypeSupported(mimeType)) {
      setRecorder({ status: 'failed', message: 'WebM/Opus recording is not supported in this browser.' });
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream, { mimeType });
      const chunks: BlobPart[] = [];
      const startedAt = Date.now();

      mediaRecorder.addEventListener('dataavailable', (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data);
        }
      });

      mediaRecorder.addEventListener('stop', async () => {
        stream.getTracks().forEach((track) => track.stop());
        setRecorder({ status: 'uploading' });

        try {
          const audio = new Blob(chunks, { type: mimeType });
          const durationSeconds = Math.max(1, Math.floor((Date.now() - startedAt) / 1000));
          const memo = await uploadRecording(audio, durationSeconds);
          setRecorder({ status: 'uploaded', memo });
        } catch (error) {
          setRecorder({ status: 'failed', message: getErrorMessage(error) });
        }
      });

      mediaRecorder.start();
      setRecorder({ status: 'recording', startedAt, elapsedSeconds: 0, recorder: mediaRecorder });
    } catch (error) {
      setRecorder({ status: 'failed', message: getErrorMessage(error) });
    }
  }

  function stopRecording() {
    if (recorder.status === 'recording') {
      recorder.recorder.stop();
    }
  }

  return (
    <main className="app-shell">
      <section className="workspace">
        <div className="eyebrow">Voys workspace</div>
        <h1>Record, transcribe, and search long voice notes.</h1>
        <p className="intro">
          Sign in to keep recordings and transcripts separated by account.
        </p>

        <div className="auth-panel">
          {auth.status === 'loading' && (
            <p className="muted">Checking session...</p>
          )}

          {auth.status === 'authenticated' && (
            <>
              <div className="signed-in">
                <div>
                  <span className="label">Signed in</span>
                  <strong>{auth.user.displayName}</strong>
                  <span className="muted">{auth.user.email}</span>
                </div>
                <button type="button" onClick={submitLogout} disabled={isSubmitting}>
                  {isSubmitting ? 'Signing out...' : 'Log out'}
                </button>
              </div>

              <div className="recorder-panel">
                <div>
                  <h2>Browser recording</h2>
                  <p>Record WebM/Opus audio and save it as a private memo.</p>
                </div>

                {recorder.status === 'recording' ? (
                  <button type="button" className="danger" onClick={stopRecording}>
                    Stop {formatDuration(recorder.elapsedSeconds)}
                  </button>
                ) : (
                  <button type="button" onClick={startRecording} disabled={recorder.status === 'uploading'}>
                    {recorder.status === 'uploading' ? 'Uploading...' : 'Start recording'}
                  </button>
                )}
              </div>

              {recorder.status === 'uploaded' && (
                <p className="result success">
                  Saved {recorder.memo.title}. Transcription is {recorder.memo.transcriptionStatus.toLowerCase()}.
                </p>
              )}

              {recorder.status === 'failed' && (
                <p className="result failure">{recorder.message}</p>
              )}
            </>
          )}

          {auth.status === 'guest' && (
            <form className="auth-form" onSubmit={submitAuth}>
              <div className="mode-switch" aria-label="Authentication mode">
                <button
                  type="button"
                  className={mode === 'login' ? 'active' : ''}
                  onClick={() => setMode('login')}
                >
                  Login
                </button>
                <button
                  type="button"
                  className={mode === 'signup' ? 'active' : ''}
                  onClick={() => setMode('signup')}
                >
                  Sign up
                </button>
              </div>

              {mode === 'signup' && (
                <label>
                  Display name
                  <input
                    value={displayName}
                    onChange={(event) => setDisplayName(event.target.value)}
                    minLength={1}
                    maxLength={120}
                    required
                  />
                </label>
              )}

              <label>
                Email
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  maxLength={320}
                  required
                />
              </label>

              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  minLength={8}
                  maxLength={72}
                  required
                />
              </label>

              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Submitting...' : mode === 'signup' ? 'Create account' : 'Log in'}
              </button>
            </form>
          )}

          {authMessage && <p className="result">{authMessage}</p>}
        </div>

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

function formatDuration(totalSeconds: number): string {
  const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
  const seconds = (totalSeconds % 60).toString().padStart(2, '0');
  return `${minutes}:${seconds}`;
}

export default App;

