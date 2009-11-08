<?php
$base64="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.";
$code=$_GET['c'];

$base=1;
$lon=0;
$i=0;
while($i<4){
    $mod=strpos($base64,$code[$i]);
    $lon+=$mod*$base;
    $i++;
    $base*=64;
}
$mod=strpos($base64,$code[4]);
$lon+=($mod % 32)*$base;
$lon=$lon/1000000.0-180.0;

$lat=$mod>>5;
$i=5;
$base=2;
while($i<9){
    $mod=strpos($base64,$code[$i]);
    $lat+=$mod*$base;
    $i++;
    $base*=64;
}
$mod=strpos($base64,$code[9]);
$lat+=($mod % 16)*$base;

$lat=$lat/1000000.0-90.0;

$id=$mod>>4;
$i=10;
$base=4;
while($i<strlen($code) && $i<15) {
    $mod=strpos($base64,$code[$i]);
    $id+=$mod*$base;
    $i++;
    $base*=64;
}

header("Location: http://mappin.hp2.jp/?lon=".$lon."&lat=".$lat."&zoom=17&id=".$id);