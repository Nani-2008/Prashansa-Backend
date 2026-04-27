Continue = 'Stop'
Write-Host "Creating session..."
 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
 = New-Object System.Net.Cookie("prashansa_session", "dummy", "/", "localhost")
.Cookies.Add()

Write-Host "Registering admin..."
 = @{
    name = "Admin User"
    phone = "9999999999"
    password = "password"
    role = "admin"
} | ConvertTo-Json
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Body  -ContentType "application/json" -WebSession  -ErrorAction SilentlyContinue | Out-Null
} catch {}

Write-Host "Logging in..."
 = @{
    phone = "9999999999"
    password = "password"
} | ConvertTo-Json
 = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Body  -ContentType "application/json" -WebSession 
Write-Host "Login response: "

Write-Host "Creating complaint..."
 = @{
    type = "Harassment"
    reporterPhone = "9999999999"
    reporterRole = "victim"
} | ConvertTo-Json
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints" -Method POST -Body  -ContentType "application/json" -WebSession 
 = .Content | ConvertFrom-Json
 = .id
Write-Host "Created complaint with ID: "

Write-Host "Getting complaints..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints" -Method GET -WebSession 
 = .Content | ConvertFrom-Json
Write-Host "Complaints count before delete: 0"

Write-Host "Deleting complaint ..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints/" -Method DELETE -WebSession 
Write-Host "Delete response: "

Write-Host "Getting complaints after delete..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints" -Method GET -WebSession 
 = .Content | ConvertFrom-Json
Write-Host "Complaints count after delete: 0"
