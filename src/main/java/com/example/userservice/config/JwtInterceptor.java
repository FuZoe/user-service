//// JwtInterceptor.java
//@Component
//@Slf4j
//public class JwtInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    private static final List<String> WHITELIST = Arrays.asList(
//            "/user/register",
//            "/user/login"
//    );
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 跳过白名单
//        if (WHITELIST.stream().anyMatch(path -> request.getRequestURI().endsWith(path))) {
//            return true;
//        }
//
//        // 获取token
//        String token = request.getHeader("Authorization");
//        if (token == null || !token.startsWith("Bearer ")) {
//            throw new RuntimeException("未登录或token无效");
//        }
//
//        // 验证token
//        token = token.substring(7);
//        if (!jwtUtil.validateToken(token)) {
//            throw new RuntimeException("token已过期或无效");
//        }
//
//        // 传递用户信息
//        request.setAttribute("userId", jwtUtil.extractUserId(token));
//        request.setAttribute("username", jwtUtil.extractUsername(token));
//
//        return true;
//    }
//}