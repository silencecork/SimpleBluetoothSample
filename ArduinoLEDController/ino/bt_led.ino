#include <SoftwareSerial.h>

const int RX = 8;
const int TX = 6;
const int LED = 13;

SoftwareSerial blueToothSerial(RX, TX);

void setup() {
  pinMode(LED, OUTPUT);
  blueToothSerial.begin(38400);
}

void loop() {
  if(blueToothSerial.available()){
    char c = blueToothSerial.read();
    if (c == '1') {
      digitalWrite(LED, HIGH);
    } 
    else {
      digitalWrite(LED, LOW);
    }
  }
  delay(500);
}

