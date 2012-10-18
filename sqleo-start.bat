:: use :: on begining of line to comment
:: This bat file for windows OS only runs SQLeo app with specified look and feel
:: (personally nimbus one is looking cool)
:: Note - Java should be already in your PATH variable
:: Note - Specify correctly SQLeoVQB jar file name with correct path of jar

java -Dcom.sqleo.laf.class=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel -jar SQLeoVQB-2012.07Beta.jar

:: Other available look and feels from jdk,replace the class name below mentioned in above command 
:: javax.swing.plaf.metal.MetalLookAndFeel (this is default one)
:: com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
:: com.sun.java.swing.plaf.windows.WindowsLookAndFeel
:: com.sun.java.swing.plaf.motif.MotifLookAndFeel
:: com.sun.java.swing.plaf.gtk.GTKLookAndFeel (For some linux variants like Ubuntu)

:: This one is external look and feel similar to Nimbus but more MacOS styled  
:: download the seaglasslookandfeel-0.2.jar from http://seaglass.googlecode.com/svn/doc/downloads.html
:: and place it just beside sqleo jar and use following command to launch SQLeo

:: java -classpath .;SQLeoVQB-20.09.12.jar;seaglasslookandfeel-0.2.jar -Dcom.sqleo.laf.class=com.seaglasslookandfeel.SeaGlassLookAndFeel com.sqleo.environment.Application



