@echo off
chcp 65001 >nul
title 系统清理工具 v1.0
color 0A

echo ========================================
echo           系统清理工具 v1.0
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 已获取管理员权限
) else (
    echo [×] 需要管理员权限，请右键以管理员身份运行
    pause
    exit /b 1
)

echo.
echo 开始系统清理...
echo ========================================

:: 1. 清理临时文件
echo [1/8] 清理临时文件...
if exist "%temp%" (
    del /q /f "%temp%\*.*" 2>nul
    echo    √ 用户临时文件已清理
)
if exist "C:\Windows\Temp" (
    del /q /f "C:\Windows\Temp\*.*" 2>nul
    echo    √ 系统临时文件已清理
)

:: 2. 清理Windows更新缓存
echo [2/8] 清理Windows更新缓存...
DISM /Online /Cleanup-Image /StartComponentCleanup /ResetBase >nul 2>&1
if %errorLevel% == 0 (
    echo    √ Windows更新缓存已清理
) else (
    echo    ! Windows更新缓存清理失败（可能正在使用中）
)

:: 3. 清理系统还原点（保留最新的3个）
echo [3/8] 清理系统还原点...
for /f "tokens=*" %%i in ('vssadmin list shadows /for=C: ^| find "Shadow Copy ID"') do (
    set "shadow_id=%%i"
    set "shadow_id=!shadow_id:Shadow Copy ID: =!"
    vssadmin delete shadows /shadow=!shadow_id! >nul 2>&1
)
echo    √ 旧系统还原点已清理

:: 4. 清理Windows日志
echo [4/8] 清理Windows日志...
for /f "tokens=*" %%i in ('wevtutil el') do (
    wevtutil cl "%%i" >nul 2>&1
)
echo    √ Windows日志已清理

:: 5. 清理npm缓存
echo [5/8] 清理npm缓存...
where npm >nul 2>&1
if %errorLevel% == 0 (
    npm cache clean --force >nul 2>&1
    echo    √ npm缓存已清理
) else (
    echo    ! npm未安装，跳过npm缓存清理
)

:: 6. 清理Gradle缓存
echo [6/8] 清理Gradle缓存...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
    echo    √ Gradle缓存已清理
)
if exist "%USERPROFILE%\.gradle\daemon" (
    rmdir /s /q "%USERPROFILE%\.gradle\daemon" 2>nul
    echo    √ Gradle守护进程缓存已清理
)

:: 7. 清理Maven本地仓库（可选）
echo [7/8] 清理Maven本地仓库...
if exist "%USERPROFILE%\.m2\repository" (
    echo    是否清理Maven本地仓库？这将删除所有下载的依赖包
    echo    清理后需要重新下载项目依赖，但可以释放约3GB空间
    set /p choice="    输入 Y 确认清理，输入 N 跳过 (Y/N): "
    if /i "!choice!"=="Y" (
        rmdir /s /q "%USERPROFILE%\.m2\repository" 2>nul
        echo    √ Maven本地仓库已清理
    ) else (
        echo    - 跳过Maven本地仓库清理
    )
)

:: 8. 清理回收站
echo [8/8] 清理回收站...
powershell -command "Clear-RecycleBin -Force" >nul 2>&1
echo    √ 回收站已清理

echo.
echo ========================================
echo 清理完成！
echo ========================================

:: 显示清理结果
echo.
echo 清理项目统计：
echo - 临时文件
echo - Windows更新缓存
echo - 系统还原点（保留最新）
echo - Windows日志
echo - npm缓存
echo - Gradle缓存
echo - Maven本地仓库（可选）
echo - 回收站

echo.
echo 建议：
echo 1. 重启计算机以确保完全释放空间
echo 2. 定期运行此脚本保持系统清洁
echo 3. 使用磁盘清理工具进行深度清理

echo.
pause 