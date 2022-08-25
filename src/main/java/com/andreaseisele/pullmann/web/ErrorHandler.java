package com.andreaseisele.pullmann.web;

import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ModelAndView handleValidationError(ConstraintViolationException exception) {
        final Map<String, String> model = Map.of("validationMessage", exception.getMessage());
        return new ModelAndView("validation-error", model, HttpStatus.BAD_REQUEST);
    }

}
