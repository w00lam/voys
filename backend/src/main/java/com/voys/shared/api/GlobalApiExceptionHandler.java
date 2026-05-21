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
import com.voys.memo.domain.InvalidRecordingException;

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
}
