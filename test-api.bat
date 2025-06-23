@echo off
echo ========================================
echo 用户服务API测试脚本
echo ========================================

set BASE_URL=http://localhost:8082
set TOKEN=

echo.
echo 1. 测试用户注册...
curl -X POST %BASE_URL%/user/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"password123\",\"email\":\"test@example.com\",\"phone\":\"13800138000\"}"

echo.
echo.
echo 2. 测试用户登录...
for /f "tokens=*" %%i in ('curl -s -X POST %BASE_URL%/user/login -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"password123\"}"') do set LOGIN_RESPONSE=%%i

echo %LOGIN_RESPONSE%

echo.
echo 3. 测试健康检查...
curl -X GET %BASE_URL%/actuator/health

echo.
echo 4. 测试用户列表查询（需要登录）...
if not "%TOKEN%"=="" (
    curl -X GET %BASE_URL%/user/users ^
      -H "Authorization: Bearer %TOKEN%"
) else (
    echo 跳过：需要先登录获取token
)

echo.
echo 测试完成！
pause 