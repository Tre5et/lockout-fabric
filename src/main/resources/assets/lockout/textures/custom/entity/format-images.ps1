mkdir -Force old;
Get-ChildItem -Path ".\*.png" -Recurse | Move-Item -Destination old
cd old;
Get-ChildItem -File | Where-Object Extension -match '\.png$' | ForEach-Object { magick $_.FullName -background transparent -trim +repage -gravity center -extent '%[fx:max(w,h)]x%[fx:max(w,h)]' "../$($_.BaseName).png" }
cd ..
Remove-Item old -Recurse;