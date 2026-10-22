<#
.SYNOPSIS
    Adds a 1-pixel black outline to a transparent pixel art image using ImageMagick.
.DESCRIPTION
    Takes an input image, expands its opaque shape by 1 pixel using morphology dilation,
    colors that expansion black, and layers the original image on top.
.PARAMETER InputPath
    Path to the source image (PNG recommended).
.PARAMETER OutputPath
    Path for the saved output image. Optional (defaults to filename_outlined.png).
#>

param(
    [Parameter(Mandatory=$true)]
    [ValidateScript({Test-Path $_ -PathType Leaf})]
    [string]$InputPath,

    [string]$OutputPath
)

# Ensure ImageMagick (magick) is installed and available in PATH
if (-not (Get-Command "magick" -ErrorAction SilentlyContinue)) {
    Write-Error "ImageMagick v7 ('magick') is not installed or not found in your system PATH."
    exit 1
}

# Auto-generate output path if not specified
if (-not $OutputPath) {
    $dir = [System.IO.Path]::GetDirectoryName($InputPath)
    $name = [System.IO.Path]::GetFileNameWithoutExtension($InputPath)
    $ext = [System.IO.Path]::GetExtension($InputPath)
    if (-not $dir) { $dir = "." }
    $OutputPath = Join-Path $dir "$($name)_outlined$($ext)"
}

Write-Host "Processing: $InputPath ->$OutputPath" -ForegroundColor Cyan

# ImageMagick pipeline breakdown:
# 1. -bordercolor none -border 1 : Adds a 1px transparent buffer so edge pixels can be outlined.
# 2. '(' +clone ... ')'          : Creates the 1-pixel expanded black silhouette outline.
# 3. -compose DstOver -composite : Places the original image over the black outline.
# 4. -trim +repage               : Crops away all remaining empty transparent space to make it as small as possible.
magick $InputPath `
    -trim -sample 6x6 `
    -bordercolor none -border 1 `
    '(' +clone -alpha extract -morphology Dilate Diamond -background black -alpha shape ')' `
    -compose DstOver -composite `
    -trim +repage $OutputPath

if ($LASTEXITCODE -eq 0) {
    Write-Host "Successfully generated outline! Saved to: $OutputPath" -ForegroundColor Green
} else {
    Write-Error "ImageMagick processing failed."
}