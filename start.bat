@echo off
echo ========================================
echo 用户权限管理系统 - 用户服务启动脚本
echo ========================================

echo.
echo 正在检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)

echo.
echo 正在检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请先安装Maven 3.6+
    pause
    exit /b 1
)

echo.
echo 正在编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 项目编译失败
    pause
    exit /b 1
)

echo.
echo 正在启动用户服务...
echo 服务地址: http://localhost:8082
echo 健康检查: http://localhost:8082/actuator/health
echo.
echo 按 Ctrl+C 停止服务
echo.

call mvn spring-boot:run

pause 