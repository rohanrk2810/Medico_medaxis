package com.medicalerp.identity;
import java.time.Instant;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
class ApiErrors {
 record Error(Instant timestamp,int status,String code,String message,String traceId,Map<String,String> fieldErrors){}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<Error> invalid(MethodArgumentNotValidException ex,HttpServletRequest request){
  Map<String,String> fields=new TreeMap<>();ex.getBindingResult().getFieldErrors().forEach(e->fields.put(e.getField(),"Invalid value"));
  return result(400,"VALIDATION_FAILED","Check the submitted fields",request,fields);
 }
 @ExceptionHandler(AccessDeniedException.class)
 ResponseEntity<Error> denied(HttpServletRequest request){return result(403,"ACCESS_DENIED","Access denied",request,Map.of());}
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<Error> status(ResponseStatusException ex,HttpServletRequest request){return result(ex.getStatusCode().value(),"REQUEST_REJECTED","Request could not be completed",request,Map.of());}
 @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
 ResponseEntity<Error> malformed(HttpServletRequest request){return result(400,"INVALID_REQUEST","Invalid request body",request,Map.of());}
 private ResponseEntity<Error> result(int status,String code,String message,HttpServletRequest request,Map<String,String> fields){
  String trace=request.getHeader("X-Correlation-ID");if(trace==null||!trace.matches("[a-zA-Z0-9-]{1,64}"))trace=UUID.randomUUID().toString();
  return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(new Error(Instant.now(),status,code,message,trace,fields));
 }
}
