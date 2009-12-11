<?php
/* This file is a part of MapPIN'on OSM (http://mappin.hp2.jp/ ).*/ 
$lang="en";
if($_GET['lang']){
    $langlist=array($_GET['lang']);
}else {
    $langlist = explode(',', $_SERVER['HTTP_ACCEPT_LANGUAGE']);
}
foreach($langlist as $curLang) {
  $curLang = explode(';', $curLang);
  if (preg_match('/(en|ja)-?.*/', $curLang[0], $reg)) {
    $lang = $reg[1];
    break;
  }
}
include("lang/$lang.php");

echo '<?xml version="1.0" encoding="UTF-8"?>'; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php
echo '<html xmlns="http://www.w3.org/1999/xhtml" lang="'.$lang.'" xml:lang="'.$lang.'">';
?>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<?php
echo '        <meta http-equiv="Content-Language" content="'.$lang.'"/>';
?>
        <meta name="copyright" content="Copyright reserved by Shun N. Watanabe. See http://mappin.hp2.jp/ for the detail."/>
        <link rel="shortcut icon" type="image/png" href="icons/logo48.png" />
        <link rel="icon" type="image/x-icon" href="favicon.ico" />
        <link rel="stylesheet" href="css/list.css" type="text/css"/>
        <title>MapPIN'on OSM - Registration form of RSS</title>
    </head>
    <body>
        <?php
        echo '<h1>MapPIN&apos;on OSM - '.$message['registration'].'</h1>';
$reload=trim($_REQUEST['reload']);
if($reload){
    $f=fopen("data/request.txt","a");
    fwrite($f,"reload:".$reload."\n");
    fclose($f);
}
$remove=trim($_REQUEST['remove']);
if($remove){
    $f=fopen("data/request.txt","a");
    fwrite($f,"remove:".$remove."\n");
    fclose($f);
}
$url=$_REQUEST['rss'];
if($url){
    $url=trim($url);
    if(strpos($url,"http://")===0||strpos($url,"flickr:")===0) {
        $f=fopen("data/new_rss.txt","a");
        fwrite($f,$url."\n");
        fclose($f);
        echo '<strong>'.$message['success'].'</strong>';
    } else {
        echo '<strong>'.$message['fail'].'</strong>';
    }
}
        ?>
        <p><a href="index.html">back to the map</a>, <a href="blog/">go to the blog</a></p>
<?php
echo '<h3>'.$message['agreement_t'].'</h3>';
echo $message['agreement'];
?>
    <br/>
    <form action="registration.php" method="post">
        <table>
            <tr><th>The URI of registering RSS</th></tr>
            <tr><td><input type="text" name="rss"/>
<?php
echo '<input type="submit" value="'.$message['agree'].'"/>';
?>
        </td></tr>
<?php
if($f=@fopen("data/new_rss.txt","r")){
    while($url=fgets($f,1500)){
        echo '<tr><td><a href="'.$url.'">'.$url.'</a></td></tr>'."\n";
    }
    fclose($f);
}
?>
        </table>
    </form>
<br/>
<?php
if($f=@fopen('data/request.txt','r')){
    echo '<table><tr><th>Request query</th></tr>';
    while($url=fgets($f,1500)){
        echo '<tr><td>'.$url.'</td></tr>'."\n";
    }
    fclose($f);
    echo '</table>';
}
?>
    </body>
</html>
