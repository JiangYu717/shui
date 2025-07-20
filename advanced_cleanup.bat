@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title 高级系统清理工具 v2.0
color 0B

echo ========================================
echo        高级系统清理工具 v2.0
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

:: 显示当前磁盘使用情况
echo.
echo 当前磁盘使用情况：
echo ========================================
for /f "tokens=1,2,3,4" %%a in ('wmic logicaldisk where "DeviceID='C:'" get size^,freespace /format:value') do (
    if "%%a"=="FreeSpace" set free=%%b
    if "%%a"=="Size" set total=%%b
)
set /a used=%total%-%free%
set /a free_gb=%free%/1073741824
set /a total_gb=%total%/1073741824
set /a used_gb=%used%/1073741824
echo C盘总空间: %total_gb% GB
echo 已使用空间: %used_gb% GB
echo 可用空间: %free_gb% GB
echo 使用率: %used_gb%/%total_gb% GB

echo.
echo 选择清理模式：
echo 1. 快速清理（推荐）
echo 2. 深度清理（需要更多时间）
echo 3. 自定义清理
echo 4. 仅分析磁盘使用情况
echo.
set /p mode="请选择模式 (1-4): "

if "%mode%"=="1" goto quick_clean
if "%mode%"=="2" goto deep_clean
if "%mode%"=="3" goto custom_clean
if "%mode%"=="4" goto analyze_only
goto end

:quick_clean
echo.
echo 开始快速清理...
echo ========================================

:: 清理临时文件
echo [1/6] 清理临时文件...
if exist "%temp%" (
    del /q /f "%temp%\*.*" 2>nul
    echo    √ 用户临时文件已清理
)
if exist "C:\Windows\Temp" (
    del /q /f "C:\Windows\Temp\*.*" 2>nul
    echo    √ 系统临时文件已清理
)

:: 清理npm缓存
echo [2/6] 清理npm缓存...
where npm >nul 2>&1
if %errorLevel% == 0 (
    npm cache clean --force >nul 2>&1
    echo    √ npm缓存已清理
)

:: 清理Gradle缓存
echo [3/6] 清理Gradle缓存...
if exist "%USERPROFILE%\.gradle\caches" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
    echo    √ Gradle缓存已清理
)

:: 清理回收站
echo [4/6] 清理回收站...
powershell -command "Clear-RecycleBin -Force" >nul 2>&1
echo    √ 回收站已清理

:: 清理Windows日志
echo [5/6] 清理Windows日志...
for /f "tokens=*" %%i in ('wevtutil el') do (
    wevtutil cl "%%i" >nul 2>&1
)
echo    √ Windows日志已清理

:: 清理浏览器缓存
echo [6/6] 清理浏览器缓存...
if exist "%LOCALAPPDATA%\Google\Chrome\User Data\Default\Cache" (
    rmdir /s /q "%LOCALAPPDATA%\Google\Chrome\User Data\Default\Cache" 2>nul
    echo    √ Chrome缓存已清理
)
if exist "%LOCALAPPDATA%\Microsoft\Edge\User Data\Default\Cache" (
    rmdir /s /q "%LOCALAPPDATA%\Microsoft\Edge\User Data\Default\Cache" 2>nul
    echo    √ Edge缓存已清理
)

goto show_result

:deep_clean
echo.
echo 开始深度清理...
echo ========================================

:: 执行快速清理的所有项目
call :quick_clean

:: 额外深度清理项目
echo [7/8] 清理Windows更新缓存...
DISM /Online /Cleanup-Image /StartComponentCleanup /ResetBase >nul 2>&1
echo    √ Windows更新缓存已清理

echo [8/8] 清理系统还原点...
for /f "tokens=*" %%i in ('vssadmin list shadows /for=C: ^| find "Shadow Copy ID"') do (
    set "shadow_id=%%i"
    set "shadow_id=!shadow_id:Shadow Copy ID: =!"
    vssadmin delete shadows /shadow=!shadow_id! >nul 2>&1
)
echo    √ 旧系统还原点已清理

goto show_result

:custom_clean
echo.
echo 自定义清理选项：
echo ========================================
echo 1. 清理临时文件
echo 2. 清理npm缓存
echo 3. 清理Gradle缓存
echo 4. 清理Maven本地仓库
echo 5. 清理Windows更新缓存
echo 6. 清理系统还原点
echo 7. 清理Windows日志
echo 8. 清理回收站
echo 9. 清理浏览器缓存
echo 0. 执行所有清理
echo.
set /p choices="请选择要清理的项目（用逗号分隔，如：1,3,5 或输入0执行所有）: "

