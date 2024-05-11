# YALDP


Yet Another Lidar Project
-------------------------

![Alt text](https://raw.githubusercontent.com/RobbyGit/YALDP/main/images/PXL_20240511_075508858.MP.jpg)


I wanted a way to use my RPLIDAR A1M8 Lidar on my Android phone using OTG.
You need an Android phone that supports OTG else this would not work. I am sure you could use the WIFI for the ESP32
but that in my experience is very slow .

After searching and trying  https://github.com/carlemil/RPLidarA2AndroidThingsAPI I could not get the data out correctly.
Carl did a fantastic job but i just could not get the protocols to give me correct data and not that the RPLIDAR is very old, the documentation is hard to find.
I have left the aar in this project if you want to try and use it.

I decided to go back to using an Arduino, this worked really well but I had to change a few things in the RPLIDAR library to get it to work.

I started out using a ESP32S3 and chose this Arduino as it has so many other uses 4G/GPS and I could get the serials to work. 
This is the unit I used https://www.makerfabs.com/esp32s3-4g-lte.html but I am sure you can use any ESP32 if it has 2 x serial ports.
Its not an easy board to understand but after playing around for some time , i got the hang of how to use it.
This is my now go to board for all my projects.

I find using adb remotely helped during the development and i suggest you use this or you will get frustrated.
Here is a link to help get started with that https://stackoverflow.com/questions/2604727/how-can-i-connect-to-android-with-adb-over-tcp

Here is the Link to to the Arduino code and how to change the rplidar library.

Im sure there are so many things that can be done better and I will work through them.

Things to do:
--------------
1. Turn on and off the motor (Not sure this can work with the RPLIDAR)
2. Add a view that can zoom and move
3. Check data integrate better and reduce the serial packet size.
4. Better hardware handling of connecting and disconnection