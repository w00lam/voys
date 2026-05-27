import { type FormEvent, useEffect, useRef, useState } from 'react';
import './App.css';
import {
  getCurrentUser,
  login,
  logout,
  signUp,
  type CurrentUser,
} from './features/auth/api';
import { getHealth, type HealthResponse } from './features/health/api';
import {
  getMemo,
  getMemoAudio,
  getTranscript,
  listMemos,
  startTranscription,
  importAudioFile,
  updateMemoTitle,
  type MemoDetail,
  type MemoSummary,
  type TranscriptResponse,
} from './features/memos/api';
import { uploadRecording, type CreatedMemo } from './features/recorder/api';
import { getErrorMessage } from './shared/api/client';
import { searchMemos, type SearchResult } from './features/search/api';

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

type MemoState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'ready'; memos: MemoSummary[] }
  | { status: 'failed'; message: string };

type PlaybackState =
  | { status: 'idle' }
  | { status: 'loading'; memoId: string }
  | { status: 'ready'; memo: MemoDetail; audioUrl: string }
  | { status: 'failed'; message: string };

type TranscriptState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'ready'; transcript: TranscriptResponse }
  | { status: 'failed'; message: string };

type SearchState =
  | { status: 'idle' }
  | { status: 'searching' }
  | { status: 'ready'; results: SearchResult[] }
  | { status: 'failed'; message: string };

