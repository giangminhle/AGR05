@echo off
rem Dong goi AGR-05 thanh mot file jar (API + giao dien) trong thu muc phat-hanh\
rem Chay: dong-goi.bat   (can JDK, Maven, Node.js)
setlocal
cd /d "%~dp0"

echo [1/3] Build giao dien React...
pushd frontend
call npm install --no-audit --no-fund || goto loi
call npm run build || goto loi
popd

echo [2/3] Build backend kem giao dien, chay test...
pushd backend
call mvn -q package || goto loi
popd

echo [3/3] Tao thu muc phat-hanh...
if not exist phat-hanh mkdir phat-hanh
copy /y backend\target\agr05.jar phat-hanh\agr05.jar >nul || goto loi
if not exist phat-hanh\application.yml (
  if exist backend\src\main\resources\application.yml (
    copy backend\src\main\resources\application.yml phat-hanh\application.yml >nul
  ) else (
    copy backend\src\main\resources\application-example.yml phat-hanh\application.yml >nul
    echo   Da tao phat-hanh\application.yml tu file mau: hay sua chuoi ket noi CSDL truoc khi chay.
  )
)
if exist backend\du-lieu if not exist phat-hanh\du-lieu xcopy /e /i /q backend\du-lieu phat-hanh\du-lieu >nul
> phat-hanh\chay.bat (
  echo @echo off
  echo cd /d "%%~dp0"
  echo java -Dstdout.encoding=UTF-8 -jar agr05.jar
  echo pause
)

echo.
echo Xong. Chay phat-hanh\chay.bat roi mo trinh duyet vao http://localhost:^<cong trong application.yml^>
echo (mac dinh 8080; may nay dung 8081)
exit /b 0

:loi
echo.
echo LOI: dong goi that bai, xem thong bao phia tren.
exit /b 1
