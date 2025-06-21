//// ApiResponse.java
//@Data
//@AllArgsConstructor
//public class ApiResponse<T> {
//    private Integer code;
//    private String message;
//    private T data;
//
//    public static <T> ApiResponse<T> success(String message) {
//        return new ApiResponse<>(200, message, null);
//    }
//
//    public static <T> ApiResponse<T> success(T data) {
//        return new ApiResponse<>(200, "success", data);
//    }
//
//    public static <T> ApiResponse<T> success(String message, T data) {
//        return new ApiResponse<>(200, message, data);
//    }
//
//    public static <T> ApiResponse<T> error(String message) {
//        return new ApiResponse<>(500, message, null);
//    }
//
//    public static <T> ApiResponse<T> error(Integer code, String message) {
//        return new ApiResponse<>(code, message, null);
//    }
//}
//
//// UserLoginRequest.java
//@Data
//public class UserLoginRequest {
//    @NotBlank(message = "用户名不能为空")
//    private String username;
//
//    @NotBlank(message = "密码不能为空")
//    private String password;
//}
//
//// UserRegisterRequest.java
//@Data
//public class UserRegisterRequest {
//    @NotBlank(message = "用户名不能为空")
//    @Size(min = 4, max = 50, message = "用户名长度必须在4-50之间")
//    private String username;
//
//    @NotBlank(message = "密码不能为空")
//    @Size(min = 6, message = "密码长度不能少于6位")
//    private String password;
//
//    @Email(message = "邮箱格式不正确")
//    private String email;
//
//    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
//    private String phone;
//}