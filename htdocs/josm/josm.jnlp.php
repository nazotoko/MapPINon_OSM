<?php
header('Content-type: application/x-java-jnlp-file');
echo '<?xml version="1.0" encoding="UTF-8"?>'."\n";
$version=2631;
?>
<!DOCTYPE jnlp PUBLIC "-//Sun Microsystems, Inc.//DTD JNLP 1.5//EN" "http://www.netbeans.org/jnlp/DTD/jnlp.dtd">
<?php
echo '<jnlp codebase="http://josm.openstreetmap.de/download/" href="josm.jnlp" version="'.$version.'">'."\n";
?>
    <information>
        <title>JOSM</title>
        <vendor>OpenStreetMap</vendor>
        <homepage href="http://josm.openstreetmap.de/"/>
        <description>Java OpenStreetMap editor</description>
        <description kind="one-line">JOSM</description>
        <description kind="tooltip">JOSM</description>
        <icon href="http://josm.openstreetmap.de/svn/trunk/images/logo.png"/>
        <shortcut>
            <desktop/> <menu/>
        </shortcut>
        <offline-allowed/>
    </information>
    <resources>
        <j2se version="1.5+" initial-heap-size="64m" max-heap-size="1024m" />
<?php
echo '<jar href="josm-snapshot-'.$version.'.jar"/>'."\n";
?>
    </resources>
    <security>
        <all-permissions/>
    </security>
    <application-desc main-class="JOSM">
     　<?php if($_GET['left']){
          $bbox=$_GET['bottom'].','.$_GET['left'].','.$_GET['top'].','.$_GET['right'];
          echo"<argument>";
          echo '--download='.$bbox;
          echo "</argument>\n";
          echo"<argument>";
          echo '--downloadgps='.$bbox;
          echo "</argument>\n";
      }
    ?></application-desc>
</jnlp>
