[System.Threading.Thread]::CurrentThread.CurrentCulture = [System.Globalization.CultureInfo]::InvariantCulture
$env:MAVEN_OPTS = "-Duser.language=en -Duser.country=US"

$strategies = @(
    @{name="SEQUENTIAL"; pool=$null},
    @{name="FIXED"; pool=2},
    @{name="FIXED"; pool=4},
    @{name="FIXED"; pool=8},
    @{name="VIRTUAL"; pool=$null}
)
$ip = "202.24.34.55"
$warmups = 2
$runs = 5

"scenario,strategy,pool_size,run,elapsed_ms,matches,consulted_providers" | Out-File results/results.csv -Encoding utf8

foreach ($io in @("true","false")) {
    foreach ($s in $strategies) {
        if ($s.name -eq "FIXED") {
            $argsStr = "FIXED $ip $io $warmups $runs $($s.pool)"
        } else {
            $argsStr = "$($s.name) $ip $io $warmups $runs"
        }
        Write-Host "Running: $argsStr"
        $output = mvn -q exec:java "-Dexec.args=$argsStr"
        $output | Select-String -Pattern "^(IO|NoIO)," | ForEach-Object { $_.Line } | Out-File -Append results/results.csv -Encoding utf8
    }
}

Write-Host "Listo. Resultados en results/results.csv"