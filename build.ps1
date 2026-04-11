$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
$argList = @("-encoding", "UTF-8", "-cp", "lib\*", "-d", "bin") + $files
Write-Host "Compiling $($files.Count) files..."
& javac $argList
if ($LASTEXITCODE -eq 0) {
    Write-Host "BUILD SUCCESS" -ForegroundColor Green
} else {
    Write-Host "BUILD FAILED" -ForegroundColor Red
}
