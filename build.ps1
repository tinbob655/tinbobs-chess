param(
  [string]$BinaryName = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

#find out what the output binary should be called
$extraArgs = @()
for ($i = 0; $i -lt $args.Count; $i++) {
  $arg = $args[$i]
  if ($arg -like "--binary-name=*") {
    if (-not $BinaryName) {
      $BinaryName = $arg.Substring("--binary-name=".Length)
    }
    continue
  }
  if ($arg -eq "--binary-name") {
    if ($i + 1 -ge $args.Count) {
      throw "Missing value for --binary-name"
    }
    if (-not $BinaryName) {
      $BinaryName = $args[$i + 1]
    }
    $i++
    continue
  }
  $extraArgs += $arg
}

if ($extraArgs.Count -gt 0) {
  throw "Unknown argument(s): $($extraArgs -join ' ')"
}

$rootDir = $PSScriptRoot
if (-not $rootDir) {
  $rootDir = (Get-Location).Path
}

$serverDir = Join-Path $rootDir "server"
$clientDir = Join-Path $rootDir "client"
$buildDir = Join-Path $rootDir "build"



#==========================================================================
#           BUILD SERVER PHASE
#==========================================================================

#build the server using maven
Write-Host ""
Write-Host "Building server..."
$mvnw = Join-Path $serverDir "mvnw.cmd"
$mvnCmd = $null
if (Test-Path $mvnw) {
  $mvnCmd = $mvnw
} else {
  $mvn = Get-Command mvn -ErrorAction SilentlyContinue
  if ($mvn) {
    $mvnCmd = $mvn.Source
  }
}

if (-not $mvnCmd) {
  throw "Maven not found. Install Maven or use the Maven wrapper (server/mvnw.cmd)."
}

Push-Location $serverDir
try {
  & $mvnCmd clean package
  if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
  }
} finally {
  Pop-Location
}

$targetDir = Join-Path $serverDir "target"
$jar = Get-ChildItem -Path $targetDir -Filter "*.jar" |
  Where-Object { $_.Name -notlike "*.jar.original" } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1

if (-not $jar) {
  throw "No backend jar found in $targetDir"
}

$serverJar = Join-Path $targetDir "server.jar"
if ($jar.FullName -ne $serverJar) {
  Move-Item -Path $jar.FullName -Destination $serverJar -Force
}



#==========================================================================
#           BUILD CLIENT PHASE
#==========================================================================

#build the client using node
Write-Host ""
Write-Host "Building client..."
Push-Location $clientDir
try {
  & npm run build
  if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
  }
} finally {
  Pop-Location
}



#==========================================================================
#           ZIPPING PHASE
#==========================================================================

#combine the server and client builds into a zip
Write-Host ""
Write-Host "Zipping artifacts..."
New-Item -ItemType Directory -Path $buildDir -Force | Out-Null

$zipName = if ($BinaryName) { "$BinaryName.zip" } else { "$(Split-Path $rootDir -Leaf).zip" }
$zipPath = Join-Path $buildDir $zipName
if (Test-Path $zipPath) {
  Remove-Item $zipPath -Force
}

$stagingDir = Join-Path ([System.IO.Path]::GetTempPath()) ("tinbobs-chess-build-" + [System.Guid]::NewGuid().ToString("N"))
$stagingClientDir = Join-Path $stagingDir "client"
New-Item -ItemType Directory -Path $stagingClientDir -Force | Out-Null

$distDir = Join-Path $clientDir "dist"
if (-not (Test-Path $distDir)) {
  throw "Client dist folder not found: $distDir"
}

Copy-Item -Path (Join-Path $distDir "*") -Destination $stagingClientDir -Recurse -Force
Copy-Item -Path $serverJar -Destination (Join-Path $stagingDir "server.jar") -Force

Compress-Archive -Path (Join-Path $stagingDir "*") -DestinationPath $zipPath -Force
Remove-Item $stagingDir -Recurse -Force


Write-Host ""
Write-Host "Done: $zipPath"