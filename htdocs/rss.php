<?php header('Content-type: application/xml+rss');
echo '<?xml version="1.0" encoding="UTF-8"?>'; ?>
<rss version="2.0" xmlns:georss="http://www.georss.org/georss">
<channel>
<language>en</language>
<?php
$x=$_GET["x"];
$y=$_GET["y"];
echo "<title>Photo tile (x=".$x.",y=".$y.") of MapPIN'on OSM</title>";
$lat=$x/20;
$lon=$y/20;
$url='http://mappin.hp2.jp/?lat='.$lat.'&amp;lon='.$lon.'&amp;zoom=14';
echo "<link>".$url."</link>";
echo '<description>Photo tile around lat='.($x/20).', lon='.($y/20)." of MapPIN'on OSM</description>";
echo "<georss:point>".$lat." ".$lon."</georss:point>";
echo "<georss:box>".($lat-0.025)." ".($lon-0.025)." ".($lat+0.025)." ".($lon+0.025)."</georss:box>";
$fname="data/photo".(($x>0)?'+':'').trim($x).(($y>0)?'+':'').trim($y).".js";
$f=@fopen($fname,"r");
if($f){
    while($line=fgets($f,1500)){
        if(strncmp($line,"AJAXI",5)&&strncmp($line,"})",2)) {
            preg_match("/^([0-9.]+):{/",$line,$id);
            preg_match("/la:([-0-9.]+),/",$line,$lat);
            preg_match("/lo:([-0-9.]+),/",$line,$lon);
            preg_match("/,li:'([^']+)'[,}]/",$line,$li);
            preg_match("/,ti:'([^']+)'[,}]/",$line,$ti);
            preg_match("/,th:'([^']+)'[,}]/",$line,$th);
            echo '<item>';
            echo '<title>'.$ti[1].'</title>';
            $url='http://mappin.hp2.jp/?lat='.$lat[1].'&amp;lon='.$lon[1].'&amp;zoom=17&amp;id='.$id[1];
            echo '<link>'.$url.'</link>';
            echo '<description><![CDATA[';
            echo '<img src="'.$th[1].'" /><br/>';
            echo '<a href="'.$url.'" >lat='.$lat[1].', lon='.$lon[1].'</a><br/>';
            echo '<a href="'.$li[1].'" >link</a>';
            echo ']]></description>';
            echo "<georss:point>".$lat[1]." ".$lon[1]."</georss:point>";
            if(preg_match("/,al:([-0-9.]+)[,}]/",$line,$al)>0) {
                echo "<georss:ele>".$al[1]."</georss:ele>";
            }
            echo '</item>'."\n";
        }
    }
    fclose($f);
}
?>
</channel>
</rss>
