Continue = 'Stop'

# Register admin
 = @{
    name = "Admin User"
    phone = "9999999999"
    password = "password"
    role = "admin"
} | ConvertTo-Json
Write-Host "Registering admin..."
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Body  -ContentType "application/json" -SessionVariable session -ErrorAction SilentlyContinue | Out-Null
} catch {}

# Login admin
 = @{
    phone = "9999999999"
    password = "password"
} | ConvertTo-Json
Write-Host "Logging in..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Body  -ContentType "application/json" -WebSession 
Write-Host "Login response: "

# Create a complaint
 = @{
    type = "Harassment"
    reporterPhone = "9999999999"
    reporterRole = "victim"
} | ConvertTo-Json
Write-Host "Creating complaint..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints" -Method POST -Body  -ContentType "application/json" -WebSession 
 = (.Content | ConvertFrom-Json).id
Write-Host "Created complaint with ID: "

# Delete complaint
Write-Host "Deleting complaint ..."
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints/" -Method DELETE -WebSession 
Write-Host "Delete response: "

# Verify deletion
 = Invoke-WebRequest -Uri "http://localhost:8080/api/complaints" -Method GET -WebSession 
 = .Content | ConvertFrom-Json
 =  | Where-Object { $_.id -eq  }
Write-Host "Is complaint still in list? False"
