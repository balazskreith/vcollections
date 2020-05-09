## 0.2.3

 - Possibility to retrieve configuration value from Storage builders implemented the 
 IStorageBuilder. IStorageBuilder#getConfiguration enforces all Storage builder 
 to provide a method retrieves the value object to a given key.
 Moreover the provided key can refer to an embedded map by using a dot(".") as a 
 separator between the keys.
   

## 0.2.2

Initial version