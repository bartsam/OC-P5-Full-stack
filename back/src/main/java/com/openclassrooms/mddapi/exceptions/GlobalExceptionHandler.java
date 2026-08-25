package com.openclassrooms.mddapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.openclassrooms.mddapi.dto.MessageResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * User already exists in database (e.g. duplicate email): 409 CONFLICT
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<MessageResponse> handleUserAlreadyExists(
      UserAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new MessageResponse(exception.getMessage()));
  }

  /**
   * Invalid or missing field in @RequestBody with @Valid : 400 BAD_REQUEST
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<MessageResponse> handleValidationErrors(
      MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult()
        .getFieldErrors()
        .getFirst()
        .getDefaultMessage();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new MessageResponse(message));
  }

}
