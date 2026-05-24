# Antigravity Handoff: Transcription Failure Reason

## Branch

`feature/56-transcription-failure-reason`

## Issue

https://github.com/w00lam/voys/issues/56

## Your Role

Implement production code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected.

## Commands

From `backend/`:

```powershell
.\gradlew.bat test
```

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

## Expected Failing Tests At Handoff

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceFailureReasonTests.java`
- `frontend/src/App.test.tsx`

They fail because transcript responses do not include `failureReason` yet and the frontend failed transcript panel only shows a generic failure message.

## Implementation Requirements

- Add a nullable `failureReason` object to transcript API responses.
- Include `code`, `message`, and `retryable` fields.
- Persist a safe reason when background transcription fails.
- Clear the previous reason when transcription is restarted and the memo enters `PROCESSING`.
- Map known Whisper failures to stable reason codes.
- Keep raw stderr/stdout, stack traces, local paths, and full command details out of API responses.
- Render the safe message in the transcript panel when status is `FAILED`.
- Keep the original audio playable after failure.

## Suggested Failure Mapping

- `Whisper CLI could not be executed.` -> `WHISPER_COMMAND_NOT_FOUND`
- `Whisper transcription timed out.` -> `WHISPER_TIMEOUT`
- `Whisper did not produce a JSON transcript.` -> `WHISPER_EMPTY_OUTPUT`
- Process failures that mention invalid audio, decode errors, or ffmpeg decode failures -> `AUDIO_UNSUPPORTED_OR_INVALID`
- Everything else -> `TRANSCRIPTION_UNEXPECTED_ERROR`

## Notes

- The current `TranscriptionFailedException` message may be useful as an internal signal, but do not expose it directly unless it has been mapped to safe copy.
- Database migration strategy is left to Antigravity based on the current persistence setup.
- Manual retry UX can stay as the existing start transcription button for this slice.

## Antigravity Prompt

아래 프롬프트를 Antigravity에 그대로 전달하세요.

```text
Voys 프로젝트의 Phase 1.5 MVP 안정화 작업입니다.

현재 브랜치: feature/56-transcription-failure-reason
GitHub issue: https://github.com/w00lam/voys/issues/56
Draft PR: https://github.com/w00lam/voys/pull/57

목표:
전사 실패 시 사용자가 원인을 이해할 수 있도록, generic FAILED 상태만 보여주는 대신 안전한 실패 원인(code/message/retryable)을 저장하고 API/UI에 표시해 주세요.

작업 전 확인:
- AGENTS.md
- agent-os/product/
- agent-os/standards/index.yml
- agent-os/specs/2026-05-24-1845-transcription-failure-reason/

중요한 작업 방식:
- 이 브랜치는 Codex가 Agent OS spec과 failing tests를 작성한 TDD handoff 브랜치입니다.
- production code를 구현해서 테스트를 통과시키세요.
- failing test를 삭제하거나 약화하지 마세요.
- 요구사항이 바뀌지 않는 한 테스트 기대값을 production code에 맞춰 낮추지 마세요.

현재 예상 failing tests:
- backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceFailureReasonTests.java
- frontend/src/App.test.tsx 의 전사 실패 원인 표시 케이스

구현 요구사항:
- transcript API 응답에 nullable failureReason 객체를 추가하세요.
- failureReason은 code, message, retryable 필드를 포함해야 합니다.
- 전사 background job 실패 시 안전한 실패 원인을 저장하세요.
- 새 전사 시도가 PROCESSING으로 들어가면 이전 실패 원인을 clear 하세요.
- FAILED 상태에서 프론트엔드 transcript panel에 안전한 message를 표시하세요.
- 원본 오디오는 실패 후에도 계속 재생 가능해야 합니다.
- raw stderr/stdout, stack trace, local filesystem path, 전체 command detail은 API/UI에 노출하지 마세요.

권장 실패 코드:
- WHISPER_COMMAND_NOT_FOUND
- WHISPER_TIMEOUT
- WHISPER_EMPTY_OUTPUT
- AUDIO_UNSUPPORTED_OR_INVALID
- TRANSCRIPTION_UNEXPECTED_ERROR

권장 매핑:
- "Whisper CLI could not be executed." -> WHISPER_COMMAND_NOT_FOUND
- "Whisper transcription timed out." -> WHISPER_TIMEOUT
- "Whisper did not produce a JSON transcript." -> WHISPER_EMPTY_OUTPUT
- invalid audio, decode error, ffmpeg decode failure 계열 -> AUDIO_UNSUPPORTED_OR_INVALID
- 그 외 예외 -> TRANSCRIPTION_UNEXPECTED_ERROR

검증 명령:
backend/ 에서:
.\gradlew.bat test

frontend/ 에서:
cmd /c npm test
cmd /c npm run build

완료 기준:
- 기존 테스트와 새 테스트가 통과합니다.
- Frontend/Backend CI가 통과합니다.
- PR #57에 구현 요약과 검증 결과를 남깁니다.
```
