<?php
/* This file is a part of MapPIN'on OSM (http://mappin.hp2.jp/ ).*/ 
echo '<?xml version="1.0" encoding="UTF-8"?>'; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta http-equiv="Content-Language" content="ja"/>
        <meta name="copyright" content="Copyright reserved by Shun N. Watanabe. See http://mappin.hp2.jp/ for the detail."/>
        <link rel="shortcut icon" type="image/png" href="icons/logo48.png" />
        <link rel="icon" type="image/x-icon" href="favicon.ico" />
        <link rel="stylesheet" href="css/list.css" type="text/css"/>
        <title>MapPIN'on OSM - Registration form of RSS</title>
    </head>
    <body>
        <h1>MapPIN'on OSM - Registration form of RSS</h1>
        <?php
if($url=$_REQUEST['rss']){
    $url=trim($url);
    if(strpos($url,"http://")===0){
        $f=fopen("data/new_rss.txt","a");
        fwrite($f,$url."\n");
        fclose($f);
        echo "<strong>It have been listed. Wait the next updating.</strong>";
    } else {
        echo "<strong>It doesnt look a url.</strong>";
    }
}
        ?>
        <p><a href="index.html">back to the map</a>, <a href="blog/">go to the blog</a></p>
        <p>Input URL of a RSS of your photo blog site that you want to show on MapPIN'on OSM</p>
        <p><form action="registration.php" method="post">
            <input type="text" name="rss"/><input type="submit"/>
        </form></p>
        <table><tr><th>The URL of registering RSS</th></tr>
<?php
if($f=@fopen("data/new_rss.txt","r")){
    while($url=fgets($f,1500)){
        echo '<tr><td><a href="'.$url.'">'.$url.'</a></td></tr>'."\n";
    }
    fclose($f);
}
?>
        </table>
    </body>
</html>
