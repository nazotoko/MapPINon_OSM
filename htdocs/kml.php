<?php
$bbox=$_GET['bbox'];
$box=preg_split("/,/",$bbox,4);
$x=round(($box[0]+$box[2])*10);
$y=round(($box[1]+$box[3])*10);

header('Content-type: text/xml');
echo '<?xml version="1.0" encoding="UTF-8"?>'."\n";
?>
<kml xmlns="http://earth.google.com/kml/2.2">
<Document>
<?php
#echo "<name>Photo tile (x=".$x.",y=".$y.") of MapPIN'on OSM</name>";
#$lat=$x/20;
#$lon=$y/20;
#$url='http://mappin.hp2.jp/?lat='.$lat.'&amp;lon='.$lon.'&amp;zoom=14';
#echo "<atom:link>".$url."</atom:link>";
#echo '<description>Photo tile around lat='.($x/20).', lon='.($y/20)." of MapPIN'on OSM</description>";
#echo "<Region><LatLonAltBox><south>".($lat-0.025)."</south><west>".($lon-0.025)."</west><north>".($lat+0.025)."</north><east>".($lon+0.025)."</east></LatLonAltBox></Region>";
$fname="data/photo".(($x>0)?'+':'').trim($x).(($y>0)?'+':'').trim($y).".js";
$f=@fopen($fname,"r");
if($f){
    while($line=fgets($f,1500)){
        if(strncmp($line,"AJAXI",5) && strncmp($line,"});",3) && preg_match("/,s:0,/",$line,$s)>0) {
            preg_match("/^([0-9.]+):{/",$line,$id);
            preg_match("/la:([-0-9.]+),/",$line,$lat);
            preg_match("/lo:([-0-9.]+),/",$line,$lon);
            preg_match("/,o:'([^']+)'[,}]/",$line,$o);
            preg_match("/,li:'([^']+)'[,}]/",$line,$li);
            preg_match("/,ti:'([^']+)'[,}]/",$line,$ti);
            preg_match("/,th:'([^']+)'[,}]/",$line,$th);
            $url='http://mappin.hp2.jp/?lat='.$lat[1].'&amp;lon='.$lon[1].'&amp;zoom=17&amp;id='.$id[1];
            echo '<Placemark id="MapPINon'.$id[1].'">'."\n";
            echo "\t".'<description>'."\n";
            echo "\t\t".'<![CDATA[';
            echo '<a href="'.$li[1].'"><img src="'.$th[1].'" alt="thumbnail" /></a>';
            echo ']]>'."\n";
            echo "\t".'</description>'."\n";
            echo "\t".'<name>'.$ti[1].'</name>'."\n";
            echo "\t".'<Icon>'."\n";
            echo "\t\t".'<href>'.$li[1].'</href>'."\n";
            echo "\t".'</Icon>'."\n";
            echo "\t".'<Point>'."\n";
            echo "\t\t".'<coordinates>'.$lon[1].','.$lat[1].'</coordinates>'."\n";
            echo "\t".'</Point>'."\n";
            echo "".'</Placemark>'."\n";
        }
    }
    fclose($f);
}
?>
</Document>
</kml>
