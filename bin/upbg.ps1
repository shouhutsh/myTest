#
# 该PowerShell脚本可以在windows下更换必应每日一图壁纸
# （你可能需要先运行powershell，执行：set-ExecutionPolicy RemoteSigned 以开启执行脚本权限）
#

function Set-Wallpaper
{
param(
[Parameter(Mandatory=$true)]
$Path,
[ValidateSet('Center', 'Stretch')]
$Style = 'Stretch'
)
Add-Type @"
using System;
using System.Runtime.InteropServices;
using Microsoft.Win32;
namespace Wallpaper
{
public enum Style : int
{
Center, Stretch
}
public class Setter {
public const int SetDesktopWallpaper = 20;
public const int UpdateIniFile = 0x01;
public const int SendWinIniChange = 0x02;
[DllImport("user32.dll", SetLastError = true, CharSet = CharSet.Auto)]
private static extern int SystemParametersInfo (int uAction, int uParam, string lpvParam, int fuWinIni);
public static void SetWallpaper ( string path, Wallpaper.Style style ) {
SystemParametersInfo( SetDesktopWallpaper, 0, path, UpdateIniFile | SendWinIniChange );
RegistryKey key = Registry.CurrentUser.OpenSubKey("Control Panel\\Desktop", true);
switch( style )
{
case Style.Stretch :
key.SetValue(@"WallpaperStyle", "2") ;
key.SetValue(@"TileWallpaper", "0") ;
break;
case Style.Center :
key.SetValue(@"WallpaperStyle", "1") ;
key.SetValue(@"TileWallpaper", "0") ;
break;
}
key.Close();
}
}
}
"@
[Wallpaper.Setter]::SetWallpaper( $Path, $Style )
}


$URL="http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=10"
$TIME=Get-Date
$FILE=-Join("${HOME}\Pictures\bing_", $TIME.ToString('yyyyMMdd'), ".jpg")
if (!(Test-Path $FILE)) {
	$DATA=Invoke-WebRequest $URL -UseBasicParsing
	$JSON=ConvertFrom-Json($DATA)
	$IMG_URL=-Join("http://www.bing.com", $JSON.images[0].url)
	Invoke-WebRequest $IMG_URL -OutFile $FILE -UseBasicParsing
	Set-Wallpaper -Path $FILE
}