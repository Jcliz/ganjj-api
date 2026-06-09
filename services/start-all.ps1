# Inicia todos os microsserviços em janelas separadas do PowerShell
$base = Split-Path -Parent $PSScriptRoot

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\auth-service'; node index.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\product-service'; node index.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\cart-service'; node index.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\order-service'; node index.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\dashboard-service'; node index.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\services\api-gateway'; node index.js"

Write-Host "Todos os servicos iniciados!"
Write-Host "  auth-service      -> http://localhost:3001"
Write-Host "  product-service   -> http://localhost:3002"
Write-Host "  cart-service      -> http://localhost:3003"
Write-Host "  order-service     -> http://localhost:3004"
Write-Host "  dashboard-service -> http://localhost:3005"
Write-Host "  api-gateway       -> http://localhost:3000"
