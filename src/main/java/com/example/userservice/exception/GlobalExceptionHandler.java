//// GlobalExceptionHandler.java
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
//        String message = e.getBindingResult().getFieldErrors()
//                .stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//        return ApiResponse.error(400, message);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ApiResponse<?> handleException(Exception e) {
//        log.error("系统异常", e);
//        return ApiResponse.error("系统繁忙，请稍后重试");
//    }
//}