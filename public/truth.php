<?php
echo "<h1>SERVER TRUTH CHECK</h1>";
echo "<b>Loaded php.ini:</b> " . php_ini_loaded_file() . "<br>";
echo "<b>Upload Max Filesize:</b> " . ini_get('upload_max_filesize') . "<br>";
echo "<b>Post Max Size:</b> " . ini_get('post_max_size') . "<br>";
echo "<b>Max Execution Time:</b> " . ini_get('max_execution_time') . "<br>";
?>
