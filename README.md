**# pushup_counter**

**WHAT IS THE PURPOSE:**

it motivates u while doing pushups 
keep track of ur progress

**HOW CIRCUIT WORKS:**

A push button is connected to GPIO12 of esp32 cam ,the esp32 acts as the Access point with the credentials(SSID:ESP32_CAM-AP and password:esp12345)
then it connects to the andriod app and sends HTTP request for every 1000ms using HTTP sockets.The app then displays the current count and says the number when incremented using text to speech.It has a reset button to start new set.

**HOW WAS IT BUILT:**

I had  very limited resources to make any standardized circuit.I has 3x esp32 cam modules ,and I thought to use one of them. It had the builtin WiFi and bluetooth function,Initially my plan is it build everything with hardware ,But had to face so many problems. Some of them like ,it draws so much power,amplifier needed analog signal as input but esp32 cam doesn't have any analog outpins available so I have to convert it to 8khz PCM signal and feed to amplifier . This resulted in distorted voice output and unbearable humming noise.
Then I was looking for alternatives and decided to build an app that can do all the fundamental functions of the project.
with the help of GitHub Copilot the coding part is done.

**THINGS TO DO TO BUILD THIS PROJECT FOR YOURSELF:**

1.install ArduinoIDE and AndroidStudio(install all the required drivers also).
2.flash the Arduino code called 'esp_wifi_AP' onto the esp32cam. 
3.Open androidstudio > newproject.
4.find these files **1**.MainActivity.kt **2**.activity_main.xml **3**.AndroidManifest.xml **4**.network_security.xml **5**.values\themes.xml **6**.night\themes.xml **7**.colors.xml **8**.layouts.xml **9**.strings.xml. 
Or simply download the app from the drive link https://drive.google.com/file/d/1ZIl8sAu8jKkSKMLIYjqs5ZR1KIqi4-17/view?usp=drive_link

**working video sample:**

https://drive.google.com/file/d/1WGVdGMnF76qSVxOJDxkCQGbdLjT_FAD8/view?usp=drive_link
