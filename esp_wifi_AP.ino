#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiAP.h>
#include <WebServer.h>

#define BUTTON_PIN 12
#define LED_PIN 33

WebServer server(80);

int counter = 0;

void handleRoot() {
  server.send(200, "text/plain", String(counter));
}

void handleReset() {
  counter = 0;
  server.send(200, "text/plain", "OK");
}

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  WiFi.softAP("ESP32-CAM-AP", "esp12345");
  Serial.println("📶 ESP32 running as Access Point");
  Serial.print("🔗 IP address: ");
  Serial.println(WiFi.softAPIP());

  server.on("/", handleRoot);
  server.on("/reset", handleReset);
  server.begin();
}

void loop() {
  server.handleClient();

  static bool buttonWasPressed = false;
  static unsigned long lastDebounceTime = 0;
  const unsigned long debounceDelay = 50; // 50 ms debounce

  bool buttonState = digitalRead(BUTTON_PIN) == LOW; // true = pressed

  if (buttonState && !buttonWasPressed && (millis() - lastDebounceTime > debounceDelay)) {
    // Button just pressed and stable
    counter++;
    Serial.print("🔘 Button pressed: ");
    Serial.println(counter);

    digitalWrite(LED_PIN, HIGH);
    delay(100);
    digitalWrite(LED_PIN, LOW);

    buttonWasPressed = true;
    lastDebounceTime = millis();
  } else if (!buttonState && buttonWasPressed) {
    // Button released
    buttonWasPressed = false;
    lastDebounceTime = millis();
  }
}
