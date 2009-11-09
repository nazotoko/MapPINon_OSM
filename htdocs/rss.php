<?php header('Content-type: application/xml+rss');
echo '<?xml version="1.0" encoding="UTF-8"?>'; ?>
<rss version="2.0">
<channel>
<link>http://mappin.hp2.jp/</link>
<language>en</language>
<?php
$x=$_GET["x"];
$y=$_GET["y"];
echo "<title>Photo tile (x=".$x.",y=".$y.") of MapPIN'on OSM</title>";
$fname="data/photo".(($x>0)?'+':'').trim($x).(($y>0)?'+':'').trim($y).".js";
$f=@fopen($fname,"r");
if($f){
    while($line=fgets($f,1500)){
        if(strncmp($line,"AJAXI",5)&&strncmp($line,"})",2)) {
            preg_match("/^([0-9.]+):{/",$line,$id);
            preg_match("/la:([0-9.]+),/",$line,$lat);
            preg_match("/lo:([0-9.]+),/",$line,$lon);
            preg_match("/,ti:'([^']+)'/",$line,$ti);
            preg_match("/,th:'([^']+)'/",$line,$th);
            echo '<item>';
            echo '<title>'.$ti[1].'</title>';
            echo '<link>http://mappin.hp2.jp/?lat='.$lat[1].'&amp;lon='.$lon[1].'&amp;zoom=17&amp;id='.$id[1].'</link>';
            echo '<description>';
            echo '&lt;img src="'.$th[1].'" /&gt;lat='.$lat[1].', lon='.$lon[1];
            echo '</description>';
            echo '</item>'."\n";
        }
    }
    fclose($f);
}
?>
</channel>
</rss>