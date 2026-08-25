package com.openclassrooms.mddapi.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.openclassrooms.mddapi.dto.MessageResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Invalid email/password during login: 401 UNAUTHORIZED
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<MessageResponse> handleBadCredentials(BadCredentialsException ex) {

    logger.warn("Authentication failed: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new MessageResponse("Invalid email or password"));
  }

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

    logger.info("Validation error: {}", message);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new MessageResponse(message));
  }

  /**
   * Any unexpected error not handled by other handlers: 500 INTERNAL_SERVER_ERROR
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {

    logger.error("Unexpected error occurred", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new MessageResponse("An internal error has occurred"));
  }

}
