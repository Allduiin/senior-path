# Serve the repo and open the progress dashboard (Ctrl+C to stop).
# Requires JDK 18+ on PATH (jwebserver ships with the JDK).
Start-Process "http://localhost:8010/docs/dashboard/"
jwebserver -d $PSScriptRoot -b 127.0.0.1 -p 8010