const MAX_RECORDING_DURATION_SECONDS = 7_200;
const RECORDING_TIMER_INTERVAL_MS = 1_000;

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
  const [memoState, setMemoState] = useState<MemoState>({ status: 'idle' });
  const [playback, setPlayback] = useState<PlaybackState>({ status: 'idle' });
  const [transcript, setTranscript] = useState<TranscriptState>({ status: 'idle' });
  const [searchQuery, setSearchQuery] = useState('');
  const [search, setSearch] = useState<SearchState>({ status: 'idle' });
  const [pendingSeek, setPendingSeek] = useState<number | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isImporting, setIsImporting] = useState(false);
  const [importError, setImportError] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState('');
  const [isSavingTitle, setIsSavingTitle] = useState(false);
  const [saveTitleError, setSaveTitleError] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const isStoppingRef = useRef(false);

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
    if (auth.status === 'authenticated') {
      void refreshMemos();
    } else if (auth.status === 'guest') {
      setMemoState({ status: 'idle' });
      setPlayback({ status: 'idle' });
      setTranscript({ status: 'idle' });
      setSearchQuery('');
      setSearch({ status: 'idle' });
    }
  }, [auth.status]);

  useEffect(() => {
    return () => {
      if (playback.status === 'ready') {
        URL.revokeObjectURL(playback.audioUrl);
      }
    };
  }, [playback]);

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
    }, RECORDING_TIMER_INTERVAL_MS);

    return () => window.clearInterval(intervalId);
  }, [recorder.status]);

  useEffect(() => {
    if (recorder.status === 'recording' && recorder.elapsedSeconds >= MAX_RECORDING_DURATION_SECONDS) {
      if (!isStoppingRef.current) {
        isStoppingRef.current = true;
        recorder.recorder.stop();
      }
    }
  }, [recorder]);

  useEffect(() => {
    if (playback.status !== 'ready' || transcript.status !== 'ready') {
      return undefined;
    }

    const currentMemoId = playback.memo.id;
    const currentStatus = transcript.transcript.status;

    if (currentStatus !== 'PENDING' && currentStatus !== 'PROCESSING') {
      return undefined;
    }

    const intervalId = window.setInterval(async () => {
      try {
        const updatedTranscript = await getTranscript(currentMemoId);

        setTranscript((prevTranscript) => {
          if (prevTranscript.status === 'ready' && prevTranscript.transcript.memoId === currentMemoId) {
            if (updatedTranscript.status === 'COMPLETED' || updatedTranscript.status === 'FAILED') {
              void refreshMemos();

              setPlayback((prevPlayback) => {
                if (prevPlayback.status === 'ready' && prevPlayback.memo.id === currentMemoId) {
                  return {
                    ...prevPlayback,
                    memo: {
                      ...prevPlayback.memo,
                      transcriptionStatus: updatedTranscript.status,
                    },
                  };
                }
                return prevPlayback;
              });
            }

            return {
              status: 'ready',
              transcript: updatedTranscript,
            };
          }
          return prevTranscript;
        });
      } catch (error) {
        setTranscript({ status: 'failed', message: getErrorMessage(error) });
      }
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [
    playback.status,
    playback.status === 'ready' ? playback.memo.id : null,
    transcript.status,
    transcript.status === 'ready' ? transcript.transcript.status : null,
  ]);

  useEffect(() => {
    const trimmed = searchQuery.trim();
    if (!trimmed) {
      setSearch({ status: 'idle' });
      return undefined;
    }

    let active = true;
    const timerId = window.setTimeout(async () => {
      try {
        setSearch({ status: 'searching' });
        const results = await searchMemos(trimmed);
        if (active) {
          setSearch({ status: 'ready', results });
        }
      } catch (error) {
        if (active) {
          setSearch({ status: 'failed', message: getErrorMessage(error) });
        }
      }
    }, 300);

    return () => {
      active = false;
      window.clearTimeout(timerId);
    };
  }, [searchQuery]);

  useEffect(() => {
    if (playback.status === 'ready' && pendingSeek !== null) {
      const audio = audioRef.current;
      if (audio) {
        audio.currentTime = pendingSeek;
        setPendingSeek(null);
      }
    }
  }, [playback.status, pendingSeek]);

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
      await refreshMemos();
      setPassword('');
      setAuthMessage(mode === 'signup' ? '계정이 생성되었습니다.' : '로그인되었습니다.');
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
      setMemoState({ status: 'idle' });
      setPlayback({ status: 'idle' });
      setTranscript({ status: 'idle' });
      setSearchQuery('');
      setSearch({ status: 'idle' });
      setAuthMessage('로그아웃되었습니다.');
    } catch (error) {
      setAuthMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function startRecording() {
    if (!navigator.mediaDevices?.getUserMedia) {
      setRecorder({ status: 'failed', message: '이 브라우저에서는 녹음을 지원하지 않습니다.' });
      return;
    }

    const mimeType = 'audio/webm;codecs=opus';
    if (!MediaRecorder.isTypeSupported(mimeType)) {
      setRecorder({ status: 'failed', message: '이 브라우저에서는 WebM/Opus 녹음을 지원하지 않습니다.' });
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream, { mimeType });
      const chunks: BlobPart[] = [];
      const startedAt = Date.now();
      isStoppingRef.current = false;

      mediaRecorder.addEventListener('dataavailable', (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data);
        }
      });

      mediaRecorder.addEventListener('stop', async () => {
        isStoppingRef.current = true;
        stream.getTracks().forEach((track) => track.stop());
        setRecorder({ status: 'uploading' });

        try {
          const audio = new Blob(chunks, { type: mimeType });
          const durationSeconds = Math.max(1, Math.floor((Date.now() - startedAt) / 1000));
          const memo = await uploadRecording(audio, durationSeconds);
          setRecorder({ status: 'uploaded', memo });
          await refreshMemos();
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
    if (recorder.status === 'recording' && !isStoppingRef.current) {
      isStoppingRef.current = true;
      recorder.recorder.stop();
    }
  }

  async function refreshMemos() {
    setMemoState({ status: 'loading' });

    try {
      const memos = await listMemos();
      setMemoState({ status: 'ready', memos });
    } catch (error) {
      setMemoState({ status: 'failed', message: getErrorMessage(error) });
    }
  }

  async function selectMemo(memoId: string, seekSeconds: number | null = null) {
    setSearchQuery('');
    setPendingSeek(seekSeconds);
    if (playback.status === 'ready') {
      URL.revokeObjectURL(playback.audioUrl);
    }

    setPlayback({ status: 'loading', memoId });
    setTranscript({ status: 'loading' });
    setSaveTitleError(null);

    try {
      const [memo, audio, transcriptResult] = await Promise.all([
        getMemo(memoId),
        getMemoAudio(memoId),
        getTranscript(memoId),
      ]);
      setPlayback({ status: 'ready', memo, audioUrl: URL.createObjectURL(audio) });
      setTranscript({ status: 'ready', transcript: transcriptResult });
      setEditingTitle(memo.title);
    } catch (error) {
      setPlayback({ status: 'failed', message: getErrorMessage(error) });
      setTranscript({ status: 'idle' });
    }
  }

  async function submitTranscription() {
    if (playback.status !== 'ready') {
      return;
    }

    setTranscript({ status: 'loading' });

    try {
      const transcriptResult = await startTranscription(playback.memo.id);
      setTranscript({ status: 'ready', transcript: transcriptResult });
      await refreshMemos();
    } catch (error) {
      setTranscript({ status: 'failed', message: getErrorMessage(error) });
      await refreshMemos();
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFile(e.target.files[0]);
      setImportError(null);
    }
  };

  const handleImport = async () => {
    if (!selectedFile) return;
    setIsImporting(true);
    setImportError(null);
    try {
      await importAudioFile(selectedFile);
      setSelectedFile(null);
      const fileInput = document.getElementById('audio-file-input') as HTMLInputElement;
      if (fileInput) {
        fileInput.value = '';
      }
      await refreshMemos();
    } catch (error) {
      setImportError(getErrorMessage(error));
    } finally {
      setIsImporting(false);
    }
  };

  const handleSaveTitle = async () => {
    if (playback.status !== 'ready' || !editingTitle.trim()) return;
    setIsSavingTitle(true);
    setSaveTitleError(null);
    const memoId = playback.memo.id;
    try {
      const updated = await updateMemoTitle(memoId, editingTitle);
      setPlayback((prev) => {
        if (prev.status === 'ready' && prev.memo.id === memoId) {
          return {
            ...prev,
            memo: {
              ...prev.memo,
              title: updated.title,
            },
          };
        }
        return prev;
      });
      await refreshMemos();
    } catch (error) {
      setSaveTitleError(getErrorMessage(error));
    } finally {
      setIsSavingTitle(false);
    }
  };

  const adoptSuggestedTitle = async (suggested: string) => {
    if (playback.status !== 'ready' || !suggested.trim()) return;
    setIsSavingTitle(true);
    setSaveTitleError(null);
    const memoId = playback.memo.id;
    try {
      const updated = await updateMemoTitle(memoId, suggested);
      setPlayback((prev) => {
        if (prev.status === 'ready' && prev.memo.id === memoId) {
          return {
            ...prev,
            memo: {
              ...prev.memo,
              title: updated.title,
            },
          };
        }
        return prev;
      });
      setEditingTitle(updated.title);
      await refreshMemos();
    } catch (error) {
      setSaveTitleError(getErrorMessage(error));
    } finally {
      setIsSavingTitle(false);
    }
  };

  return (
    <main className="app-shell">
      <section className="workspace">
        <div className="eyebrow">Voys 워크스페이스</div>
        <h1>긴 음성 기록을 녹음하고, 문서화하고, 다시 찾아보세요.</h1>
        <p className="intro">
          회의, 강의, 인터뷰 녹음을 계정별로 안전하게 보관하고 필요한 순간을 검색할 수 있습니다.
        </p>

        <div className="auth-panel">
          {auth.status === 'loading' && (
            <p className="muted">로그인 상태를 확인하고 있습니다...</p>
          )}

          {auth.status === 'authenticated' && (
            <>
              <div className="signed-in">
                <div>
                  <span className="label">로그인됨</span>
                  <strong>{auth.user.displayName}</strong>
                  <span className="muted">{auth.user.email}</span>
                </div>
                <button type="button" onClick={submitLogout} disabled={isSubmitting}>
                  {isSubmitting ? '로그아웃 중...' : '로그아웃'}
                </button>
              </div>

              <div className="recorder-panel">
                <div>
                  <h2>브라우저 녹음</h2>
                  <p>WebM/Opus 음성을 녹음해 개인 메모로 저장합니다. 최대 2시간까지 녹음할 수 있습니다.</p>
                </div>

                {recorder.status === 'recording' ? (
                  <button type="button" className="danger" onClick={stopRecording}>
                    녹음 중지 {formatDuration(recorder.elapsedSeconds)}
                  </button>
                ) : (
                  <button type="button" onClick={startRecording} disabled={recorder.status === 'uploading'}>
                    {recorder.status === 'uploading' ? '업로드 중...' : '녹음 시작'}
                  </button>
                )}
              </div>

              <div className="import-panel">
                <div>
                  <h2>오디오 파일 가져오기</h2>
                  <p>기존 오디오 파일(MP3, WAV, WebM)을 가져와 라이브러리에 저장합니다.</p>
                </div>
                <div className="import-controls">
                  <label htmlFor="audio-file-input">오디오 파일</label>
                  <input
                    id="audio-file-input"
                    type="file"
                    accept=".mp3,.wav,.webm,audio/*"
                    onChange={handleFileChange}
                  />
                  <button
                    type="button"
                    onClick={handleImport}
                    disabled={isImporting || !selectedFile}
                  >
                    {isImporting ? '가져오는 중...' : '가져오기'}
                  </button>
                </div>
                {importError && <p className="result failure">{importError}</p>}
              </div>

              {recorder.status === 'uploaded' && (
                <p className="result success">
                  {recorder.memo.title} 메모를 저장했습니다. 전사 상태는 {formatStatus(recorder.memo.transcriptionStatus)}입니다.
                </p>
              )}

              {recorder.status === 'failed' && (
                <p className="result failure">{recorder.message}</p>
              )}

              <div className="search-panel">
                <h2>검색</h2>
                <div className="search-input-wrapper">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="제목과 전사 내용 검색..."
                    aria-label="녹음 검색"
                    className="search-input"
                  />
                </div>

                {searchQuery.trim() === '' && (
                  <p className="search-guide muted">검색어를 입력해 저장된 메모를 찾아보세요.</p>
                )}

                {searchQuery.trim() !== '' && search.status === 'searching' && (
                  <p className="muted">검색 중...</p>
                )}

                {search.status === 'failed' && (
                  <p className="result failure">{search.message}</p>
                )}

                {search.status === 'ready' && searchQuery.trim() !== '' && (
                  <div className="search-results">
                    {search.results.length === 0 ? (
                      <p className="muted">검색 결과가 없습니다.</p>
                    ) : (
                      <ul className="search-results-list">
                        {search.results.map((result) => (
                          <li key={`${result.memoId}-${result.matchType}-${result.snippet}`}>
                            <button
                              type="button"
                              onClick={() => selectMemo(result.memoId, result.segmentStartSeconds)}
                              className="search-result-btn"
                            >
                              <div className="search-result-header">
                                <span className="search-result-title">{result.title}</span>
                                {result.segmentStartSeconds != null && (
                                  <span className="search-result-timestamp">{formatTimestamp(result.segmentStartSeconds)}</span>
                                )}
                                <span className="badge-match-type">{formatMatchType(result.matchType)}</span>
                              </div>
                              <p className="search-result-snippet">{result.snippet}</p>
                              <small className="search-result-status">
                                상태: {formatStatus(result.transcriptionStatus)}
                              </small>
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}
              </div>

              <div className="library-panel">
                <div className="library-heading">
                  <div>
                    <h2>저장된 메모</h2>
                    <p>계정에 저장된 녹음과 전사 내용을 확인합니다.</p>
                  </div>
                  <button type="button" onClick={refreshMemos} disabled={memoState.status === 'loading'}>
                    {memoState.status === 'loading' ? '불러오는 중...' : '새로고침'}
                  </button>
                </div>

                {memoState.status === 'ready' && memoState.memos.length === 0 && (
                  <p className="muted">아직 저장된 녹음이 없습니다.</p>
                )}

                {memoState.status === 'ready' && memoState.memos.length > 0 && (
                  <ul className="memo-list">
                    {memoState.memos.map((memo) => (
                      <li key={memo.id}>
                        <button type="button" onClick={() => selectMemo(memo.id)}>
                          <span>{memo.title}</span>
                          <small>
                            {formatStatus(memo.transcriptionStatus)} · {formatBytes(memo.audioSizeBytes)}
                          </small>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}

                {memoState.status === 'failed' && (
                  <p className="result failure">{memoState.message}</p>
                )}

                {playback.status === 'loading' && (
                  <p className="muted">선택한 메모를 불러오는 중...</p>
                )}

                {playback.status === 'ready' && (
                  <div className="playback-panel">
                    <div className="memo-header-pane">
                      <span className="label">선택한 메모</span>
                      <div className="title-edit-container">
                        <label htmlFor="memo-title-input">메모 제목</label>
                        <input
                          id="memo-title-input"
                          type="text"
                          value={editingTitle}
                          onChange={(e) => setEditingTitle(e.target.value)}
                        />
                        <button
                          type="button"
                          onClick={handleSaveTitle}
                          disabled={isSavingTitle || !editingTitle.trim()}
                        >
                          {isSavingTitle ? '저장 중...' : '저장'}
                        </button>
                      </div>
                      {transcript.status === 'ready' && transcript.transcript.suggestedTitle && (
                        <div className="suggested-title-box">
                          <span className="suggested-title-text">
                            추천 제목: <strong>{transcript.transcript.suggestedTitle}</strong>
                          </span>
                          <button
                            type="button"
                            className="suggested-title-btn"
                            onClick={() => adoptSuggestedTitle(transcript.transcript.suggestedTitle!)}
                            disabled={isSavingTitle}
                          >
                            추천 제목 사용
                          </button>
                        </div>
                      )}
                      {saveTitleError && <p className="result failure">{saveTitleError}</p>}
                      <strong>{playback.memo.title}</strong>
                      <span className="muted">
                        {new Date(playback.memo.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <audio ref={audioRef} controls src={playback.audioUrl}>
                      <track kind="captions" />
                    </audio>
                    <div className="transcript-panel">
                      <div className="library-heading">
                        <div>
                          <h2>전사</h2>
                          <p>
                            상태:{' '}
                            {transcript.status === 'ready'
                              ? formatStatus(transcript.transcript.status)
                              : formatStatus(playback.memo.transcriptionStatus)}
                          </p>
                        </div>
                        <button
                          type="button"
                          onClick={submitTranscription}
                          disabled={transcript.status === 'loading'
                            || (transcript.status === 'ready' && transcript.transcript.status === 'PROCESSING')}
                        >
                          {transcript.status === 'loading' ? '전사 중...' : '전사 시작'}
                        </button>
                      </div>

                      {transcript.status === 'ready' && transcript.transcript.text && (
                        <pre>{transcript.transcript.text}</pre>
                      )}

                      {transcript.status === 'ready' && transcript.transcript.segments && transcript.transcript.segments.length > 0 && (
                        <div className="transcript-segments">
                          {transcript.transcript.segments.map((segment) => (
                            <button
                              key={segment.position}
                              type="button"
                              onClick={() => {
                                if (audioRef.current) {
                                  audioRef.current.currentTime = segment.startSeconds;
                                }
                              }}
                              className="segment-btn"
                            >
                              <span className="segment-timestamp">{formatTimestamp(segment.startSeconds)}</span>{' '}
                              <span className="segment-text">{segment.text}</span>
                            </button>
                          ))}
                        </div>
                      )}

                      {transcript.status === 'ready' && transcript.transcript.status === 'FAILED' && (
                        <p className="result failure">
                          {transcript.transcript.failureReason?.message || '전사에 실패했습니다.'}
                        </p>
                      )}

                      {transcript.status === 'ready'
                        && transcript.transcript.status !== 'FAILED'
                        && !transcript.transcript.text && (
                        <p className="muted">아직 생성된 전사 내용이 없습니다.</p>
                      )}

                      {transcript.status === 'ready' && transcript.transcript.status === 'PROCESSING' && (
                        <p className="processing-guide">
                          긴 녹음이나 첫 전사는 몇 분 이상 걸릴 수 있습니다.
                        </p>
                      )}

                      {transcript.status === 'failed' && (
                        <p className="result failure">{transcript.message}</p>
                      )}
                    </div>
                  </div>
                )}

                {playback.status === 'failed' && (
                  <p className="result failure">{playback.message}</p>
                )}
              </div>
            </>
          )}

          {auth.status === 'guest' && (
            <form className="auth-form" onSubmit={submitAuth}>
              <div className="mode-switch" aria-label="인증 모드">
                <button
                  type="button"
                  className={mode === 'login' ? 'active' : ''}
                  onClick={() => setMode('login')}
                >
                  로그인
                </button>
                <button
                  type="button"
                  className={mode === 'signup' ? 'active' : ''}
                  onClick={() => setMode('signup')}
                >
                  회원가입
                </button>
              </div>

              {mode === 'signup' && (
                <label>
                  표시 이름
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
                이메일
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  maxLength={320}
                  required
                />
              </label>

              <label>
                비밀번호
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
                {isSubmitting ? '처리 중...' : mode === 'signup' ? '계정 만들기' : '로그인'}
              </button>
            </form>
          )}

          {authMessage && <p className="result">{authMessage}</p>}
        </div>

        <div className="status-panel">
          <div>
            <h2>API 연결 상태</h2>
            <p>브라우저 세션을 포함해 <code>/api/health</code>를 호출합니다.</p>
          </div>
          <button type="button" onClick={checkBackend} disabled={health.status === 'loading'}>
            {health.status === 'loading' ? '확인 중...' : 'API 확인'}
          </button>
        </div>

        {health.status === 'ready' && (
          <p className="result success">
            API 상태는 {health.data.status === 'ok' ? '정상' : health.data.status}입니다. 마지막 확인 시간은 {new Date(health.data.timestamp).toLocaleString()}입니다.
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

function formatTimestamp(seconds: number): string {
  const totalSeconds = Math.floor(seconds);
  const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
  const secs = (totalSeconds % 60).toString().padStart(2, '0');
  return `${minutes}:${secs}`;
}

function formatStatus(status: string): string {
  switch (status) {
    case 'PENDING':
      return '대기 중';
    case 'PROCESSING':
      return '처리 중';
    case 'COMPLETED':
      return '완료';
    case 'FAILED':
      return '실패';
    case 'UPLOADED':
      return '업로드 완료';
    default:
      return status.toLowerCase();
  }
}

function formatMatchType(matchType: string): string {
  switch (matchType) {
    case 'TITLE':
      return '제목';
    case 'TRANSCRIPT':
      return '전사';
    default:
      return matchType;
  }
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export default App;

