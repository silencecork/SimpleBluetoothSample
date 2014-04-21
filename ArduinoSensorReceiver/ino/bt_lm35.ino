#include <SoftwareSerial.h>

const int RX = 8;
const int TX = 6;
const int SENSOR = 0;

SoftwareSerial blueToothSerial(RX, TX);

void setup() {
  Serial.begin(9600);
  while (!Serial) {
    ;
  }
  blueToothSerial.begin(38400);

  Serial.println("Setup Finish");
}

void loop() {
  if(blueToothSerial.available()){
    blueToothSerial.read();
    int val = analogRead(SENSOR);
    int dat=(5.0 * val * 100.0)/1024.0;
    blueToothSerial.print(String(dat, DEC));
    blueToothSerial.println("C");
    Serial.print(String(dat, DEC)); 
    Serial.println("C");
  }
  delay(500);
}
