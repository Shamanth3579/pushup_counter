#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiAP.h>
#include <WebServer.h>

#define BUTTON_PIN 12
#define LED_PIN 33

WebServer server(80);

int counter = 0;

// Serve the current pushup count at "/"
void handleRoot() {
  server.send(200, "text/plain", String(counter));
}

// Reset endpoint, sets the counter to zero
void handleReset() {
  counter = 0;
  server.send(200, "text/plain", "OK");
}

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  // Create WiFi Access Point
  WiFi.softAP("ESP32-CAM-AP", "esp12345");
  Serial.println("📶 ESP32 running as Access Point");
  Serial.print("🔗 IP address: ");
  Serial.println(WiFi.softAPIP());

  // Handle HTTP requests
  server.on("/", handleRoot);
  server.on("/reset", handleReset);
  server.begin();
}

void loop() {
  server.handleClient();

  static bool lastButtonState = HIGH;
  bool currentButtonState = digitalRead(BUTTON_PIN);

  // Detect button press (falling edge)
  if (lastButtonState == HIGH && currentButtonState == LOW) {
    counter++;
    Serial.print("🔘 Button pressed: ");
    Serial.println(counter);

    digitalWrite(LED_PIN, HIGH);
    delay(100);
    digitalWrite(LED_PIN, LOW);
  }

  lastButtonState = currentButtonState;
}