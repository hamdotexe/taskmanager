<#
Builds OEL-Report.pdf from OEL-Report.tex with Tectonic, then lists what is still missing from the report.

    powershell -ExecutionPolicy Bypass -File report\build.ps1          build, and list what is missing
    powershell -ExecutionPolicy Bypass -File report\build.ps1 -Final   refuse to build while anything is missing

Tectonic is taken from the TECTONIC environment variable, or else from ..\..\tools\tectonic\tectonic.exe (outside
the repository). Its first run downloads the TeX files it needs into its cache; later runs work offline.
#>
param([switch]$Final)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$tectonic = if ($env:TECTONIC) { $env:TECTONIC } else { Join-Path $PSScriptRoot '..\..\tools\tectonic\tectonic.exe' }
if (-not (Test-Path $tectonic)) {
    Write-Error "Tectonic not found at $tectonic; set TECTONIC to the path of tectonic.exe"
    exit 1
}

# What is still missing: screenshots not yet saved, \gap{...} text not yet written, and "% VERIFY" notes not yet
# checked against the code. Comment lines are skipped, except for the VERIFY notes, which are comments.
$sources = Get-ChildItem -Path $PSScriptRoot -Filter *.tex -Recurse | Where-Object Name -ne 'OEL-Report.tex'
$shots = $sources | Select-String -Pattern '\\screenshot(?:wide)?\{([a-z0-9-]+)\}' |
    ForEach-Object { $_.Matches[0].Groups[1].Value }
$missing = @($shots | Where-Object { -not (Test-Path (Join-Path $PSScriptRoot "screenshots\$_.png")) })
$gaps = @($sources | Select-String -Pattern '\\gap\{' | Where-Object { $_.Line -notmatch '^\s*%' })
# a transcript or source file the report quotes but cannot find prints as a gap too
$gaps += @($sources | Select-String -Pattern '\\(evidence|source)(?:\[[^\]]*\])?\{([^}]+)\}' | Where-Object {
    $m = $_.Matches[0]
    $folder = if ($m.Groups[1].Value -eq 'evidence') { '..\..\evidence' } else { '..' }
    $_.Line -notmatch '^\s*%' -and -not (Test-Path (Join-Path $PSScriptRoot (Join-Path $folder $m.Groups[2].Value)))
})
$verify = @($sources | Select-String -Pattern '^\s*%\s*VERIFY')

function Write-Lines($found) {
    $found | Group-Object Path | ForEach-Object {
        Write-Host ("  {0}: line {1}" -f (Resolve-Path -Relative $_.Name), (($_.Group | ForEach-Object LineNumber) -join ', '))
    }
}

function Show-Missing {
    Write-Host ''
    Write-Host ("Screenshots: {0} in the report, {1} missing" -f @($shots).Count, $missing.Count)
    $missing | ForEach-Object { Write-Host "  screenshots\$_.png" }
    Write-Host ("Unwritten text (\gap): {0}" -f $gaps.Count)
    Write-Lines $gaps
    Write-Host ("Unchecked notes (% VERIFY): {0}" -f $verify.Count)
    Write-Lines $verify
}

# The commit table is written from the live history at every build, so it never falls behind the repository. Without
# git or outside a repository, the last one written is kept.
function ConvertTo-Tex([string]$text) {
    $mark = [string][char]1
    $text = $text -replace '\\', $mark
    $text = $text -replace '([&%$#_{}])', '\$1'
    $text = $text -replace '~', '\textasciitilde{}' -replace '\^', '\textasciicircum{}'
    return $text -replace $mark, '\textbackslash{}'
}
$repo = Split-Path $PSScriptRoot -Parent
$log = @(git -C $repo log --reverse --date=format:'%d %b %Y, %H:%M' --format='%h%x09%ad%x09%s' 2>$null)
if ($LASTEXITCODE -eq 0 -and $log.Count -gt 0) {
    $rows = for ($i = 0; $i -lt $log.Count; $i++) {
        $hash, $date, $subject = $log[$i] -split "`t", 3
        '    {0} & \texttt{{{1}}} & {2} & {3} \\' -f ($i + 1), $hash, $date, (ConvertTo-Tex $subject)
    }
    $first = ($log[0] -split "`t")[1]
    $last = ($log[-1] -split "`t")[1]
    $tex = @(
        '% Written by build.ps1 from git log; do not edit.'
        "\newcommand{\CommitCount}{$($log.Count)}"
        "\newcommand{\FirstCommitDate}{$first}"
        "\newcommand{\LastCommitDate}{$last}"
        '\newcommand{\CommitRows}{%'
    ) + $rows + '}'
    New-Item -ItemType Directory -Force (Join-Path $PSScriptRoot 'generated') | Out-Null
    [IO.File]::WriteAllText((Join-Path $PSScriptRoot 'generated\history.tex'), ($tex -join "`n") + "`n",
        (New-Object Text.UTF8Encoding $false))
}

if ($Final -and ($missing.Count + $gaps.Count + $verify.Count) -gt 0) {
    Show-Missing
    Write-Error 'Not building the final report while anything above is missing.'
    exit 1
}

& $tectonic -X compile OEL-Report.tex
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Write-Host ''
Write-Host ("Built {0}" -f (Join-Path $PSScriptRoot 'OEL-Report.pdf'))
Show-Missing
