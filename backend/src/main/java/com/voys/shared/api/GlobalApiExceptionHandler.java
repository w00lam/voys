package com.voys.shared.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.voys.identity.domain.DuplicateEmailException;
import com.voys.memo.application.StoragePort.StorageException;
import com.voys.memo.domain.InvalidMemoFolderException;
import com.voys.memo.domain.InvalidMemoTitleException;
import com.voys.memo.domain.InvalidRecordingException;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.notes.domain.GeneratedNoteNotReadyException;
import com.voys.search.domain.InvalidSearchQueryException;
import com.voys.transcription.domain.TranscriptionAlreadyRunningException;
import com.voys.transcription.domain.TranscriptionFailedException;
import com.voys.transcription.domain.TranscriptionRetryNotAllowedException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

	@ExceptionHandler(DuplicateEmailException.class)
	ResponseEntity<ApiErrorResponse> duplicateEmail() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ApiErrorResponse("auth.email_taken", "Email is already registered."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception) {
		Map<String, Object> fields = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(
				FieldError::getField,
				error -> error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage(),
				(left, right) -> left
			));

		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("request.validation_failed", "Request validation failed.", fields));
	}

	@ExceptionHandler(InvalidRecordingException.class)
	ResponseEntity<ApiErrorResponse> invalidRecording(InvalidRecordingException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("recording.invalid", exception.getMessage()));
	}

	@ExceptionHandler(StorageException.class)
	ResponseEntity<ApiErrorResponse> storageFailure() {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ApiErrorResponse("storage.failed", "Recording could not be stored."));
	}

	@ExceptionHandler(MemoNotFoundException.class)
	ResponseEntity<ApiErrorResponse> memoNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ApiErrorResponse("memo.not_found", "Memo was not found."));
	}

	@ExceptionHandler(TranscriptionFailedException.class)
	ResponseEntity<ApiErrorResponse> transcriptionFailed(TranscriptionFailedException exception) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
			.body(new ApiErrorResponse("transcription.failed", "Transcription failed."));
	}

	@ExceptionHandler(TranscriptionAlreadyRunningException.class)
	ResponseEntity<ApiErrorResponse> transcriptionAlreadyRunning(TranscriptionAlreadyRunningException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ApiErrorResponse("transcription.already_running", exception.getMessage()));
	}

	@ExceptionHandler(TranscriptionRetryNotAllowedException.class)
	ResponseEntity<ApiErrorResponse> transcriptionRetryNotAllowed() {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ApiErrorResponse(
				"transcription.retry_not_allowed",
				"Transcription can be retried only after a failed attempt."
			));
	}

	@ExceptionHandler(InvalidSearchQueryException.class)
	ResponseEntity<ApiErrorResponse> invalidSearchQuery(InvalidSearchQueryException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("search.query_blank", exception.getMessage()));
	}

	@ExceptionHandler(InvalidMemoTitleException.class)
	ResponseEntity<ApiErrorResponse> invalidMemoTitle(InvalidMemoTitleException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("memo.invalid_title", exception.getMessage()));
	}

	@ExceptionHandler(InvalidMemoFolderException.class)
	ResponseEntity<ApiErrorResponse> invalidMemoFolder(InvalidMemoFolderException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("memo.invalid_folder", exception.getMessage()));
	}

	@ExceptionHandler(GeneratedNoteNotReadyException.class)
	ResponseEntity<ApiErrorResponse> generatedNoteNotReady(GeneratedNoteNotReadyException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ApiErrorResponse("note.not_ready", exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiErrorResponse> illegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.badRequest()
			.body(new ApiErrorResponse("note.invalid_argument", exception.getMessage()));
	}
}
