# Shape: Phase 2C Generated Note Draft

## User Flow

1. User opens a memo with a completed transcript.
2. User clicks a generate note action in the memo detail area.
3. Backend validates memo ownership and completed transcript availability.
4. Backend asks the local generator for a draft note.
5. Backend stores the generated note separately from the transcript.
6. Frontend renders summary, key points, and action items.
7. User can refresh/read the stored generated note later.

## API Shape

```http
POST /api/memos/{memoId}/generated-note
```

Generates or replaces the current draft note for the memo.

```http
GET /api/memos/{memoId}/generated-note
```

Returns the current generated note state.

Response:

```json
{
  "memoId": "33333333-3333-3333-3333-333333333333",
  "status": "GENERATED",
  "summary": "The team reviewed launch strategy and risks.",
  "keyPoints": ["Launch risks", "Roadmap sequencing"],
  "actionItems": ["Follow up on risk owners"],
  "updatedAt": "2026-05-27T15:30:00Z",
  "failureReason": null
}
```

## Local Generator Shape

For this slice, keep the heuristic simple:

- Summary: first meaningful sentence or compact opening excerpt.
- Key points: up to three meaningful sentences or segment-like lines.
- Action items: sentences with action-oriented words such as `action`, `todo`, `follow up`, `next`, `해야`, or `확인`.

The exact heuristic can evolve, but tests should verify stable behavior and separation from transcript persistence.

## Frontend Shape

- Keep generated notes inside the selected memo detail workflow.
- Do not create a separate notes workspace.
- Show an explicit generate button only when a memo is selected.
- Render generated note sections compactly under the transcript area.
