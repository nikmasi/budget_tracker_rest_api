package org.example.budget_tracker_api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import com.fasterxml.jackson.databind.JsonMappingException
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, String>> {

        val errors = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Invalid value.")
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        exception: ResponseStatusException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val status = exception.statusCode

        val path = (request as? ServletWebRequest) ?.request ?.requestURI ?: ""

        val body = ErrorResponse(
            status = status.value(),
            error = status.toString().substringAfter(" "),
            message = exception.reason ?: "Request failed.",
            path = path,
            timestamp = Instant.now()
        )

        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val path = (request as? ServletWebRequest)
            ?.request
            ?.requestURI ?: ""

        val messageText = ex.mostSpecificCause.message.orEmpty()

        val field = Regex("""CategoryRequest\["([^"]+)"\]""")
            .find(messageText)
            ?.groupValues
            ?.get(1)

        val message = if (field != null) {
            "Invalid value for field '$field'. Expected a string."
        } else {
            "Invalid JSON request body. Check field types and required values."
        }

        val body = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = message,
            path = path,
            timestamp = Instant.now()
        )

        return ResponseEntity.badRequest().body(body)
    }
}