if "%choices%"=="0" (
    call :deep_clean
    goto show_result
)

for %%i in (%choices%) do (
    if "%%i"=="1" (
        echo 清理临时文件...
        del /q /f "%temp%\*.*" 2>nul
        del /q /f "C:\Windows\Temp\*.*" 2>nul
        echo    √ 临时文件已清理
    )
    if "%%i"=="2" (
        echo 清理npm缓存...
        npm cache clean --force >nul 2>&1
        echo    √ npm缓存已清理
    )
    if "%%i"=="3" (
        echo 清理Gradle缓存...
        rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
        echo    √ Gradle缓存已清理
    )
    if "%%i"=="4" (
        echo 清理Maven本地仓库...
        rmdir /s /q "%USERPROFILE%\.m2\repository" 2>nul
        echo    √ Maven本地仓库已清理
    )
    if "%%i"=="5" (
        echo 清理Windows更新缓存...
        DISM /Online /Cleanup-Image /StartComponentCleanup /ResetBase >nul 2>&1
        echo    √ Windows更新缓存已清理
    )
    if "%%i"=="6" (
        echo 清理系统还原点...
        for /f "tokens=*" %%j in ('vssadmin list shadows /for=C: ^| find "Shadow Copy ID"') do (
            set "shadow_id=%%j"
            set "shadow_id=!shadow_id:Shadow Copy ID: =!"
            vssadmin delete shadows /shadow=!shadow_id! >nul 2>&1
        )
        echo    √ 系统还原点已清理
    )
    if "%%i"=="7" (
        echo 清理Windows日志...
        for /f "tokens=*" %%j in ('wevtutil el') do (
            wevtutil cl "%%j" >nul 2>&1
        )
        echo    √ Windows日志已清理
    )
    if "%%i"=="8" (
        echo 清理回收站...
        powershell -command "Clear-RecycleBin -Force" >nul 2>&1
        echo    √ 回收站已清理
    )
    if "%%i"=="9" (
        echo 清理浏览器缓存...
        rmdir /s /q "%LOCALAPPDATA%\Google\Chrome\User Data\Default\Cache" 2>nul
        rmdir /s /q "%LOCALAPPDATA%\Microsoft\Edge\User Data\Default\Cache" 2>nul
        echo    √ 浏览器缓存已清理
    )
)

goto show_result

:analyze_only
echo.
echo 磁盘使用情况详细分析：
echo ========================================

:: 分析大文件夹
echo 大文件夹分析：
echo.

if exist "%USERPROFILE%\.gradle" (
    for /f "tokens=*" %%a in ('dir "%USERPROFILE%\.gradle" /s 2^>nul ^| find "个文件"') do (
        echo Gradle缓存: %%a
    )
)

if exist "%USERPROFILE%\.m2" (
    for /f "tokens=*" %%a in ('dir "%USERPROFILE%\.m2" /s 2^>nul ^| find "个文件"') do (
        echo Maven仓库: %%a
    )
)

if exist "%temp%" (
    for /f "tokens=*" %%a in ('dir "%temp%" /s 2^>nul ^| find "个文件"') do (
        echo 临时文件: %%a
    )
)

echo.
echo 建议清理项目：
echo 1. Gradle缓存（约3GB）
echo 2. Maven本地仓库（约3GB）
echo 3. 临时文件（约1-2GB）
echo 4. npm缓存（约700MB）
echo 5. 浏览器缓存（约500MB-1GB）

goto end

:show_result
echo.
echo ========================================
echo 清理完成！
echo ========================================

:: 显示清理后的磁盘使用情况
echo.
echo 清理后磁盘使用情况：
echo ========================================
for /f "tokens=1,2,3,4" %%a in ('wmic logicaldisk where "DeviceID='C:'" get size^,freespace /format:value') do (
    if "%%a"=="FreeSpace" set free_after=%%b
    if "%%a"=="Size" set total_after=%%b
)
set /a freed_space=%free_after%-%free%
set /a freed_gb=%freed_space%/1073741824
set /a free_gb_after=%free_after%/1073741824
echo 释放空间: %freed_gb% GB
echo 当前可用空间: %free_gb_after% GB

echo.
echo 清理建议：
echo 1. 重启计算机以确保完全释放空间
echo 2. 每周运行一次快速清理
echo 3. 每月运行一次深度清理
echo 4. 定期检查大文件占用

:end
echo.
pause 